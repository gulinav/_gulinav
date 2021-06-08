package de.ukaachen.gpm.core.model.rule_network;

import static de.ukaachen.gpm.core.model.patient.Performer.HUMAN;
import static de.ukaachen.gpm.core.util.TestConstants.*;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.core.IsEqual.equalTo;

import de.ukaachen.gpm.core.conditions.Literal;
import de.ukaachen.gpm.core.conditions.True;
import de.ukaachen.gpm.core.util.Pair;
import org.junit.Before;
import org.junit.Test;

public class RuleNetworkBuilderTest {

  private RuleNetworkBuilder cut;

  @Before
  public void setUp() {
    cut = new RuleNetworkBuilder();
  }

  @Test
  public void testEmptyNetwork() {
    // Given
    // Initialized CUT as is

    // When
    final RuleNetwork actual = cut.build();

    // Then
    assertThat(actual.getVertices(), is(empty()));
    assertThat(actual.getEdges(), is(empty()));
  }

  @Test
  public void testAddStartVertex() {
    // When
    final Vertex startVertex =
        Vertex.withId(A).type(VertexType.START_EVENT).performer(HUMAN).build();
    final RuleNetwork actual = cut
        .addVertex(startVertex).build();

    // Then
    assertThat(actual.getVertices(), contains(startVertex));
    assertThat(actual.getEdges(), is(empty()));
  }

  @Test
  public void testAddExclusiveGateway() {
    // When
    final RuleNetwork actual = cut.addExclusiveGateway(A).build();
    final Vertex expectedVertex = Vertex.withId(A).type(VertexType.EXCLUSIVE_GATEWAY).build();

    // Then
    assertThat(actual.getVertices(), contains(expectedVertex));
    assertThat(actual.getEdges(), is(empty()));
  }

  @Test
  public void testAddVertex() {
    // When
    final Vertex vertex = Vertex.withId(B).type(VertexType.PARALLEL_GATEWAY).build();
    final RuleNetwork actual = cut.addVertex(vertex).build();

    // Then
    assertThat(actual.getVertices(), contains(vertex));
    assertThat(actual.getEdges(), is(empty()));
  }

  @Test
  public void testAddHandledPropertyToVertex() {
    // When
    final Vertex vertex =
        Vertex.withId(C).type(VertexType.ACTIVITY).handledProperty(Pair.create(A, cA)).performer(HUMAN).build();

    final RuleNetwork actual = cut
        .addVertex(vertex)
        .build();

    // When
    assertThat(actual.getVertices(), contains(vertex));
    assertThat(actual.getEdges(), is(empty()));
    assertThat(vertex.getHandledProperty(), is(equalTo(Pair.create(A, cA))));
  }

  @Test
  public void testAddEdge1() {
    // When
    final Vertex a = Vertex.withId(A).type(VertexType.ACTIVITY).performer(HUMAN).build();
    final Vertex b = Vertex.withId(B).type(VertexType.ACTIVITY).performer(HUMAN).build();
    final RuleNetwork actual = cut
        .addVertex(a)
        .addVertex(b)
        .addEdge(X, A, B, null)
        .build();

    // Then
    assertThat(actual.getVertices(), containsInAnyOrder(a, b));
    assertThat(actual.getEdges(), containsInAnyOrder(new Edge(X, a, b, True.getInstance())));
  }

  @Test
  public void testAddEdge2() {
    // When
    final Vertex a = Vertex.withId(A).type(VertexType.ACTIVITY).performer(HUMAN).build();
    final Vertex b = Vertex.withId(B).type(VertexType.ACTIVITY).performer(HUMAN).build();
    final RuleNetwork actual = cut
        .addVertex(a)
        .addVertex(b)
        .addEdge(X, A, B, Literal.of(Y))
        .build();

    // Then
    assertThat(actual.getVertices(), containsInAnyOrder(a, b));
    assertThat(actual.getEdges(), containsInAnyOrder(new Edge(X, a, b, Literal.of(Y))));
  }

  @Test
  public void testReplaceConditionForEdgeWithId() {
    // When
    final Vertex a = Vertex.withId(A).type(VertexType.ACTIVITY).performer(HUMAN).build();
    final Vertex b = Vertex.withId(B).type(VertexType.ACTIVITY).performer(HUMAN).build();
    final RuleNetwork actual = cut
        .addVertex(a)
        .addVertex(b)
        .addEdge(X, A, B, Literal.of(Y))
        .replaceConditionForEdgeWithId(X, Literal.of(Z))
        .build();

    // Then
    assertThat(actual.getVertices(), containsInAnyOrder(a, b));
    assertThat(actual.getEdges(), containsInAnyOrder(new Edge(X, a, b, Literal.of(Z))));
  }

  @Test
  public void testRemoveEdge() {
    // When
    final Vertex a = Vertex.withId(A).type(VertexType.ACTIVITY).performer(HUMAN).build();
    final Vertex b = Vertex.withId(B).type(VertexType.ACTIVITY).performer(HUMAN).build();
    final RuleNetwork actual = cut
        .addVertex(a)
        .addVertex(b)
        .addEdge(X, A, B, Literal.of(Y))
        .removeEdge(X)
        .build();

    // Then
    assertThat(actual.getVertices(), containsInAnyOrder(a, b));
    assertThat(actual.getEdges(), is(empty()));
  }

  @Test
  public void testRemoveVertex() {
    // When
    final Vertex vertex = Vertex.withId(A).type(VertexType.ACTIVITY).performer(HUMAN).build();
    final RuleNetwork actual = cut
        .addVertex(vertex)
        .removeVertex(A)
        .build();

    // Then
    assertThat(actual.getVertices(), is(empty()));
    assertThat(actual.getEdges(), is(empty()));
  }

  @Test
  public void testGetVertexIds() {
    // When
    final Vertex a = Vertex.withId(A).type(VertexType.ACTIVITY).performer(HUMAN).build();
    final Vertex b = Vertex.withId(B).type(VertexType.ACTIVITY).performer(HUMAN).build();
    final RuleNetworkBuilder actual = cut
        .addVertex(a)
        .addVertex(b);

    // Then
    assertThat(actual.getVertexIds(), contains(A, B));
  }

  @Test
  public void testContainsEdgeWithId() {
    // When
    final Vertex a = Vertex.withId(A).type(VertexType.ACTIVITY).performer(HUMAN).build();
    final Vertex b = Vertex.withId(B).type(VertexType.ACTIVITY).performer(HUMAN).build();
    final RuleNetworkBuilder actual = cut
        .addVertex(a)
        .addVertex(b)
        .addEdge(X, A, B, null);

    // Then
    assertThat(actual.containsEdgeWithId(X), is(true));
    assertThat(actual.containsEdgeWithId(Y), is(false));
  }

  @Test
  public void testGetConditionForEdge() {
    // When
    final Vertex a = Vertex.withId(A).type(VertexType.ACTIVITY).performer(HUMAN).build();
    final Vertex b = Vertex.withId(B).type(VertexType.ACTIVITY).performer(HUMAN).build();
    final RuleNetworkBuilder actual = cut
        .addVertex(a)
        .addVertex(b)
        .addEdge(X, A, B, null)
        .addEdge(Y, B, A, Literal.of(cA));

    assertThat(actual.getConditionForEdge(A, B), is(True.getInstance()));
    assertThat(actual.getConditionForEdge(B, A), is(Literal.of(cA)));
  }

  @Test
  public void testGetConditionForEdgeWithId() {
    // When
    final Vertex a = Vertex.withId(A).type(VertexType.ACTIVITY).performer(HUMAN).build();
    final Vertex b = Vertex.withId(B).type(VertexType.ACTIVITY).performer(HUMAN).build();
    final RuleNetworkBuilder actual = cut
        .addVertex(a)
        .addVertex(b)
        .addEdge(X, A, B, null)
        .addEdge(Y, B, A, Literal.of(cA));

    assertThat(actual.getConditionForEdgeWithId(X), is(True.getInstance()));
    assertThat(actual.getConditionForEdgeWithId(Y), is(Literal.of(cA)));
  }
}
