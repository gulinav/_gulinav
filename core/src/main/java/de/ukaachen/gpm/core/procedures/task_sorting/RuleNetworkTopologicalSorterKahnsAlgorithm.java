package de.ukaachen.gpm.core.procedures.task_sorting;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import de.ukaachen.gpm.core.exceptions.CyclicGraphException;
import de.ukaachen.gpm.core.model.rule_network.Edge;
import de.ukaachen.gpm.core.model.rule_network.RuleNetwork;
import de.ukaachen.gpm.core.model.rule_network.SortedItems;
import de.ukaachen.gpm.core.model.rule_network.Vertex;
import de.ukaachen.gpm.core.model.rule_network.VertexType;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class RuleNetworkTopologicalSorterKahnsAlgorithm implements TopologicalSorter {

  protected final RuleNetwork ruleNetwork;
  protected final Multimap<Vertex, Edge> vertexToOutgoingEdges;
  protected final Multimap<Vertex, Edge> vertexToIncomingEdges;

  public RuleNetworkTopologicalSorterKahnsAlgorithm(final RuleNetwork ruleNetwork) {
    this.ruleNetwork = ruleNetwork;
    this.vertexToOutgoingEdges = computeVertexToOutgoingEdges();
    this.vertexToIncomingEdges = computeVertexToIncomingEdges();
  }

  @Override
  public SortedItems sort() {
    // Adaption of Kahn's algorithm: https://en.wikipedia.org/wiki/Topological_sorting
    final List<String> L = new ArrayList<>(ruleNetwork.getVertices().size());
    final Deque<Vertex> S = initS();
    final Set<Edge> removedEdges = new HashSet<>();

    final HashMap<Vertex, Integer> vertexToLength =
        computeMaximumRemainingActivityPathLengthFromEachVertex();

    final Comparator<Edge> edgeComparator =
        Comparator.comparingInt(e -> -vertexToLength.get(e.getTarget()));

    while (!S.isEmpty()) {
      final Vertex n = S.pop();
      L.add(n.getId());
      final List<Edge> sortedOutgoingEdges = vertexToOutgoingEdges.get(n).stream()
          .sorted(edgeComparator)
          .collect(Collectors.toList());

      for (final Edge edge : sortedOutgoingEdges) {
        removedEdges.add(edge);
        if (hasNoOtherIncomingEdge(edge.getTarget(), removedEdges)) {
          S.push(edge.getTarget());
        }
      }
    }
    if (removedEdges.size() != ruleNetwork.getEdges().size()) {
      throw new CyclicGraphException();
    } else {
      return new SortedItems(L);
    }
  }

  private HashMap<Vertex, Integer> computeMaximumRemainingActivityPathLengthFromEachVertex() {
    // Initialize data structures
    final HashMap<Vertex, Integer> result = new HashMap<>();
    final Deque<Edge> stack = new ArrayDeque<>();
    final Set<Vertex> visitedVertices = new HashSet<>();

    // Initialize data
    ruleNetwork.getVertices().stream()
        .filter(vertex -> vertexToOutgoingEdges.get(vertex).size() == 0)
        .map(vertex -> ruleNetwork.getEdges().stream()
            .filter(edge -> edge.getTarget().equals(vertex))
            .collect(Collectors.toSet()))
        .flatMap(Set::stream)
        .forEach(stack::push);

    ruleNetwork.getVertices().stream()
        .filter(vertex -> vertexToOutgoingEdges.get(vertex).size() == 0)
        .forEach(visitedVertices::add);

    stack.forEach(endEvent -> result.put(endEvent.getTarget(), 0));

    while (!stack.isEmpty()) {
      final Edge currentE = stack.pop();
      final int storedLength = result.getOrDefault(currentE.getSource(), -Integer.MAX_VALUE);
      final int additionalLength = currentE.getSource().getType() == VertexType.ACTIVITY ? 1 : 0;
      final int potentialNewLength = result.get(currentE.getTarget()) + additionalLength;
      final int newLength = Math.max(storedLength, potentialNewLength);
      result.put(currentE.getSource(), newLength);

      if (!visitedVertices.contains(currentE.getSource())) {
        visitedVertices.add(currentE.getSource());
        vertexToIncomingEdges.get(currentE.getSource()).forEach(stack::push);
      }
    }

    return result;
  }


  private boolean hasNoOtherIncomingEdge(final Vertex vertex, final Set<Edge> removedEdges) {
    final Collection<Edge> incomingEdges = vertexToIncomingEdges.get(vertex);
    return removedEdges.containsAll(incomingEdges);
  }

  private Multimap<Vertex, Edge> computeVertexToOutgoingEdges() {
    final Multimap<Vertex, Edge> result = HashMultimap.create();
    ruleNetwork.getEdges().forEach(edge -> result.put(edge.getSource(), edge));
    return result;
  }

  private Multimap<Vertex, Edge> computeVertexToIncomingEdges() {
    final Multimap<Vertex, Edge> result = HashMultimap.create();
    ruleNetwork.getEdges().forEach(edge -> result.put(edge.getTarget(), edge));
    return result;
  }

  private Deque<Vertex> initS() {
    return ruleNetwork
        .getVertices()
        .stream()
        .filter(vertex -> vertex.getType() == VertexType.START_EVENT)
        .collect(ArrayDeque::new, ArrayDeque::add, ArrayDeque::addAll);
  }
}
