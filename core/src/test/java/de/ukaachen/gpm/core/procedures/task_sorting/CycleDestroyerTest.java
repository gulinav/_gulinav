package de.ukaachen.gpm.core.procedures.task_sorting;

import static de.ukaachen.gpm.core.util.TestConstants.*;

import static de.ukaachen.gpm.core.util.VertexUtil.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsEqual.equalTo;

import de.ukaachen.gpm.core.conditions.Literal;
import de.ukaachen.gpm.core.model.rule_network.Edge;
import de.ukaachen.gpm.core.model.rule_network.RuleNetwork;
import de.ukaachen.gpm.core.model.rule_network.RuleNetworkBuilder;
import de.ukaachen.gpm.core.model.rule_network.TaskDetailTexts;
import de.ukaachen.gpm.core.model.task_sorting.EdgeColoring;
import de.ukaachen.gpm.core.model.task_sorting.RuleNetworkManipulatorReturnValue;
import de.ukaachen.gpm.core.procedures.RuleNetworkCreator;
import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class CycleDestroyerTest {

  private RuleNetworkManipulator<EdgeColoring> cycleDestroyer;

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  private RuleNetwork ruleNetwork;

  @Before
  public void setUp() {
    cycleDestroyer = new CycleDestroyer();
  }

  @Test
  public void testWhenGivenNull_IllegalArgumentExceptionIsThrown() {
    // Given
    expectedException.expect(IllegalArgumentException.class);

    // When
    cycleDestroyer.manipulate(null);

    // Then should throw exception
  }

  @Test
  public void testWhenGivenNonCyclicGraph_allEdgesAreRetained() {
    // Given
    this.ruleNetwork = new RuleNetworkBuilder()
        .addVertices(start(A), v(B), v(C), v(D))
        .addEdge(X, A, B)
        .addEdge(Y, B, C)
        .addEdge(Z, B, D, Literal.of("Whatever"))
        .build();

    // When
    final RuleNetworkManipulatorReturnValue<EdgeColoring> actual = cycleDestroyer
        .manipulate(this.ruleNetwork);

    // Then
    assertThat(actual.getManipulatedNetwork().getVertices(),
        is(equalTo(this.ruleNetwork.getVertices())));
    assertThat(actual.getManipulatedNetwork().getEdges(),
        is(equalTo(this.ruleNetwork.getEdges())));
    assertThat(actual.getManipulationInfo().getNotRemovedCycleEdges(), is(empty()));
    assertThat(actual.getManipulationInfo().getRemovedEdges(), is(empty()));
    assertThat(actual.getManipulationInfo().getNonCycleEdges(),
        is(equalTo(this.ruleNetwork.getEdges())));
  }

  @Test
  public void testWhenGivenSimpleCycle_theCorrectEdgeIsRemoved() {
    // Given
    this.ruleNetwork = new RuleNetworkBuilder()
        .addVertices(start(A), v(B), v(C), v(D), v(E))
        .addEdge(V, A, B)
        .addEdge(W, B, C)
        .addEdge(X, C, D)
        .addEdge(Y, D, E)
        .addEdge(Z, E, B)
        .build();
    final RuleNetwork expectedRuleNetwork = new RuleNetworkBuilder()
        .addVertices(start(A), v(B), v(C), v(D), v(E))
        .addEdge(V, A, B)
        .addEdge(W, B, C)
        .addEdge(X, C, D)
        .addEdge(Y, D, E)
        .build();

    // When
    final RuleNetworkManipulatorReturnValue<EdgeColoring> actual = cycleDestroyer
        .manipulate(this.ruleNetwork);

    // Then
    assertThat(actual.getManipulatedNetwork().getVertices(),
        is(equalTo(expectedRuleNetwork.getVertices())));
    assertThat(actual.getManipulatedNetwork().getEdges(),
        is(equalTo(expectedRuleNetwork.getEdges())));
    assertThat(actual.getManipulationInfo().getRemovedEdges(),
        is(equalTo(selectEdges(Z))));
    assertThat(actual.getManipulationInfo().getNonCycleEdges(),
        is(equalTo(selectEdges(V))));
    assertThat(actual.getManipulationInfo().getNotRemovedCycleEdges(),
        is(equalTo(selectEdges(W, X, Y))));
  }

  @Test
  public void testWhenGivenGraphWithNonchordialCycle_theCorrectEdgeIsRemoved() {
    // Given
    this.ruleNetwork = new RuleNetworkBuilder()
        .addVertices(start(A), v(B), v(C), v(D), v(E), v(F), end(G))
        .addEdge(V, A, B)
        .addEdge(W, B, C)
        .addEdge(X, C, D)
        .addEdge(Y, D, E)
        .addEdge(Z, E, B)
        .addEdge(P, E, F)
        .addEdge(Q, B, F)
        .addEdge(R, C, F)
        .addEdge(S, A, F)
        .build();
    final RuleNetwork expectedRuleNetwork = new RuleNetworkBuilder()
        .addVertices(start(A), v(B), v(C), v(D), v(E), v(F), end(G))
        .addEdge(V, A, B)
        .addEdge(W, B, C)
        .addEdge(X, C, D)
        .addEdge(Y, D, E)
        .addEdge(P, E, F)
        .addEdge(Q, B, F)
        .addEdge(R, C, F)
        .addEdge(S, A, F)
        .build();

    // When
    final RuleNetworkManipulatorReturnValue<EdgeColoring> actual = cycleDestroyer
        .manipulate(this.ruleNetwork);

    // Then
    assertThat(actual.getManipulatedNetwork().getVertices(),
        is(equalTo(expectedRuleNetwork.getVertices())));
    assertThat(actual.getManipulatedNetwork().getEdges(),
        is(equalTo(expectedRuleNetwork.getEdges())));
    assertThat(actual.getManipulationInfo().getRemovedEdges(),
        is(equalTo(selectEdges(Z))));
    assertThat(actual.getManipulationInfo().getNonCycleEdges(),
        is(equalTo(selectEdges(V, P, S, Q, R))));
    assertThat(actual.getManipulationInfo().getNotRemovedCycleEdges(),
        is(equalTo(selectEdges(W, X, Y))));
  }

  @Test
  public void testWhenGivenGraphWithMultipleInterleavingChordialCycles_theCorrectEdgesAreRemoved() {
    // Given
    this.ruleNetwork = new RuleNetworkBuilder()
        .addVertices(start(S), v(A), v(B), v(C), v(D), v(E), v(F), v(G), v(H), end(T))
        .addEdge(K, S, A)
        .addEdge(L, A, B)
        .addEdge(M, B, C)
        .addEdge(N, C, D)
        .addEdge(O, D, E)
        .addEdge(P, E, A)
        .addEdge(Q, C, G)
        .addEdge(R, D, F)
        .addEdge(S, F, H)
        .addEdge(T, G, H)
        .addEdge(U, H, T)
        .addEdge(V, B, E)
        .addEdge(W, E, B)
        .addEdge(X, A, C)
        .addEdge(Y, D, A)
        .addEdge("HE", H, E)
        .addEdge("HC", H, C)
        .addEdge("GF", G, F)
        .build();

    final RuleNetwork expectedNetwork = new RuleNetworkBuilder()
        .addVertices(start(S), v(A), v(B), v(C), v(D), v(E), v(F), v(G), v(H), end(T))
        .addEdge(K, S, A)
        .addEdge(L, A, B)
        .addEdge(M, B, C)
        .addEdge(N, C, D)
        .addEdge(O, D, E)
        .addEdge(Q, C, G)
        .addEdge(R, D, F)
        .addEdge(S, F, H)
        .addEdge(T, G, H)
        .addEdge(U, H, T)
        .addEdge(V, B, E)
        .addEdge(X, A, C)
        .addEdge("HE", H, E)
        .addEdge("GF", G, F)
        .build();

    // When
    final RuleNetworkManipulatorReturnValue<EdgeColoring> actual = cycleDestroyer
        .manipulate(this.ruleNetwork);

    // Then
    assertThat(actual.getManipulatedNetwork().getVertices(),
        is(equalTo(expectedNetwork.getVertices())));
    assertThat(actual.getManipulatedNetwork().getEdges(),
        is(equalTo(expectedNetwork.getEdges())));
    assertThat(actual.getManipulationInfo().getRemovedEdges(),
        is(equalTo(selectEdges(P, Y, W, "HC"))));
    assertThat(actual.getManipulationInfo().getNonCycleEdges(),
        is(equalTo(selectEdges(K, U))));
    assertThat(actual.getManipulationInfo().getNotRemovedCycleEdges(),
        is(equalTo(selectEdges(L, M, N, O, V, X, Q, R, S, T, "GF", "HE"))));
  }

  @Test
  public void testWhen2EntryPointsIntoCircle_theEdgeToTheEarlierOneIsRemoved() {
    // Given
    this.ruleNetwork = new RuleNetworkBuilder()
        .addVertices(start(S), v(A), v(B), v(C), v(D), end(T))
        .addEdge(P, S, A)
        .addEdge(Q, S, B)
        .addEdge(R, B, C)
        .addEdge(S, A, D)
        .addEdge(T, C, A)
        .addEdge(U, D, C)
        .addEdge(V, D, T)
        .build();

    final RuleNetwork expectedNetwork = new RuleNetworkBuilder()
        .addVertices(start(S), v(A), v(B), v(C), v(D), end(T))
        .addEdge(P, S, A)
        .addEdge(Q, S, B)
        .addEdge(R, B, C)
        .addEdge(S, A, D)
        .addEdge(U, D, C)
        .addEdge(V, D, T)
        .build();

    // When
    final RuleNetworkManipulatorReturnValue<EdgeColoring> actual = cycleDestroyer
        .manipulate(this.ruleNetwork);

    // Then
    assertThat(actual.getManipulatedNetwork(), is(equalTo(expectedNetwork)));
    assertThat(actual.getManipulationInfo().getRemovedEdges(), is(equalTo(selectEdges(T))));
    assertThat(actual.getManipulationInfo().getNonCycleEdges(),
        is(equalTo(selectEdges(P, Q, R, V))));
    assertThat(actual.getManipulationInfo().getNotRemovedCycleEdges(),
        is(equalTo(selectEdges(S, U))));
  }

  @Test
  public void testComplexCycleExample() {
    // Given
    this.ruleNetwork = getRuleNetworkFromFile("time-classifier-cycle-complex.bpmn");

    // When
    final RuleNetworkManipulatorReturnValue<EdgeColoring> actual = cycleDestroyer
        .manipulate(this.ruleNetwork);

    // Then
    assertThat(actual.getManipulationInfo().getRemovedEdges().size(), is(3));
    assertThat(actual.getManipulationInfo().getRemovedEdges(),
        is(equalTo(
            selectEdges("SequenceFlow_1a0wngp", "SequenceFlow_1sdcmg5", "SequenceFlow_1j9vd1k"))));
  }

  private RuleNetwork getRuleNetworkFromFile(final String fileName) {
    final File file = new File(getClass().getResource("../" + fileName).getFile());
    final BpmnModelInstance bpmnModelInstance = Bpmn.readModelFromFile(file);

    return new RuleNetworkCreator(bpmnModelInstance, new RuleNetworkBuilder(), new TaskDetailTexts())
        .create();
  }

  private Set<Edge> selectEdges(final String... ids) {
    final Set<String> idsSet = new HashSet<>(Arrays.asList(ids));
    return ruleNetwork.getEdges().stream().filter(e -> idsSet.contains(e.getId()))
        .collect(Collectors.toSet());
  }
}
