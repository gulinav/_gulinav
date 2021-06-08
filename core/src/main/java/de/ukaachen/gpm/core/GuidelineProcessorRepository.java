package de.ukaachen.gpm.core;

import de.ukaachen.gpm.core.model.patient.GPMPatient;
import de.ukaachen.gpm.core.model.patient.GuidelineItem;
import de.ukaachen.gpm.core.model.rule_network.RuleNetwork;
import de.ukaachen.gpm.core.model.rule_network.RuleNetworkBuilder;
import de.ukaachen.gpm.core.model.rule_network.SortedItems;
import de.ukaachen.gpm.core.model.rule_network.TaskDetailTexts;
import de.ukaachen.gpm.core.model.rule_network.initial_facts.network_facts.NetworkFacts;
import de.ukaachen.gpm.core.procedures.ActivityVisibilityDeterminator;
import de.ukaachen.gpm.core.procedures.DroolsFactsHandler;
import de.ukaachen.gpm.core.procedures.KieSessionProvider;
import de.ukaachen.gpm.core.procedures.RuleNetworkCreator;
import de.ukaachen.gpm.core.procedures.RuleNetworkGpmGuidelinePatientProcessor;
import de.ukaachen.gpm.core.procedures.RuleNetworkTimePhaseClassifier;
import de.ukaachen.gpm.core.procedures.task_sorting.CyclicNetworkSorter;
import de.ukaachen.gpm.core.procedures.task_sorting.GPMSorter;
import de.ukaachen.gpm.core.util.ObjInsertedEventListener;
import de.ukaachen.gpm.mks.model.Code;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Getter;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.kie.api.event.rule.DebugRuleRuntimeEventListener;
import org.springframework.stereotype.Component;

@Component
public class GuidelineProcessorRepository {

  private final Map<String, RuleNetworkGpmGuidelinePatientProcessor> map;
  @Getter
  private final Map<String, Map<String, Code>> codeMappingByGPMProcessor;

  private final DebugRuleRuntimeEventListener debugListener = new ObjInsertedEventListener();

  private final Map<String, String> originalGuidelineBpmnById = new HashMap<>();

  public GuidelineProcessorRepository() {
    this.map = new HashMap<>();
    this.codeMappingByGPMProcessor = new HashMap<>();
  }

  private void saveOriginalBpmnForLater(final String id, final String bpmn) {
    originalGuidelineBpmnById.put(id, bpmn);
  }

  public String getOriginalGuideline(final String id) {
    return originalGuidelineBpmnById.get(id);
  }

  public void addGuideline(final String id, final InputStream bpmnInput) {
    if (map.get(id) != null) {
      throw new IllegalArgumentException(
          String.format("There is already a guideline registered with "
              + "the provided id: %s", id));
    }
    final String model = new BufferedReader(
        new InputStreamReader(bpmnInput, StandardCharsets.UTF_8))
        .lines()
        .collect(Collectors.joining("\n"));

    final InputStream bpmnInputAgain =
        new ByteArrayInputStream(model.getBytes(StandardCharsets.UTF_8));

    final BpmnModelInstance bpmnModel = Bpmn.readModelFromStream(bpmnInputAgain);
    saveOriginalBpmnForLater(id, model);

    final RuleNetworkBuilder ruleNetworkBuilder = new RuleNetworkBuilder();
    final TaskDetailTexts taskDetailTexts = new TaskDetailTexts();

    final RuleNetwork ruleNetwork =
        new RuleNetworkCreator(bpmnModel, ruleNetworkBuilder, taskDetailTexts).create();

    final DroolsFactsHandler droolsFactsHandler = new DroolsFactsHandler(ruleNetwork);
    final NetworkFacts networkFacts = droolsFactsHandler.getNetworkFacts();

    final KieSessionProvider kieSessionProvider = new KieSessionProvider(networkFacts,
        debugListener);

    final RuleNetworkTimePhaseClassifier classifier =
        new RuleNetworkTimePhaseClassifier(kieSessionProvider, droolsFactsHandler);

    final SortedItems sortedItems = new CyclicNetworkSorter(ruleNetwork, GPMSorter::new).sort();

    final ActivityVisibilityDeterminator visDeterm =
        new ActivityVisibilityDeterminator(bpmnModel);

    final RuleNetworkGpmGuidelinePatientProcessor processor =
        new RuleNetworkGpmGuidelinePatientProcessor(classifier, sortedItems, visDeterm);

    final Map<String, Code> codeMap = new HashMap<>();
    ruleNetwork.getVertices().forEach(v -> {
      if (v.getHandledProperty() != null) {
        if (v.getHandledProperty().f() == null || v.getHandledProperty().s() == null) {
          throw new IllegalStateException(String.format("Guideline with id %s has incomplete "
              + "handled property in vertex %s", id, v.getId()));
        }
        codeMap.put(v.getHandledProperty().f(), new Code(v.getHandledProperty().s()));
      }
    });

    map.put(id, processor);
    codeMappingByGPMProcessor.put(id, codeMap);
  }

  public List<GuidelineItem> process(final String processorId, final GPMPatient patient) {
    final RuleNetworkGpmGuidelinePatientProcessor processor = map.get(processorId);

    final Map<String, Code> codeMap = codeMappingByGPMProcessor.get(processorId);
    for (final String key : codeMap.keySet()) {
      if (!patient.getKnownHumanReadableKeys().contains(key)) {
        patient.addProperties(key, codeMap.get(key), Collections.emptyList());
      }
    }

    if (processor == null) {
      throw new IllegalArgumentException("Unknown gpmProcessor-id: " + processorId);
    }

    return processor.process(patient);
  }


}
