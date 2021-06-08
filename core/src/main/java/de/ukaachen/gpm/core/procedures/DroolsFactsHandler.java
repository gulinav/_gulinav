package de.ukaachen.gpm.core.procedures;


import de.ukaachen.gpm.mks.model.Code;
import de.ukaachen.gpm.core.model.patient.GPMPatient;
import de.ukaachen.gpm.core.model.rule_network.RuleNetwork;
import de.ukaachen.gpm.core.model.rule_network.Vertex;
import de.ukaachen.gpm.core.model.rule_network.VertexType;
import de.ukaachen.gpm.core.model.rule_network.initial_facts.network_facts.Edg;
import de.ukaachen.gpm.core.model.rule_network.initial_facts.network_facts.NetworkFacts;
import de.ukaachen.gpm.core.model.rule_network.initial_facts.network_facts.Vtx;
import de.ukaachen.gpm.core.model.rule_network.initial_facts.patient_facts.ConditionSatisfied;
import de.ukaachen.gpm.core.model.rule_network.initial_facts.patient_facts.PatientFacts;
import de.ukaachen.gpm.core.model.rule_network.initial_facts.patient_facts.PotentiallyConditionSatisfied;
import de.ukaachen.gpm.core.model.rule_network.initial_facts.patient_facts.VertexFinished;
import de.ukaachen.gpm.core.util.Pair;
import de.ukaachen.gpm.mks.model.TernaryEvaluationResponse;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Getter;

public class DroolsFactsHandler {

  @Getter
  private final RuleNetwork ruleNetwork;

  @Getter
  private final NetworkFacts networkFacts;

  private final ConditionFactsPreparator conditionFactsPreparator;
  private final HashMap<String, Vtx> vertexFacts;

  public DroolsFactsHandler(final RuleNetwork ruleNetwork) {
    this.ruleNetwork = ruleNetwork;

    this.vertexFacts = new HashMap<>();
    final Set<Edg> edgeFacts = new HashSet<>();

    ruleNetwork.getVertices().forEach(vertex -> {
      final String id = vertex.getId();
      final VertexType type = vertex.getType();

      vertexFacts.put(vertex.getId(), new Vtx(id, type));
    });

    ruleNetwork.getEdges().forEach(edge -> {
      final String id = edge.getId();
      final Vtx source = vertexFacts.get(edge.getSource().getId());
      final Vtx target = vertexFacts.get(edge.getTarget().getId());

      edgeFacts.add(new Edg(id, source, target));
    });

    this.networkFacts = new NetworkFacts(new HashSet<>(vertexFacts.values()), edgeFacts);

    this.conditionFactsPreparator = new ConditionFactsPreparator(this.ruleNetwork,
        this.networkFacts);
  }

  public PatientFacts createPatientFacts(final GPMPatient patient) {
    final Pair<Set<ConditionSatisfied>, Set<PotentiallyConditionSatisfied>> conditionFacts =
        this.conditionFactsPreparator.determineEdgesWhoseConditionIsSatisfied(patient);

    final Set<VertexFinished> patientProperties = this.ruleNetwork.getVertices().stream()
        .filter(vertex -> vertexIsFinished(patient, vertex))
        .map(vertex -> this.vertexFacts.get(vertex.getId()))
        .map(VertexFinished::new)
        .collect(Collectors.toSet());

    return new PatientFacts(conditionFacts.f(), conditionFacts.s(), patientProperties);
  }

  private boolean vertexIsFinished(final GPMPatient patient, final Vertex vertex) {
    return vertex.getHandledProperty() == null ||
        (patient.findByHumanReadableKey(vertex.getHandledProperty().f()) != null &&
            (patient.findByHumanReadableKey(vertex.getHandledProperty().f()).getValue() != null
                && patient.findByHumanReadableKey(vertex.getHandledProperty().f()).getValue()
                != TernaryEvaluationResponse.UNKNOWN) ||
            patient.findByCode(new Code(vertex.getHandledProperty().s())) != null &&
                patient.findByCode(new Code(vertex.getHandledProperty().s())).getValue() != null
                && patient.findByCode(new Code(vertex.getHandledProperty().s())).getValue()
                != TernaryEvaluationResponse.UNKNOWN);
  }

}
