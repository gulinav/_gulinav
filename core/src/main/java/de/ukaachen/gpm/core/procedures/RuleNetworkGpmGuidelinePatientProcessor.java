package de.ukaachen.gpm.core.procedures;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import de.ukaachen.gpm.core.model.patient.GuidelineItem;
import de.ukaachen.gpm.core.model.patient.GPMPatient;
import de.ukaachen.gpm.mks.model.PatientProperty;
import de.ukaachen.gpm.core.model.patient.TimePhase;
import de.ukaachen.gpm.core.model.rule_network.SortedItems;
import de.ukaachen.gpm.core.model.rule_network.Vertex;
import de.ukaachen.gpm.core.model.rule_network.VertexType;
import de.ukaachen.gpm.core.util.Pair;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class RuleNetworkGpmGuidelinePatientProcessor {

  private final RuleNetworkTimePhaseClassifier timePhaseClassifier;
  private final ImmutableList<String> sortedItemsTemplate;
  private final ImmutableSet<String> invisibleActivities;

  public RuleNetworkGpmGuidelinePatientProcessor(
      final RuleNetworkTimePhaseClassifier timePhaseClassifier,
      final SortedItems sortedItems,
      final ActivityVisibilityDeterminator visibilityDeterminator) {
    this.timePhaseClassifier = timePhaseClassifier;
    this.sortedItemsTemplate = sortedItems.getVertexIds();
    this.invisibleActivities =
        ImmutableSet.copyOf(visibilityDeterminator.determineIdsOfInvisibleActivities());
  }

  public List<GuidelineItem> process(final GPMPatient patient) {
    final Map<Vertex, TimePhase> vertexToTimePhase = timePhaseClassifier.classify(patient);
    return createGuidelineItems(vertexToTimePhase, patient);
  }

  private List<GuidelineItem> createGuidelineItems(
      final Map<Vertex, TimePhase> vertexToTimePhase,
      final GPMPatient patient) {
    final Map<String, Vertex> vertexIdToVertex = new HashMap<>();
    vertexToTimePhase.keySet().forEach(vertex -> vertexIdToVertex.put(vertex.getId(), vertex));

    return sortedItemsTemplate.stream()
        .filter(id -> vertexIdToVertex.get(id).getType() == VertexType.ACTIVITY)
        .filter(id -> !invisibleActivities.contains(id))
        .filter(id -> vertexToTimePhase.get(vertexIdToVertex.get(id)) != TimePhase.UNREACHABLE)
        .map(id -> GuidelineItem
            .task(id)
            .timePhase(vertexToTimePhase.get(vertexIdToVertex.get(id)))
            .desc(vertexIdToVertex.get(id).getDescription())
            .property(vertexIdToVertex.get(id).getHandledProperty())
            .performer(vertexIdToVertex.get(id).getPerformer())
            .detailText(vertexIdToVertex.get(id).getDetailText())
        )
        .peek(item -> this.insertPropertyValues(item, patient))
        .peek(item -> this.insertExternalId(item, patient))
        .collect(Collectors.toList());
  }

  private void insertPropertyValues(final GuidelineItem item,
      final GPMPatient patient) {
    item.getHandledProperties()
        .forEach((k, v) -> {
          final PatientProperty patientProperty = patient.findByHumanReadableKey(k);
          final Object value = patientProperty != null ? patientProperty.getValue() : null;
          item.value(k, value);
        });
  }

  private void insertExternalId(final GuidelineItem item,
      final GPMPatient patient) {
    final Optional<Pair<String, String>> prop = item.getSingleHandledProperty();
    prop.ifPresent(theProp -> {
      final PatientProperty patientProperty = patient
          .findByHumanReadableKey(theProp.f());

      if (patientProperty != null && patientProperty.getExternalId() != null) {
        item.basedOnExternalEntity(patientProperty.getExternalId());
      }
    });
  }
}
