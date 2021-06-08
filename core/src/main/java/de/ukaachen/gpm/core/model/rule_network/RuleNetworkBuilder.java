package de.ukaachen.gpm.core.model.rule_network;

import com.google.common.collect.ImmutableSet;

import de.ukaachen.gpm.core.conditions.Condition;
import de.ukaachen.gpm.core.conditions.True;
import java.util.Arrays;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.TreeMap;
import java.util.TreeSet;
import lombok.Getter;
import org.springframework.stereotype.Component;

// Builder often have unused return values but they might be used in the future
@SuppressWarnings("UnusedReturnValue")
@Component
public class RuleNetworkBuilder {

  @Getter
  private final NavigableMap<String, Vertex> vertices;

  @Getter
  private final NavigableMap<String, Edge> edges;

  private static final IllegalArgumentException ILLEGAL_ARGUMENT_EXCEPTION =
      new IllegalArgumentException("At least on of the given ids does not exist.");

  public RuleNetworkBuilder() {
    this.vertices = new TreeMap<>();
    this.edges = new TreeMap<>();
  }

  public RuleNetwork build() {
    return new RuleNetwork(ImmutableSet.copyOf(edges.values()),
        ImmutableSet.copyOf(vertices.values()));
  }

  public RuleNetworkBuilder addExclusiveGateway(final String id) {
    final Vertex vertex = Vertex.withId(id).type(VertexType.EXCLUSIVE_GATEWAY).build();
    this.vertices.put(vertex.getId(), vertex);
    return this;
  }

  public RuleNetworkBuilder addVertex(final Vertex vertex) {
    vertices.put(vertex.getId(), vertex);
    return this;
  }

  public RuleNetworkBuilder addVertices(final Vertex... vertices) {
    Arrays.stream(vertices).forEach(this::addVertex);
    return this;
  }

  public RuleNetworkBuilder addEdge(final String id, final String from, final String to,
      final Condition condition) {
    final Vertex fromVertex = vertices.get(from);
    final Vertex toVertex = vertices.get(to);
    if (fromVertex == null || toVertex == null) {
      throw ILLEGAL_ARGUMENT_EXCEPTION;
    }

    final Condition theCondition = condition != null ? condition : True.getInstance();

    final Edge edge = new Edge(id, fromVertex, toVertex, theCondition);
    edges.put(edge.getId(), edge);
    return this;
  }

  public RuleNetworkBuilder addEdge(final String id, final String from, final String to) {
    return this.addEdge(id, from, to, True.getInstance());
  }

  public RuleNetworkBuilder addEdge(final Edge edge) {
    if (edge == null) {
      throw ILLEGAL_ARGUMENT_EXCEPTION;
    }

    edges.put(edge.getId(), edge);
    return this;
  }

  public RuleNetworkBuilder replaceConditionForEdgeWithId(final String id,
      final Condition alternativeCondition) {
    final Edge edge = edges.get(id);
    if (edge == null) {
      throw ILLEGAL_ARGUMENT_EXCEPTION;
    }

    final Edge newEdge = new Edge(edge.getId(), edge.getSource(), edge.getTarget(),
        alternativeCondition);
    edges.remove(id);
    edges.put(id, newEdge);

    return this;
  }

  public RuleNetworkBuilder removeEdge(final String edgeId) {
    edges.remove(edgeId);
    return this;
  }

  public RuleNetworkBuilder removeVertex(final String vertexId) {
    vertices.remove(vertexId);
    return this;
  }

  public NavigableSet<String> getVertexIds() {
    return new TreeSet<>(vertices.keySet());
  }

  public boolean containsEdgeWithId(final String edgeId) {
    return edges.keySet().contains(edgeId);
  }

  public Condition getConditionForEdge(final String from, final String to) {
    return edges.values().stream()
        .filter(edge -> edge.getSource().getId().equals(from)
            && edge.getTarget().getId().equals(to))
        .findAny()
        .orElseThrow(() -> ILLEGAL_ARGUMENT_EXCEPTION)
        .getCondition();
  }

  public Condition getConditionForEdgeWithId(final String id) {
    final Edge edge = edges.get(id);
    if (edge == null) {
      throw ILLEGAL_ARGUMENT_EXCEPTION;
    }

    return edge.getCondition();
  }

  public Vertex getVertex(final String id) {
    return vertices.get(id);
  }

  public Edge getEdge(final String id) {
    return edges.get(id);
  }
}
