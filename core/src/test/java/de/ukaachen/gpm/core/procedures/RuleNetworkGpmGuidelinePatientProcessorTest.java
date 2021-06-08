package de.ukaachen.gpm.core.procedures;


import static de.ukaachen.gpm.core.model.patient.Performer.DEVICE;
import static de.ukaachen.gpm.core.model.patient.Performer.HUMAN;
import static de.ukaachen.gpm.core.model.patient.TimePhase.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static de.ukaachen.gpm.core.util.TestConstants.*;
import static org.hamcrest.collection.IsIn.isIn;

import de.ukaachen.gpm.core.exceptions.BpmnNotProcessableException;
import de.ukaachen.gpm.core.model.patient.GPMPatient;
import de.ukaachen.gpm.core.model.patient.GuidelineItem;
import de.ukaachen.gpm.core.model.rule_network.RuleNetwork;
import de.ukaachen.gpm.core.model.rule_network.RuleNetworkBuilder;
import de.ukaachen.gpm.core.model.rule_network.SortedItems;
import de.ukaachen.gpm.core.model.rule_network.TaskDetailTexts;
import de.ukaachen.gpm.core.procedures.task_sorting.CyclicNetworkSorter;
import de.ukaachen.gpm.core.procedures.task_sorting.GPMSorter;
import de.ukaachen.gpm.core.util.DependencyMapBuilder;
import de.ukaachen.gpm.core.util.GPMPatientMock;
import de.ukaachen.gpm.core.util.Pair;
import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.Test;
import org.kie.api.event.rule.DebugRuleRuntimeEventListener;

public class RuleNetworkGpmGuidelinePatientProcessorTest {

  private final BiConsumer<Collection<GuidelineItem>, GuidelineItem> assertContains =
      (collection, item) -> assertThat(item, isIn(collection));

  private final DebugRuleRuntimeEventListener debugRuleRuntimeEventListener =
      new DebugRuleRuntimeEventListener();

  private void testForFileWithExpectedItemsWithDependencies(
      final String fileName, final Map<GuidelineItem, Collection<GuidelineItem>> dependencyMap) {
    testForFileAndPatientWithExpectedDependencies(
        fileName, new GPMPatientMock(), dependencyMap);
  }

  private void testForFileAndPatientWithExpectedDependencies(
      final String fileName,
      final GPMPatient gpmPatient,
      final Map<GuidelineItem, Collection<GuidelineItem>> dependencyMap) {

    // Given
    final File file = new File(
        getClass().getResource(fileName).getFile());
    final BpmnModelInstance bpmnModelInstance = Bpmn.readModelFromFile(file);
    final RuleNetwork ruleNetwork = new RuleNetworkCreator(bpmnModelInstance,
        new RuleNetworkBuilder(), new TaskDetailTexts()).create();
    final DroolsFactsHandler droolsFactsHandler = new DroolsFactsHandler(ruleNetwork);
    final KieSessionProvider kieSessionProvider =
        new KieSessionProvider(droolsFactsHandler.getNetworkFacts(), debugRuleRuntimeEventListener);

    final RuleNetworkTimePhaseClassifier classifier =
        new RuleNetworkTimePhaseClassifier(kieSessionProvider, droolsFactsHandler);
    final SortedItems sortedItems = new CyclicNetworkSorter(ruleNetwork, GPMSorter::new)
        .sort();
    final RuleNetworkGpmGuidelinePatientProcessor processor =
        new RuleNetworkGpmGuidelinePatientProcessor(classifier, sortedItems,
            new ActivityVisibilityDeterminator(bpmnModelInstance));

    // When
    final List<GuidelineItem> result = processor.process(gpmPatient);

    // Then
    assertResults(result, dependencyMap);
  }

  /**
   * Performs all necessary assertions
   * @param actualGuidelineItems Actual result from the tested code
   * @param dependencyMap A map that specifies for each item which other items depend on it (i.e.
   * have to come later than the key-item). This is necessary, because the exact order is
   * non-deterministic (this might change in the future, but it is like that for now).
   * The keySet of this map is also the set of expected items.
   */
  private void assertResults(
      final List<GuidelineItem> actualGuidelineItems,
      final Map<GuidelineItem, Collection<GuidelineItem>> dependencyMap) {

    assertGuidelineItems(actualGuidelineItems, dependencyMap.keySet());
    DependencyMapBuilder.assertDependencies(actualGuidelineItems, dependencyMap);
  }


  private void assertGuidelineItems(
      final Collection<GuidelineItem> actualGuidelineItems,
      final Collection<GuidelineItem> expectedGuidelineItems) {
    assertThat(actualGuidelineItems.size(), is(expectedGuidelineItems.size()));
    actualGuidelineItems.forEach(item -> assertContains.accept(expectedGuidelineItems, item));
  }

  @Test
  public void testWithSimplePathLikeWorkflow() throws BpmnNotProcessableException {
    final GPMPatientMock guidelinePatient = new GPMPatientMock();
    guidelinePatient.addCodeMapping("HEART_RATE", "LOINC:HR");
    guidelinePatient.putPatientProperty("LOINC:HR", "66");

    final GuidelineItem[] expectedGuidelineItems = new GuidelineItem[]{
        GuidelineItem.task("A")
            .desc("Say Hello")
            .timePhase(PAST)
            .noHandledProperties()
            .performer(DEVICE),
        GuidelineItem.task("B")
            .desc("Confirm that Patient has received treatment B")
            .timePhase(PRESENT)
            .property(Pair.create("B", "SMITH:B"))
            .value(B, null)
            .performer(DEVICE),
        GuidelineItem.task("C")
            .desc("Say Goodbye")
            .timePhase(FUTURE)
            .noHandledProperties()
            .performer(DEVICE)
    };

    final Map<GuidelineItem, Collection<GuidelineItem>> dependencyMap =
        new DependencyMapBuilder<GuidelineItem>()
            .item(expectedGuidelineItems[0])
            .dependsOnNothing()
            .item(expectedGuidelineItems[1])
            .dependsOn(expectedGuidelineItems[0])
            .item(expectedGuidelineItems[2])
            .dependsOn(expectedGuidelineItems[0], expectedGuidelineItems[1])
            .getDependencyMap();

    testForFileAndPatientWithExpectedDependencies("guideline-patient-processor.bpmn",
        guidelinePatient, dependencyMap);
  }

  @Test
  public void testWithSimplePathLikeWorkflow2() throws BpmnNotProcessableException {
    final GPMPatientMock guidelinePatient = new GPMPatientMock();
    guidelinePatient.addCodeMapping("HEART_RATE", "LOINC:HR");
    guidelinePatient.putPatientProperty("LOINC:HR", "66");
    guidelinePatient.addCodeMapping("B", "SMITH:B");
    guidelinePatient.putPatientProperty("SMITH:B", "true");

    final GuidelineItem[] expectedGuidelineItems = new GuidelineItem[]{
        GuidelineItem.task("A")
            .desc("Say Hello")
            .timePhase(PAST)
            .noHandledProperties()
            .performer(DEVICE),
        GuidelineItem.task("B")
            .desc("Confirm that Patient has received treatment B")
            .timePhase(PAST)
            .property(Pair.create("B", "SMITH:B"))
            .value(B, true)
            .performer(DEVICE),
        GuidelineItem.task("C")
            .desc("Say Goodbye")
            .timePhase(PAST)
            .noHandledProperties()
            .performer(DEVICE)
    };

    final Map<GuidelineItem, Collection<GuidelineItem>> dependencyMap =
        new DependencyMapBuilder<GuidelineItem>()
            .item(expectedGuidelineItems[0])
            .dependsOnNothing()
            .item(expectedGuidelineItems[1])
            .dependsOn(expectedGuidelineItems[0])
            .item(expectedGuidelineItems[2])
            .dependsOn(expectedGuidelineItems[0], expectedGuidelineItems[1])
            .getDependencyMap();

    testForFileAndPatientWithExpectedDependencies("guideline-patient-processor.bpmn",
        guidelinePatient, dependencyMap);
  }

  @Test
  public void testWithSimplePathLikeWorkflow3() throws BpmnNotProcessableException {
    final GPMPatientMock guidelinePatient = new GPMPatientMock();
    guidelinePatient.addCodeMapping("HEART_RATE", "LOINC:HR");
    guidelinePatient.putPatientProperty("LOINC:HR", "66");
    guidelinePatient.addCodeMapping("B", "SMITH:B");
    guidelinePatient.putPatientProperty("SMITH:B", "false");

    final GuidelineItem[] expectedGuidelineItems = new GuidelineItem[]{
        GuidelineItem.task("A")
            .desc("Say Hello")
            .timePhase(PAST)
            .noHandledProperties()
            .performer(DEVICE),
        GuidelineItem.task("B")
            .desc("Confirm that Patient has received treatment B")
            .timePhase(PAST)
            .property(Pair.create("B", "SMITH:B"))
            .value(B, false)
            .performer(DEVICE),
    };

    final Map<GuidelineItem, Collection<GuidelineItem>> dependencyMap =
        new DependencyMapBuilder<GuidelineItem>()
            .item(expectedGuidelineItems[0])
            .dependsOnNothing()
            .item(expectedGuidelineItems[1])
            .dependsOn(expectedGuidelineItems[0])
            .getDependencyMap();

    testForFileAndPatientWithExpectedDependencies("guideline-patient-processor.bpmn",
        guidelinePatient, dependencyMap);
  }

  @Test
  public void testWithComplexWorkflow() throws BpmnNotProcessableException {
    final GPMPatientMock guidelinePatient = new GPMPatientMock();
    guidelinePatient.putPatientProperty("SMITH:A", "true");
    guidelinePatient.putPatientProperty("SMITH:B", "50");
    guidelinePatient.addCodeMapping(A, "SMITH:A");
    guidelinePatient.addCodeMapping(B, "SMITH:B");

    final GuidelineItem[] xpctd = new GuidelineItem[]{
        GuidelineItem.task("A").desc("dA").property(Pair.create("A", "SMITH:A"))
            .timePhase(PAST).value(A, true).performer(DEVICE),
        GuidelineItem.task("B").desc("dB").property(Pair.create("B", "SMITH:B"))
            .timePhase(PAST).value(B, 50).performer(DEVICE),
        GuidelineItem.task("C").desc("dC").property(Pair.create("C", "SMITH:C"))
            .timePhase(PRESENT).value(C, null).performer(DEVICE),
        GuidelineItem.task("D").desc("dD").property(Pair.create("D", "SMITH:D"))
            .timePhase(PRESENT).value(D, null).performer(DEVICE),
        GuidelineItem.task("E").desc("dE").property(Pair.create("E", "SMITH:E"))
            .timePhase(PRESENT).value(E, null).performer(DEVICE),
        GuidelineItem.task("G").desc("dG").property(Pair.create("G", "SMITH:G"))
            .timePhase(FUTURE).value(G, null).performer(DEVICE),
        GuidelineItem.task("I").desc("dI").property(Pair.create("I", "SMITH:I"))
            .timePhase(FUTURE).value(I, null).performer(DEVICE),
    };

    final Map<GuidelineItem, Collection<GuidelineItem>> dependencyMap =
        new DependencyMapBuilder<GuidelineItem>()
            .item(xpctd[0]).dependsOnNothing()
            .item(xpctd[1]).dependsOnNothing()
            .item(xpctd[2]).dependsOn(xpctd[0])
            .item(xpctd[3]).dependsOn(xpctd[0])
            .item(xpctd[4]).dependsOn(xpctd[1])
            .item(xpctd[5]).dependsOn(xpctd[0], xpctd[1], xpctd[3], xpctd[4])
            .item(xpctd[6]).dependsOn(xpctd[0], xpctd[1], xpctd[2], xpctd[3], xpctd[4], xpctd[5])
            .getDependencyMap();

    testForFileAndPatientWithExpectedDependencies(
        "guideline-patient-processor-complex.bpmn", guidelinePatient, dependencyMap);
  }

  @Test
  public void testOptionalTextAnnotation() throws BpmnNotProcessableException {
    final GPMPatient testPatient = new GPMPatientMock();

    final GuidelineItem[] expected = new GuidelineItem[]{
        GuidelineItem.task("A")
            .noHandledProperties()
            .timePhase(PAST)
            .desc("Annotation of Task A")
            .performer(DEVICE),
        GuidelineItem.task("B")
            .noHandledProperties()
            .timePhase(PAST)
            .desc("B")
            .performer(DEVICE),
        GuidelineItem.task("C")
            .noHandledProperties()
            .timePhase(PAST)
            .desc("C")
            .performer(DEVICE)
    };

    final Map<GuidelineItem, Collection<GuidelineItem>> dependencyMap =
        new DependencyMapBuilder<GuidelineItem>()
            .item(expected[0]).dependsOnNothing()
            .item(expected[1]).dependsOn(expected[0])
            .item(expected[2]).dependsOn(expected[0], expected[1])
            .getDependencyMap();

    testForFileAndPatientWithExpectedDependencies(
        "guideline-patient-processor-optional-annotation.bpmn", testPatient, dependencyMap);
  }

  @Test
  public void testExampleDiagram01ThatWasUploadedToConfluenceDocumentation()
      throws BpmnNotProcessableException {
    final GPMPatientMock guidelinePatient = new GPMPatientMock();
    guidelinePatient.putPatientProperty("LOINC:XYZ-2", "55");
    guidelinePatient.putPatientProperty("SMITH:BRADYCARDIA", null);
    guidelinePatient.addCodeMapping("HEART_RATE", "LOINC:XYZ-2");
    guidelinePatient.addCodeMapping("BRADYCARDIA", "SMITH:BRADYCARDIA");

    final GuidelineItem[] expected = new GuidelineItem[]{
        GuidelineItem.task("Task_0ya5bli")
            .property(Pair.create("HEART_RATE", "LOINC:XYZ-2"))
            .timePhase(PAST)
            .desc("Falls die Beschreibung im Task selbst nicht ausreicht, wuerde man eine "
                + "Annotation nutzen, um eine umfangreichere Beschreibung zu liefern.")
            .value("HEART_RATE", 55)
            .performer(DEVICE),
        GuidelineItem.task("Task_1a71jga")
            .timePhase(PRESENT)
            .property(Pair.create("BRADYCARDIA", "SMITH:BRADYCARDIA"))
            .desc("Diagnostiziere: Bradykardie")
            .value("BRADYCARDIA", null)
            .performer(DEVICE)
    };

    final Map<GuidelineItem, Collection<GuidelineItem>> dependencyMap =
        new DependencyMapBuilder<GuidelineItem>()
            .item(expected[0]).dependsOnNothing()
            .item(expected[1]).dependsOn(expected[0])
            .getDependencyMap();

    testForFileAndPatientWithExpectedDependencies(
        "guideline-patient-processor-confluence-example-01.bpmn", guidelinePatient, dependencyMap);
  }

  @Test
  public void testWhenGivenBpmnWithNormalTask_AGuidelineItemWithHumanPerformerIsCreated() {
    final GPMPatientMock guidelinePatient = new GPMPatientMock();
    final GuidelineItem expected = GuidelineItem.task(A)
        .timePhase(PAST)
        .noHandledProperties()
        .desc(A)
        .performer(HUMAN);

    final Map<GuidelineItem, Collection<GuidelineItem>> dependencyMap =
        new DependencyMapBuilder<GuidelineItem>()
            .item(expected).dependsOnNothing()
            .getDependencyMap();

    testForFileAndPatientWithExpectedDependencies(
        "guideline-patient-processor-human-task.bpmn",
        guidelinePatient,
        dependencyMap);
  }

  @Test
  public void testWcp01SequenceGPMPatientProcessor() {
    final GuidelineItem itemA = GuidelineItem.task(A).timePhase(PAST).desc(A).performer(DEVICE);
    final GuidelineItem itemB = GuidelineItem.task(B).timePhase(PAST).desc(B).performer(DEVICE);

    final Map<GuidelineItem, Collection<GuidelineItem>> dependencyMap =
        new DependencyMapBuilder<GuidelineItem>()
            .item(itemA).dependsOn()
            .item(itemB).dependsOn(itemA)
            .getDependencyMap();

    testForFileWithExpectedItemsWithDependencies("patterns/wcp-01-sequence.bpmn", dependencyMap);
  }

  @Test
  public void testWcp02ParallelSplitGPMPatientProcessor() {
    final GuidelineItem itemA = GuidelineItem.task(A).timePhase(PAST).desc(A).performer(DEVICE);
    final GuidelineItem itemB = GuidelineItem.task(B).timePhase(PAST).desc(B).performer(DEVICE);
    final GuidelineItem itemC = GuidelineItem.task(C).timePhase(PAST).desc(C).performer(DEVICE);
    final GuidelineItem itemD = GuidelineItem.task(D).timePhase(PAST).desc(D).performer(DEVICE);
    final GuidelineItem itemE = GuidelineItem.task(E).timePhase(PAST).desc(E).performer(DEVICE);
    final Map<GuidelineItem, Collection<GuidelineItem>> dependencyMap =
        new DependencyMapBuilder<GuidelineItem>()
            .item(itemA).dependsOnNothing()
            .item(itemB).dependsOn(itemA)
            .item(itemC).dependsOn(itemA)
            .item(itemD).dependsOn(itemA, itemB)
            .item(itemE).dependsOn(itemA, itemC)
            .getDependencyMap();

    testForFileWithExpectedItemsWithDependencies("patterns/wcp-02-par-spl.bpmn", dependencyMap);
  }

  @Test
  public void testWcp03SynchronizationGPMPatientProcessor() {
    final GuidelineItem itemA =
        GuidelineItem.task(A).timePhase(PRESENT).desc(A).property(Pair.create(A, cA))
            .value(A, null).performer(DEVICE);
    final GuidelineItem itemB =
        GuidelineItem.task(B).timePhase(PRESENT).desc(B).property(Pair.create(B, cB))
            .value(B, null).performer(DEVICE);
    final GuidelineItem itemC =
        GuidelineItem.task(C).timePhase(FUTURE).desc(C).property(Pair.create(C, cC))
            .value(C, null).performer(DEVICE);
    final Map<GuidelineItem, Collection<GuidelineItem>> dependencyMap =
        new DependencyMapBuilder<GuidelineItem>()
            .item(itemA).dependsOnNothing()
            .item(itemB).dependsOnNothing()
            .item(itemC).dependsOn(itemA, itemB)
            .getDependencyMap();

    testForFileWithExpectedItemsWithDependencies("patterns/wcp-03-synch.bpmn", dependencyMap);
  }

  @Test
  public void testWcp04ExclusiveChoiceGPMPatientProcessor() {
    final GuidelineItem itemA =
        GuidelineItem.task(A).timePhase(PRESENT).desc(A).property(Pair.create(A, cA))
            .value(A, null).performer(DEVICE);
    final GuidelineItem itemB = GuidelineItem.task(B).timePhase(FUTURE).desc(B).performer(DEVICE);
    final GuidelineItem itemC = GuidelineItem.task(C).timePhase(FUTURE).desc(C).performer(DEVICE);
    final Map<GuidelineItem, Collection<GuidelineItem>> dependencyMap =
        new DependencyMapBuilder<GuidelineItem>()
            .item(itemA).dependsOnNothing()
            .item(itemB).dependsOn(itemA)
            .item(itemC).dependsOn(itemA)
            .getDependencyMap();

    testForFileWithExpectedItemsWithDependencies("patterns/wcp-04-ex-ch.bpmn", dependencyMap);
  }

  @Test
  public void testWcp05SimpleMergeGPMPatientProcessor() {
    final GuidelineItem itemA =
        GuidelineItem.task(A).property(Pair.create(A, cA))
            .timePhase(PAST).desc(A).value(A, true).performer(DEVICE);
    final GuidelineItem itemB =
        GuidelineItem.task(B).property(Pair.create(B, cB))
            .timePhase(PRESENT).desc(B)
            .value(B, null).performer(DEVICE);
    final GuidelineItem itemC = GuidelineItem.task(C).timePhase(FUTURE).desc(C).performer(DEVICE);
    final GPMPatientMock guidelinePatient = new GPMPatientMock();
    guidelinePatient.putPatientProperty(cA, "true");
    guidelinePatient.addCodeMapping(A, cA);

    final Map<GuidelineItem, Collection<GuidelineItem>> dependencyMap =
        new DependencyMapBuilder<GuidelineItem>()
            .item(itemA).dependsOnNothing()
            .item(itemB).dependsOnNothing()
            .item(itemC).dependsOn(itemA, itemB)
            .getDependencyMap();

    testForFileAndPatientWithExpectedDependencies(
        "patterns/wcp-05-simple-m.bpmn",
        guidelinePatient,
        dependencyMap);
  }

  @Test
  public void testWhenContainsCycle_isStillProcessedCorrectly() {
    final GPMPatientMock guidelinePatient = new GPMPatientMock();
    final GuidelineItem itemA = GuidelineItem.task(A).desc(A).timePhase(PAST).performer(HUMAN);
    final GuidelineItem itemB = GuidelineItem.task(B).desc(B).timePhase(PAST).performer(HUMAN);

    final Map<GuidelineItem, Collection<GuidelineItem>> dependencyMap =
        new DependencyMapBuilder<GuidelineItem>()
            .item(itemA).dependsOnNothing()
            .item(itemB).dependsOn(itemA)
            .getDependencyMap();

    testForFileAndPatientWithExpectedDependencies(
        "guideline-patient-processor-cycle.bpmn",
        guidelinePatient,
        dependencyMap
    );
  }

  @Test
  public void testWhenContainsInvisibleActivity_thisActivityIsNotShownInResult() {
    final GPMPatientMock guidelinePatient = new GPMPatientMock();
    final GuidelineItem itemFromVisible =
        GuidelineItem.task("Task_0pwgpve")
            .desc("Task_0pwgpve")
            .timePhase(FUTURE)
            .property(Pair.create("ASDFQWER", "SMITH:SOME_PROPERTY_2"))
            .value("ASDFQWER", null)
            .performer(HUMAN);

    final Map<GuidelineItem, Collection<GuidelineItem>> dependencyMap =
        new DependencyMapBuilder<GuidelineItem>()
            .item(itemFromVisible).dependsOnNothing()
            .getDependencyMap();

    final String fileName = "guideline-patient-processor-invisible-activity.bpmn";
    final File file = new File(
        getClass().getResource(fileName).getFile());
    final BpmnModelInstance bpmnModelInstance = Bpmn.readModelFromFile(file);

    testForFileAndPatientWithExpectedDependencies(
        fileName,
        guidelinePatient,
        dependencyMap
    );
  }
}
