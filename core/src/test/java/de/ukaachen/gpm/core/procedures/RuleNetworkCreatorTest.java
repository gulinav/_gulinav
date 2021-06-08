package de.ukaachen.gpm.core.procedures;


import static de.ukaachen.gpm.core.model.patient.Performer.DEVICE;
import static de.ukaachen.gpm.core.model.patient.Performer.HUMAN;
import static de.ukaachen.gpm.core.model.rule_network.VertexType.*;
import static de.ukaachen.gpm.core.util.TestConstants.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isIn;


import de.ukaachen.gpm.core.conditions.Literal;
import de.ukaachen.gpm.core.conditions.True;
import de.ukaachen.gpm.core.model.rule_network.RuleNetwork;
import de.ukaachen.gpm.core.model.rule_network.RuleNetworkBuilder;
import de.ukaachen.gpm.core.model.rule_network.TaskDetailTexts;
import de.ukaachen.gpm.core.model.rule_network.Vertex;
import de.ukaachen.gpm.core.util.Pair;
import java.io.File;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.Test;

public class RuleNetworkCreatorTest {

  @Test
  public void testWcp01SequenceRuleNetworkCreation() {
    final Vertex vA =
        Vertex.withId(A).type(ACTIVITY).performer(DEVICE).description(A).build();
    final Vertex vB =
        Vertex.withId(B).type(ACTIVITY).performer(DEVICE).description(B).build();
    final Vertex vS = Vertex.withId(S).type(START_EVENT).build();
    final Vertex vT = Vertex.withId(T).type(END_EVENT).build();

    testFileAndAssertForExpectedRuleNetwork(
        "patterns/wcp-01-sequence.bpmn",
        new RuleNetworkBuilder()
            .addVertex(vS)
            .addVertex(vA)
            .addVertex(vB)
            .addVertex(vT)
            .addEdge("SequenceFlow_1ad7cst", S, A, True.getInstance())
            .addEdge("SequenceFlow_1u78yem", A, B, True.getInstance())
            .addEdge("SequenceFlow_0mo8pzf", B, T, True.getInstance())
            .build()
    );
  }

  @Test
  public void testWcp02ParallelSplitRuleNetworkCreation() {
    final Vertex vS = Vertex.withId(S).type(START_EVENT).build();
    final Vertex vA = Vertex.withId(A).type(ACTIVITY).description(A).performer(DEVICE).build();
    final Vertex vB = Vertex.withId(B).type(ACTIVITY).description(B).performer(DEVICE).build();
    final Vertex vC = Vertex.withId(C).type(ACTIVITY).description(C).performer(DEVICE).build();
    final Vertex vD = Vertex.withId(D).type(ACTIVITY).description(D).performer(DEVICE).build();
    final Vertex vE = Vertex.withId(E).type(ACTIVITY).description(E).performer(DEVICE).build();
    final Vertex vT = Vertex.withId(T).type(END_EVENT).build();
    final Vertex vGateway01 = Vertex.withId(GATEWAY_01).type(PARALLEL_GATEWAY).build();

    testFileAndAssertForExpectedRuleNetwork(
        "patterns/wcp-02-par-spl.bpmn",
        new RuleNetworkBuilder()
            .addVertices(vS, vA, vGateway01, vB, vC, vD, vE, vT)
            .addEdge("SequenceFlow_02ey87g", S, A)
            .addEdge("SequenceFlow_15gkon6", A, GATEWAY_01)
            .addEdge("SequenceFlow_1yslpjs", GATEWAY_01, B)
            .addEdge("SequenceFlow_09421bn", GATEWAY_01, C)
            .addEdge("SequenceFlow_0b8gux8", B, D)
            .addEdge("SequenceFlow_0qtp90c", C, E)
            .addEdge("SequenceFlow_1p87q2h", D, T)
            .addEdge("SequenceFlow_0s0v0sp", E, T)
            .build()
    );
  }

  @Test
  public void testWcp03SynchronizationRuleNetworkCreation() {
    final Vertex vS = Vertex.withId(S).type(START_EVENT).build();
    final Vertex vA = Vertex.withId(A)
        .type(ACTIVITY)
        .description(A)
        .performer(DEVICE)
        .handledProperty(Pair.create(A, cA))
        .build();
    final Vertex vB = Vertex.withId(B)
        .type(ACTIVITY)
        .description(B)
        .performer(DEVICE)
        .handledProperty(Pair.create(B, cB))
        .build();
    final Vertex vC =
        Vertex.withId(C).type(ACTIVITY).description(C).performer(DEVICE).handledProperty(Pair.create(C, cC)).build();
    final Vertex vT = Vertex.withId(T).type(END_EVENT).build();
    final Vertex vGateway01 = Vertex.withId(GATEWAY_01).type(PARALLEL_GATEWAY).build();
    testFileAndAssertForExpectedRuleNetwork(
        "patterns/wcp-03-synch.bpmn",
        new RuleNetworkBuilder()
            .addVertices(vS, vA, vB, vC, vT, vGateway01)
            .addEdge("SequenceFlow_1uyfv62", S, B)
            .addEdge("SequenceFlow_02ey87g", S, A)
            .addEdge("SequenceFlow_0oyt3mc", A, GATEWAY_01, Literal.of("${A == true}"))
            .addEdge("SequenceFlow_1tcmjzh", A, T, Literal.of("${A == true}").invert())
            .addEdge("SequenceFlow_1fjbz2b", B, GATEWAY_01, Literal.of("${B == true}"))
            .addEdge("SequenceFlow_03b0cag", B, T, Literal.of("${B == true}").invert())
            .addEdge("SequenceFlow_0rr7bmw", GATEWAY_01, C)
            .addEdge("SequenceFlow_1ldwql5", C, T, Literal.of("${C == true}"))
            .build()
    );
  }

  @Test
  public void testWcp04ExChRuleNetworkCreation() {

    final Vertex vS = Vertex.withId(S).type(START_EVENT).build();
    final Vertex vA = Vertex.withId(A)
        .type(ACTIVITY)
        .description(A)
        .performer(DEVICE)
        .handledProperty(Pair.create(A, cA))
        .build();
    final Vertex vB = Vertex.withId(B).type(ACTIVITY).description(B).performer(DEVICE).build();
    final Vertex vC = Vertex.withId(C).type(ACTIVITY).description(C).performer(DEVICE).build();
    final Vertex vT = Vertex.withId(T).type(END_EVENT).build();
    final Vertex vGateway01 = Vertex.withId(GATEWAY_01).type(EXCLUSIVE_GATEWAY).build();

    testFileAndAssertForExpectedRuleNetwork("patterns/wcp-04-ex-ch.bpmn",
        new RuleNetworkBuilder()
            .addVertices(vS, vA, vB, vC, vT, vGateway01)
            .addEdge("SequenceFlow_1uyfv62", S, A)
            .addEdge("SequenceFlow_146pbxk", A, GATEWAY_01)
            .addEdge("SequenceFlow_15gvukz", GATEWAY_01, B, Literal.of("${A == true}"))
            .addEdge("SequenceFlow_0noveqv", GATEWAY_01, C, Literal.of("${A == false}"))
            .addEdge("SequenceFlow_10q2ryi", B, T)
            .addEdge("SequenceFlow_1yre820", C, T)
            .build()
    );
  }

  @Test
  public void testWcp05SimpleMerge() {
    final Vertex vS = Vertex.withId(S).type(START_EVENT).build();
    final Vertex vA = Vertex.withId(A)
        .type(ACTIVITY)
        .description(A)
        .performer(DEVICE)
        .handledProperty(Pair.create(A, cA))
        .build();
    final Vertex vB = Vertex.withId(B)
        .type(ACTIVITY)
        .description(B)
        .performer(DEVICE)
        .handledProperty(Pair.create(B, cB))
        .build();
    final Vertex vC = Vertex.withId(C).type(ACTIVITY).description(C).performer(DEVICE).build();
    final Vertex vT = Vertex.withId(T).type(END_EVENT).build();
    final Vertex vGateway01 = Vertex.withId(GATEWAY_01).type(EXCLUSIVE_GATEWAY).build();

    testFileAndAssertForExpectedRuleNetwork("patterns/wcp-05-simple-m.bpmn",
        new RuleNetworkBuilder()
            .addVertices(vS, vA, vB, vC, vT, vGateway01)
            .addEdge("SequenceFlow_17ssahw", S, A)
            .addEdge("SequenceFlow_11h14zf", S, B)
            .addEdge("SequenceFlow_05zo58s", B, GATEWAY_01, Literal.of("${B == true}"))
            .addEdge("SequenceFlow_122551a", B, T, Literal.of("${B == true}").invert())
            .addEdge("SequenceFlow_0rs6ne4", A, GATEWAY_01, Literal.of("${A == true}"))
            .addEdge("SequenceFlow_16g3ff2", A, T, Literal.of("${A == true}").invert())
            .addEdge("SequenceFlow_0x42yix", GATEWAY_01, C)
            .addEdge("SequenceFlow_08joew9", C, T)
            .build()
    );
  }

  @Test
  public void testThatVerticesAndEdgesAreCreatedCorrectly() {
    final Vertex start = Vertex.withId("start").type(START_EVENT).build();
    final Vertex task =
        Vertex.withId("task").type(ACTIVITY).description("Stelle Diagnose A").performer(DEVICE)
            .build();
    final Vertex end = Vertex.withId("end").type(END_EVENT).build();

    testFileAndAssertForExpectedRuleNetwork("rule-network-creator-simplest.bpmn",
        new RuleNetworkBuilder()
            .addVertices(start, task, end)
            .addEdge("start_task", "start", "task", True.getInstance())
            .addEdge("task_end", "task", "end", True.getInstance())
            .build()
    );
  }

  @Test
  public void testThatParallelGatewaysAreProcessedCorrectly() {
    final Vertex start = Vertex.withId("start").type(START_EVENT).build();
    final Vertex aAndD = Vertex.withId("A-and-D").type(PARALLEL_GATEWAY).build();
    final Vertex bAndD = Vertex.withId("B-and-C").type(PARALLEL_GATEWAY).build();
    final Vertex bAndCAndD = Vertex.withId("B-and-C-and-D").type(PARALLEL_GATEWAY).build();
    final Vertex a = Vertex.withId(A).type(ACTIVITY).description(A)
        .handledProperty(Pair.create(A, "SMITH:A")).performer(DEVICE).build();
    final Vertex b = Vertex.withId(B).type(ACTIVITY).performer(DEVICE).description(B)
        .handledProperty(Pair.create(B, "SMITH:B")).build();
    final Vertex c = Vertex.withId(C).type(ACTIVITY).performer(DEVICE).description(C)
        .handledProperty(Pair.create(C, "SMITH:C")).build();
    final Vertex d = Vertex.withId(D).type(ACTIVITY).performer(DEVICE).description(D)
        .handledProperty(Pair.create(D, "SMITH:D")).build();
    final Vertex end = Vertex.withId("end").type(END_EVENT).build();

    testFileAndAssertForExpectedRuleNetwork("rule-network-creator-parallel.bpmn",
        new RuleNetworkBuilder()
            .addVertices(start, aAndD, bAndCAndD, bAndD, a, b, c, d, end)
            .addEdge("SequenceFlow_0yd874d", start.getId(), aAndD.getId())
            .addEdge("SequenceFlow_0tm21x1", aAndD.getId(), a.getId())
            .addEdge("SequenceFlow_12q79ic", aAndD.getId(), d.getId())
            .addEdge("SequenceFlow_08tvpmn", a.getId(), bAndD.getId(), Literal.of("A == true"))
            .addEdge("SequenceFlow_1kgpv7y", bAndD.getId(), b.getId())
            .addEdge("SequenceFlow_1l5jt36", bAndD.getId(), c.getId())
            .addEdge("SequenceFlow_11pk81q", d.getId(), bAndCAndD.getId(), Literal.of("D == true"))
            .addEdge("SequenceFlow_0o62rm2", b.getId(), bAndCAndD.getId(),
                Literal.of("B == \"Hello\""))
            .addEdge("SequenceFlow_1f28kz7", c.getId(), bAndCAndD.getId(), Literal.of("C < 5.4"))
            .addEdge("SequenceFlow_16h5vvc", bAndCAndD.getId(), end.getId())
            .build()
    );
  }

  @Test
  public void testThatExclusiveGatewaysAreProcessedCorrectly() {
    final Vertex start = Vertex.withId("start").type(START_EVENT).build();
    final Vertex exclBc = Vertex.withId("EXCL_BC").type(EXCLUSIVE_GATEWAY).build();
    final Vertex exclBd = Vertex.withId("EXCL_BD").type(EXCLUSIVE_GATEWAY).build();
    final Vertex a = Vertex.withId(A).type(ACTIVITY).description(A)
        .handledProperty(Pair.create(A, "SMITH:A")).performer(DEVICE).build();
    final Vertex b = Vertex.withId(B).type(ACTIVITY).description(B)
        .handledProperty(Pair.create(B, "SMITH:B")).performer(DEVICE).build();
    final Vertex c = Vertex.withId(C).type(ACTIVITY).description(C)
        .handledProperty(Pair.create(C, "SMITH:C")).performer(DEVICE).build();
    final Vertex d = Vertex.withId(D).type(ACTIVITY).description(D)
        .handledProperty(Pair.create(D, "SMITH:D")).performer(DEVICE).build();
    final Vertex e = Vertex.withId(E).type(ACTIVITY).performer(DEVICE).description(E)
        .handledProperty(Pair.create(E, "SMITH:E")).build();
    final Vertex end1 = Vertex.withId("EndEvent_0tiqv5r").type(END_EVENT).build();
    final Vertex end2 = Vertex.withId("EndEvent_0kn47wi").type(END_EVENT).build();
    final Vertex end3 = Vertex.withId("EndEvent_01mahnt").type(END_EVENT).build();
    final Vertex end4 = Vertex.withId("EndEvent_1yx5bhz").type(END_EVENT).build();
    final Vertex end5 = Vertex.withId("EndEvent_0uqkxsm").type(END_EVENT).build();
    final Vertex end6 = Vertex.withId("end").type(END_EVENT).build();

    testFileAndAssertForExpectedRuleNetwork("rule-network-creator-exclusive.bpmn",
        new RuleNetworkBuilder()
            .addVertices(start, exclBc, exclBd, a, b, c, d, e, end1, end2, end3, end4, end5, end6)
            .addEdge("SequenceFlow_1bv6h1k", start.getId(), a.getId())
            .addEdge("SequenceFlow_144tys5", a.getId(), exclBc.getId(), Literal.of("${A == true}"))
            .addEdge("SequenceFlow_1ekvagb", a.getId(), end1.getId(),
                Literal.of("${A == true}").invert())
            .addEdge("SequenceFlow_1gilvuf", exclBc.getId(), b.getId(),
                Literal.of("${AB == true}"))
            .addEdge("SequenceFlow_01e34m6", exclBc.getId(), c.getId(), Literal.of("${AC == true}"))
            .addEdge("SequenceFlow_0nsl00c", b.getId(), exclBd.getId(), Literal.of("${B == true}"))
            .addEdge("SequenceFlow_1ewvevn", b.getId(), end2.getId(),
                Literal.of("${B == true}").invert())
            .addEdge("SequenceFlow_1ol275t", c.getId(), d.getId(), Literal.of("${C == true}"))
            .addEdge("SequenceFlow_1o4f23t", c.getId(), end3.getId(),
                Literal.of("${C == true}").invert())
            .addEdge("SequenceFlow_1tpzn5n", d.getId(), exclBd.getId(), Literal.of("${D == true}"))
            .addEdge("SequenceFlow_0wb2ukd", d.getId(), end4.getId(),
                Literal.of("${D == true}").invert())
            .addEdge("SequenceFlow_1d9hyn8", exclBd.getId(), e.getId())
            .addEdge("SequenceFlow_107qufl", e.getId(), end6.getId(), Literal.of("${E == true}"))
            .addEdge("SequenceFlow_12qc054", e.getId(), end5.getId(),
                Literal.of("${E == true}").invert())
            .build()
    );
  }

  @Test
  public void testThatTheGraphCreatorCanHandleMultipleSimilarGatewaysInAChain() {
    final Vertex s = Vertex.withId(S).type(START_EVENT).build();
    final Vertex gw1 = Vertex.withId(GATEWAY_01).type(EXCLUSIVE_GATEWAY).build();
    final Vertex gw2 = Vertex.withId(GATEWAY_02).type(EXCLUSIVE_GATEWAY).build();
    final Vertex a = Vertex.withId(A).type(ACTIVITY).description(A).performer(DEVICE).build();
    final Vertex t = Vertex.withId(T).type(END_EVENT).build();

    testFileAndAssertForExpectedRuleNetwork("rule-network-creator-chained-gateways.bpmn",
        new RuleNetworkBuilder()
            .addVertices(s, gw1, gw2, a, t)
            .addEdge("SequenceFlow_0pbdwfe", s.getId(), gw1.getId())
            .addEdge("SequenceFlow_0wy8bct", gw1.getId(), gw2.getId())
            .addEdge("SequenceFlow_0f4qf8y", gw2.getId(), a.getId())
            .addEdge("SequenceFlow_1glnu1n", a.getId(), t.getId())
            .build());
  }

  @Test
  public void testThatTheCreatorConsidersMultipleEdgesBetweenTheSameVertices() {
    final Vertex s = Vertex.withId(S).type(START_EVENT).build();
    final Vertex a = Vertex.withId(A)
        .type(ACTIVITY)
        .performer(DEVICE)
        .handledProperty(Pair.create(A, cA))
        .description(A)
        .build();
    final Vertex b = Vertex.withId(B)
        .type(ACTIVITY)
        .performer(DEVICE)
        .handledProperty(Pair.create(B, cB))
        .description(B)
        .build();
    final Vertex x = Vertex.withId(X).type(END_EVENT).build();
    final Vertex y = Vertex.withId(Y).type(END_EVENT).build();

    testFileAndAssertForExpectedRuleNetwork("rule-network-creator-edges-between-same-vertices.bpmn",
        new RuleNetworkBuilder()
            .addVertices(s, a, b, x, y)
            .addEdge("SequenceFlow_1rx2rkm", s.getId(), b.getId())
            .addEdge("SequenceFlow_0p4zaav", b.getId(), a.getId(), Literal.of("${B < 5}"))
            .addEdge("SequenceFlow_03t5wwn", b.getId(), a.getId(), Literal.of("${B >= 10}"))
            .addEdge("SequenceFlow_1wt0e2s", a.getId(), x.getId(), Literal.of("${A == true}"))
            .addEdge("SequenceFlow_1w69297", a.getId(), y.getId(), Literal.of("${A == false}"))
            .build()
    );
  }

  @Test
  public void testWhenGivenDefaultTask_AVertexWithPerformerHumanIsCreated() {
    final Vertex humanVertex =
        Vertex.withId(A).description(A).type(ACTIVITY).performer(HUMAN).build();
    final Vertex deviceVertex =
        Vertex.withId(B).description(B).type(ACTIVITY).performer(DEVICE).build();

    testFileAndAssertForExpectedRuleNetwork("rule-network-creator-human-task.bpmn",
        new RuleNetworkBuilder()
            .addVertices(humanVertex, deviceVertex)
            .build()
    );
  }

  private void testFileAndAssertForExpectedRuleNetwork(
      final String fileName,
      final RuleNetwork expectedRuleNetwork) {
    final BpmnModelInstance model = getModelFromFile(fileName);
    final RuleNetwork actualRuleNetwork =
        new RuleNetworkCreator(model, new RuleNetworkBuilder(), new TaskDetailTexts()).create();
    assertThat(actualRuleNetwork.getVertices(),
        hasItems(expectedRuleNetwork.getVertices().toArray(new Vertex[0])));

    assertThat(actualRuleNetwork.getEdges().size(),
        is(equalTo(expectedRuleNetwork.getEdges().size())));

    actualRuleNetwork.getEdges().forEach(actualEdge -> assertThat(actualEdge,
        isIn(expectedRuleNetwork.getEdges())));
  }

  private BpmnModelInstance getModelFromFile(final String fileName) {
    final File file = new File(getClass().getResource(fileName).getFile());
    return Bpmn.readModelFromFile(file);
  }
}
