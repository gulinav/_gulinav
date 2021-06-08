package de.ukaachen.gpm.core.procedures;

import static de.ukaachen.gpm.core.model.patient.TimePhase.*;

import static junit.framework.TestCase.assertEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static de.ukaachen.gpm.core.util.TestConstants.*;

import de.ukaachen.gpm.core.model.patient.GPMPatient;
import de.ukaachen.gpm.core.model.patient.TimePhase;
import de.ukaachen.gpm.core.model.rule_network.RuleNetwork;
import de.ukaachen.gpm.core.model.rule_network.RuleNetworkBuilder;
import de.ukaachen.gpm.core.model.rule_network.TaskDetailTexts;
import de.ukaachen.gpm.core.model.rule_network.Vertex;
import de.ukaachen.gpm.core.util.ExpectedTimePhaseBuilder;
import de.ukaachen.gpm.core.util.GPMPatientMock;
import de.ukaachen.gpm.core.util.MinimalisticObjectInsertedRuntimeEventListener;

import de.ukaachen.gpm.core.util.TestPatientBuilder;
import java.io.File;
import java.util.Map;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.kie.api.event.rule.DebugRuleRuntimeEventListener;

public class RuleNetworkTimePhaseClassifierTest {

  @Rule
  public final ExpectedException expectedException = ExpectedException.none();

  private final DebugRuleRuntimeEventListener debugRuleRuntimeEventListener =
      new MinimalisticObjectInsertedRuntimeEventListener();

  @Test
  public void testWcp01SequenceTimePhaseClassifier() {
    testForRuleNetworkAndOnePatient(
        getRuleNetworkFromFile("patterns/wcp-01-sequence.bpmn"),
        new GPMPatientMock(),
        ExpectedTimePhaseBuilder.forOnePatient()
            .forTimePhase(PAST).addVertices(A, B)
            .forTimePhase(NOT_CLASSIFIABLE).addVertices(S, T)
            .getExpectedTimePhases()
    );
  }

  @Test
  public void testWcp02ParallelSplitTimePhaseClassifier() {
    testForRuleNetworkAndOnePatient(
        getRuleNetworkFromFile("patterns/wcp-02-par-spl.bpmn"),
        new GPMPatientMock(),
        ExpectedTimePhaseBuilder.forOnePatient()
            .forTimePhase(PAST).addVertices(A, B, C, D, E)
            .forTimePhase(NOT_CLASSIFIABLE).addVertices(S, T, GATEWAY_01)
            .getExpectedTimePhases()
    );
  }

  @Test
  public void testWcp03SynchronizationTimePhaseClassifier() {
    testForMultiplePatients(
        "patterns/wcp-03-synch.bpmn",
        TestPatientBuilder.initForPatient("patient_01")
            .property(A, cA, "true").property(B, cB, "true")
            .forTestPatient("patient_02")
            .property(A, cA, "true").property(B, cB, null)
            .forTestPatient("patient_03")
            .property(A, cA, "true").property(B, cB, "false")
            .forTestPatient("patient_04")
            .property(A, cA, null).property(B, cB, "true")
            .forTestPatient("patient_05")
            .property(A, cA, null).property(B, cB, null)
            .forTestPatient("patient_06")
            .property(A, cA, null).property(B, cB, "false")
            .forTestPatient("patient_07")
            .property(A, cA, "false").property(B, cB, "true")
            .forTestPatient("patient_08")
            .property(A, cA, "false").property(B, cB, null)
            .forTestPatient("patient_09")
            .property(A, cA, "false").property(B, cB, "false")
            .getTestPatients(),
        ExpectedTimePhaseBuilder.forInitialPatient("patient_01")
            .forTimePhase(NOT_CLASSIFIABLE).addVertices(S, T, GATEWAY_01)
            .forTimePhase(PAST).addVertices(A, B)
            .forTimePhase(PRESENT).addVertices(C)
            .forPatient("patient_02")
            .forTimePhase(NOT_CLASSIFIABLE).addVertices(S, T, GATEWAY_01)
            .forTimePhase(PAST).addVertices(A)
            .forTimePhase(PRESENT).addVertices(B)
            .forTimePhase(FUTURE).addVertices(C)
            .forPatient("patient_03")
            .forTimePhase(NOT_CLASSIFIABLE).addVertices(S, T, GATEWAY_01)
            .forTimePhase(PAST).addVertices(A, B)
            .forTimePhase(UNREACHABLE).addVertices(C)
            .forPatient("patient_04")
            .forTimePhase(NOT_CLASSIFIABLE).addVertices(S, T, GATEWAY_01)
            .forTimePhase(PAST).addVertices(B)
            .forTimePhase(PRESENT).addVertices(A)
            .forTimePhase(FUTURE).addVertices(C)
            .forPatient("patient_05")
            .forTimePhase(NOT_CLASSIFIABLE).addVertices(S, T, GATEWAY_01)
            .forTimePhase(PRESENT).addVertices(A, B)
            .forTimePhase(FUTURE).addVertices(C)
            .forPatient("patient_06")
            .forTimePhase(NOT_CLASSIFIABLE).addVertices(S, T, GATEWAY_01)
            .forTimePhase(PRESENT).addVertices(A)
            .forTimePhase(PAST).addVertices(B)
            .forTimePhase(UNREACHABLE).addVertices(C)
            .forPatient("patient_07")
            .forTimePhase(NOT_CLASSIFIABLE).addVertices(S, T, GATEWAY_01)
            .forTimePhase(PAST).addVertices(A, B)
            .forTimePhase(UNREACHABLE).addVertices(C)
            .forPatient("patient_08")
            .forTimePhase(NOT_CLASSIFIABLE).addVertices(S, T, GATEWAY_01)
            .forTimePhase(PRESENT).addVertices(B)
            .forTimePhase(PAST).addVertices(A)
            .forTimePhase(UNREACHABLE).addVertices(C)
            .forPatient("patient_09")
            .forTimePhase(NOT_CLASSIFIABLE).addVertices(S, T, GATEWAY_01)
            .forTimePhase(PAST).addVertices(A, B)
            .forTimePhase(UNREACHABLE).addVertices(C)
            .getExpectedTimePhasesPerPatient()
    );


  }

  @Test
  public void testWcp04ExclusiveChoiceTimePhaseClassifier() {
    testForMultiplePatients(
        "patterns/wcp-04-ex-ch.bpmn",
        TestPatientBuilder
            .initForPatient("patient_01")
            .property(A, cA, null)
            .forTestPatient("patient_02")
            .property(A, cA, "true")
            .forTestPatient("patient_03")
            .property(A, cA, "false")
            .getTestPatients(),
        ExpectedTimePhaseBuilder.forInitialPatient("patient_01")
            .forTimePhase(NOT_CLASSIFIABLE).addVertices(S, T, GATEWAY_01)
            .forTimePhase(PRESENT).addVertices(A)
            .forTimePhase(FUTURE).addVertices(B, C)
            .forPatient("patient_02")
            .forTimePhase(NOT_CLASSIFIABLE).addVertices(S, T, GATEWAY_01)
            .forTimePhase(PAST).addVertices(A, B)
            .forTimePhase(UNREACHABLE).addVertices(C)
            .forPatient("patient_03")
            .forTimePhase(NOT_CLASSIFIABLE).addVertices(S, T, GATEWAY_01)
            .forTimePhase(PAST).addVertices(A, C)
            .forTimePhase(UNREACHABLE).addVertices(B)
            .getExpectedTimePhasesPerPatient()
    );
  }

  @Test
  public void testWcp05SimpleMergeTimePhaseClassifier() {
    testForMultiplePatients(
        "patterns/wcp-05-simple-m.bpmn",
        TestPatientBuilder.initForPatient("patient_01")
            .property(A, cA, "true").property(B, cB, "true")
            .forTestPatient("patient_02")
            .property(A, cA, "true").property(B, cB, null)
            .forTestPatient("patient_03")
            .property(A, cA, "true").property(B, cB, "false")
            .forTestPatient("patient_04")
            .property(A, cA, null).property(B, cB, null)
            .forTestPatient("patient_05")
            .property(A, cA, null).property(B, cB, "false")
            .forTestPatient("patient_06")
            .property(A, cA, "false").property(B, cB, "false")
            .getTestPatients(),
        ExpectedTimePhaseBuilder.forInitialPatient("patient_01")
            .forTimePhase(NOT_CLASSIFIABLE).addVertices(S, T, GATEWAY_01)
            .forTimePhase(PAST).addVertices(A, B, C)
            .forPatient("patient_02")
            .forTimePhase(NOT_CLASSIFIABLE).addVertices(S, T, GATEWAY_01)
            .forTimePhase(PRESENT).addVertices(B)
            .forTimePhase(PAST).addVertices(A)
            .forTimePhase(FUTURE).addVertices(C)
            .forPatient("patient_03")
            .forTimePhase(NOT_CLASSIFIABLE).addVertices(S, T, GATEWAY_01)
            .forTimePhase(PAST).addVertices(A, B, C)
            .forPatient("patient_04")
            .forTimePhase(NOT_CLASSIFIABLE).addVertices(S, T, GATEWAY_01)
            .forTimePhase(PRESENT).addVertices(A, B)
            .forTimePhase(FUTURE).addVertices(C)
            .forPatient("patient_05")
            .forTimePhase(NOT_CLASSIFIABLE).addVertices(S, T, GATEWAY_01)
            .forTimePhase(PRESENT).addVertices(A)
            .forTimePhase(PAST).addVertices(B)
            .forTimePhase(FUTURE).addVertices(C)
            .forPatient("patient_06")
            .forTimePhase(NOT_CLASSIFIABLE).addVertices(S, T, GATEWAY_01)
            .forTimePhase(PAST).addVertices(A, B)
            .forTimePhase(UNREACHABLE).addVertices(C)
            .getExpectedTimePhasesPerPatient()
    );
  }

  @Test
  public void testThatTheClassifierWorksForASimpleWorkflow() {
    testForFileAndOnePatient("time-classifier-test-simple.bpmn",
        TestPatientBuilder.initForOnePatient()
            .property("HEART_RATE", "LOINC:XYZ", "55")
            .getPatient(),
        ExpectedTimePhaseBuilder.forOnePatient()
            .forTimePhase(NOT_CLASSIFIABLE).addVertices(S, T)
            .forTimePhase(PAST).addVertices(A)
            .forTimePhase(UNREACHABLE).addVertices(B)
            .getExpectedTimePhases()
    );
  }

  @Test
  public void testThatTheClassifierWorksForASimpleWorkflow2() {
    testForFileAndOnePatient("time-classifier-test-simple.bpmn",
        TestPatientBuilder.initForOnePatient()
            .property("HEART_RATE", "LOINC:XYZ", "70")
            .getPatient(),
        ExpectedTimePhaseBuilder.forOnePatient()
            .forTimePhase(NOT_CLASSIFIABLE).addVertices(S, T)
            .forTimePhase(PAST).addVertices(A, B)
            .getExpectedTimePhases()
    );
  }

  @Test
  public void testThatTheClassifierWorksForASimpleWorkflow3() {
    testForFileAndOnePatient("time-classifier-test-simple.bpmn",
        TestPatientBuilder.initForOnePatient()
            .property("HEART_RATE", "LOINC:XYZ", null)
            .getPatient(),
        ExpectedTimePhaseBuilder.forOnePatient()
            .forTimePhase(NOT_CLASSIFIABLE).addVertices(S, T)
            .forTimePhase(PRESENT).addVertices(A)
            .forTimePhase(FUTURE).addVertices(B)
            .getExpectedTimePhases()
    );
  }

  @Test
  public void testThatTheClassifierThrowsAnExceptionIfTheGivenPatientIsNull() {
    expectedException.expect(IllegalArgumentException.class);

    final RuleNetwork ruleNetwork = new RuleNetworkBuilder().build();
    final DroolsFactsHandler handler = new DroolsFactsHandler(ruleNetwork);

    final RuleNetworkTimePhaseClassifier classifier = new RuleNetworkTimePhaseClassifier(
        getKieSessionProviderForFile("time-classifier-test-simple.bpmn"), handler);

    // Should throw an IllegalArgumentException
    classifier.classify(null);
  }

  @Test
  public void testThatTheClassifierIsCorrectWhenNoStepCanBeTaken() {
    testForFileAndOnePatient("time-classifier-parallel-gateway.bpmn",
        TestPatientBuilder.initForOnePatient().getPatient(),
        ExpectedTimePhaseBuilder.forOnePatient()
            .forTimePhase(NOT_CLASSIFIABLE)
            .addVertices(S, "B-and-C", "C-and-D", "EndEvent_1ihjphm",
                "EndEvent_1sllsmf", "EndEvent_1wlhd2k", T, "EndEvent_14y485j", "EndEvent_1ftwxzw")
            .forTimePhase(PRESENT).addVertices(A)
            .forTimePhase(FUTURE).addVertices(B, C, D, E)
            .getExpectedTimePhases());
  }

  @Test
  public void testThatTheClassifierIsCorrectWhenStuckAtParallelMerge() {
    testForFileAndOnePatient("time-classifier-parallel-gateway.bpmn",
        TestPatientBuilder.initForOnePatient()
            .property(A, "SMITH:A", "true")
            .property(B, "SMITH:B", "true")
            .property(C, "SMITH:C", "true")
            .getPatient(),
        ExpectedTimePhaseBuilder.forOnePatient()
            .forTimePhase(NOT_CLASSIFIABLE)
            .addVertices(S, "B-and-C", "C-and-D", "EndEvent_1ihjphm",
                "EndEvent_1sllsmf", "EndEvent_1wlhd2k", T, "EndEvent_14y485j", "EndEvent_1ftwxzw")
            .forTimePhase(PRESENT).addVertices(D)
            .forTimePhase(PAST).addVertices(A, B, C)
            .forTimePhase(FUTURE).addVertices(E)
            .getExpectedTimePhases()
    );
  }


  @Test
  public void testThatTheClassifierClassifiesToPastPresentFutureForSimpleExample() {
    testForFileAndOnePatient("time-classifier-sequential.bpmn",
        TestPatientBuilder.initForOnePatient()
            .property(A, "SMITH:A", "true")
            .getPatient(),
        ExpectedTimePhaseBuilder.forOnePatient()
            .forTimePhase(NOT_CLASSIFIABLE).addVertices(S, T, "EndEvent_0dtk9i8")
            .forTimePhase(PAST).addVertices(A)
            .forTimePhase(PRESENT).addVertices(B)
            .forTimePhase(FUTURE).addVertices(C)
            .getExpectedTimePhases());
  }

  @Test
  public void testThatTheClassifierClassifiesToPastPresentFutureForBpmnWithParallelGateways() {
    testForFileAndOnePatient("time-classifier-parallel-gateway.bpmn",
        TestPatientBuilder.initForOnePatient()
            .property(A, "SMITH:A", "true")
            .property(B, "SMITH:B", null)
            .property(C, "SMITH:C", "true")
            .property(D, "SMITH:D", "true")
            .property(E, "SMITH:E", null)
            .getPatient(),
        ExpectedTimePhaseBuilder.forOnePatient()
            .forTimePhase(NOT_CLASSIFIABLE)
            .addVertices(S, "B-and-C", "C-and-D", "EndEvent_1ihjphm",
                "EndEvent_1sllsmf", "EndEvent_1wlhd2k", T, "EndEvent_14y485j", "EndEvent_1ftwxzw")
            .forTimePhase(PAST).addVertices(A, C)
            .forTimePhase(PRESENT).addVertices(B)
            .forTimePhase(FUTURE).addVertices(D, E)
            .getExpectedTimePhases()
    );
  }

  @Test
  public void testThatTheClassifierClassifiesCorrectlyIfAnExclusiveMergeIsPresent() {
    testForFileAndOnePatient("time-classifier-exclusive-gateway-merge.bpmn",
        TestPatientBuilder.initForOnePatient().getPatient(),
        ExpectedTimePhaseBuilder.forOnePatient()
            .forTimePhase(NOT_CLASSIFIABLE)
            .addVertices(S, T, "EndEvent_1jpjwsj", "EndEvent_1nzx4j5", GATEWAY_01, GATEWAY_02)
            .forTimePhase(PAST).addVertices(D)
            .forTimePhase(PRESENT).addVertices(A, B, E)
            .forTimePhase(FUTURE).addVertices(C, F)
            .getExpectedTimePhases()
    );
  }

  @Test
  public void testThatTheClassifierClassifiesAllToPastExceptEndIfEverythingHasBeenDone() {
    testForFileAndOnePatient("time-classifier-parallel-gateway.bpmn",
        TestPatientBuilder.initForOnePatient()
            .property(A, "SMITH:A", "true")
            .property(B, "SMITH:B", "true")
            .property(C, "SMITH:C", "true")
            .property(D, "SMITH:D", "true")
            .property(E, "SMITH:E", "true")
            .getPatient(),
        ExpectedTimePhaseBuilder.forOnePatient()
            .forTimePhase(PAST).addVertices(A, B, C, D, E)
            .forTimePhase(NOT_CLASSIFIABLE).addVertices(S, "B-and-C", "C-and-D", "EndEvent_1ihjphm",
            "EndEvent_1sllsmf", "EndEvent_1wlhd2k", T, "EndEvent_14y485j", "EndEvent_1ftwxzw")
            .getExpectedTimePhases()
    );
  }


  @Test
  public void testThatTheClassifierClassifiesAlreadyTakenStepsToFutureIfPossiblyTakenAgain() {
    testForFileAndOnePatient("time-classifier-task-taken-again.bpmn",
        TestPatientBuilder.initForOnePatient()
            .property(B, "SMITH:B", null)
            .getPatient(),
        ExpectedTimePhaseBuilder.forOnePatient()
            .forTimePhase(NOT_CLASSIFIABLE).addVertices(S, T, "B-and-C-and-D", "B-and-C")
            .forTimePhase(PAST).addVertices(A, C)
            .forTimePhase(PRESENT).addVertices(B)
            .forTimePhase(FUTURE).addVertices(D)
            .getExpectedTimePhases()
    );
  }


  @Test
  public void testComplexCase() {
    testForFileAndOnePatient("time-classifier-complex.bpmn",
        TestPatientBuilder.initForOnePatient()
            .property(A, "SMITH:A", "true")
            .property(B, "SMITH:B", "55")
            .property(D, "SMITH:D", "true")
            .property(E, "SMITH:E", "true")
            .property(F, "SMITH:F", "true")
            .getPatient(),
        ExpectedTimePhaseBuilder.forOnePatient()
            .forTimePhase(NOT_CLASSIFIABLE)
            .addVertices(S, T, "EndEvent_1uqok05", "EndEvent_1unzqla", "EndEvent_12273xn",
                "EndEvent_1bfpin6", "EndEvent_1615zwd", "EndEvent_1qp1cgc", "EndEvent_0wzjefv",
                "EndEvent_1but4q8", "A-and-B", "C-and-D", "D-and-E", "C-and-G")
            .forTimePhase(PAST).addVertices(A, B, D, E)
            .forTimePhase(PRESENT).addVertices(C, G)
            .forTimePhase(FUTURE).addVertices(I)
            .forTimePhase(UNREACHABLE).addVertices(F, H)
            .getExpectedTimePhases()
    );
  }

  @Test
  public void testWhenGraphHasPastCycle_stillProcessedCorrectly() {
    testForFileAndOnePatient("time-classifier-cycle-future.bpmn",
        TestPatientBuilder.initForOnePatient()
            .property(A, "SMITH:A", "true")
            .property(B, "SMITH:B", "true")
            .property(C, "SMITH:C", "true")
            .getPatient(),
        ExpectedTimePhaseBuilder.forOnePatient()
            .forTimePhase(NOT_CLASSIFIABLE).addVertices(S, T)
            .forTimePhase(PAST).addVertices(A, B, C)
            .getExpectedTimePhases()
    );
  }

  @Test
  public void testWhenGraphHasFutureCycle_stillProcessedCorrectly() {
    testForFileAndOnePatient("time-classifier-cycle-future.bpmn",
        TestPatientBuilder.initForOnePatient()
            .property(A, "SMITH:A", null)
            .property(B, "SMITH:B", null)
            .property(C, "SMITH:C", null)
            .getPatient(),
        ExpectedTimePhaseBuilder.forOnePatient()
            .forTimePhase(NOT_CLASSIFIABLE).addVertices(S, T)
            .forTimePhase(PRESENT).addVertices(C)
            .forTimePhase(FUTURE).addVertices(A, B)
            .getExpectedTimePhases()
    );
  }

  @Test
  public void testWhenGraphHasCycleThatIsPresent_stillProcessedCorrectly() {
    testForFileAndOnePatient("time-classifier-cycle-present.bpmn",
        TestPatientBuilder.initForOnePatient()
            .property(A, "SMITH:A", "true")
            .property(B, "SMITH:B", null)
            .getPatient(),
        ExpectedTimePhaseBuilder.forOnePatient()
            .forTimePhase(NOT_CLASSIFIABLE).addVertices(S, T)
            .forTimePhase(FUTURE).addVertices(A)
            .forTimePhase(PRESENT).addVertices(B).getExpectedTimePhases()
    );
  }

  @Test
  public void testWhenGraphHasUnreachableCycle_stillProcessedCorrectly() {
    testForFileAndOnePatient("time-classifier-cycle-unreachable.bpmn",
        TestPatientBuilder.initForOnePatient()
            .property(A, "SMITH:A", null)
            .property(B, "SMITH:B", null)
            .property(C, "SMITH:C", "false")
            .getPatient(),
        ExpectedTimePhaseBuilder.forOnePatient()
            .forTimePhase(NOT_CLASSIFIABLE).addVertices(S, T)
            .forTimePhase(PAST).addVertices(C)
            .forTimePhase(UNREACHABLE).addVertices(A, B)
            .getExpectedTimePhases()
    );
  }

  @Test
  public void testComplexCycles() {
    testForMultiplePatients("time-classifier-cycle-complex.bpmn",
        TestPatientBuilder.initForPatient("p1")
            .property(A, "SMITH:A", null)
            .property(B, "SMITH:B", null)
            .property(C, "SMITH:C", null)
            .property(D, "SMITH:D", null)
            .property(E, "SMITH:E", null)
            .property(F, "SMITH:F", null)
            .property(G, "SMITH:G", null)
            .property(H, "SMITH:H", null)
            .forTestPatient("p2")
            .property(A, "SMITH:A", null)
            .property(B, "SMITH:B", null)
            .property(C, "SMITH:C", null)
            .property(D, "SMITH:D", null)
            .property(E, "SMITH:E", null)
            .property(F, "SMITH:F", null)
            .property(G, "SMITH:G", null)
            .property(H, "SMITH:H", "some_value")
            .getTestPatients(),
        ExpectedTimePhaseBuilder.forInitialPatient("p1")
            .forTimePhase(PRESENT).addVertices(C, H)
            .forTimePhase(FUTURE).addVertices(A, B, D, E, F, G)
            .forTimePhase(NOT_CLASSIFIABLE).addVertices(S, T)
            .forPatient("p2")
            .forTimePhase(PRESENT).addVertices(C, D)
            .forTimePhase(PAST).addVertices(H)
            .forTimePhase(FUTURE).addVertices(A, B, E, F, G)
            .forTimePhase(NOT_CLASSIFIABLE).addVertices(S, T)
            .getExpectedTimePhasesPerPatient()
    );
  }


  private void testForMultiplePatients(
      final String fileName,
      final Map<String, GPMPatient> testPatients,
      final Map<String, Map<String, TimePhase>> expectedTimePhasesPerPatient) {

    final RuleNetwork ruleNetwork = getRuleNetworkFromFile(fileName);
    testPatients.keySet().forEach(patientId ->
        testForRuleNetworkAndOnePatient(
            ruleNetwork,
            testPatients.get(patientId),
            expectedTimePhasesPerPatient.get(patientId)
        ));
  }

  private void testForFileAndOnePatient(
      final String fileName,
      final GPMPatient patient,
      final Map<String, TimePhase> expectedTimePhases) {

    final RuleNetwork ruleNetwork = getRuleNetworkFromFile(fileName);
    testForRuleNetworkAndOnePatient(ruleNetwork, patient,
        expectedTimePhases);
  }


  private void testForRuleNetworkAndOnePatient(
      final RuleNetwork ruleNetwork,
      final GPMPatient guidelinePatient,
      final Map<String, TimePhase> vertexIdToExpectedTimePhase) {
    // Given the ruleNetwork

    // When
    final Map<Vertex, TimePhase> vertexToTimePhase =
        getTimePhaseClassifier(ruleNetwork).classify(guidelinePatient);

    // Then
    assertThat(vertexToTimePhase.size(), is(vertexIdToExpectedTimePhase.size()));
    vertexToTimePhase.forEach((vertex, timePhase) ->
        assertEquals(
            "For patient " + guidelinePatient + ". " +
                "Expected: " + vertexIdToExpectedTimePhase.get(vertex.getId()) + ", " + "but was "
                + timePhase + " (vertex " + vertex.getId() + ").",
            vertexIdToExpectedTimePhase.get(vertex.getId()), timePhase));

  }

  private RuleNetworkTimePhaseClassifier getTimePhaseClassifier(final RuleNetwork ruleNetwork) {
    final DroolsFactsHandler droolsFactsHandler = new DroolsFactsHandler(ruleNetwork);

    return new RuleNetworkTimePhaseClassifier(
        new KieSessionProvider(droolsFactsHandler.getNetworkFacts(), debugRuleRuntimeEventListener),
        droolsFactsHandler);
  }

  private KieSessionProvider getKieSessionProviderForFile(final String fileName) {
    final RuleNetwork ruleNetwork = getRuleNetworkFromFile(fileName);
    final DroolsFactsHandler droolsFactsHandler = new DroolsFactsHandler(ruleNetwork);
    return new KieSessionProvider(droolsFactsHandler.getNetworkFacts(),
        debugRuleRuntimeEventListener);
  }

  private RuleNetwork getRuleNetworkFromFile(final String fileName) {
    final File file = new File(getClass().getResource(fileName).getFile());
    final BpmnModelInstance bpmnModelInstance = Bpmn.readModelFromFile(file);

    return new RuleNetworkCreator(bpmnModelInstance, new RuleNetworkBuilder(), new TaskDetailTexts())
        .create();
  }
}
