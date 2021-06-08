package de.ukaachen.gpm.core.procedures.task_sorting;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.SortedSetMultimap;
import de.ukaachen.gpm.core.exceptions.CyclicGraphException;
import de.ukaachen.gpm.core.model.rule_network.Edge;
import de.ukaachen.gpm.core.model.rule_network.RuleNetwork;
import de.ukaachen.gpm.core.model.rule_network.SortedItems;
import de.ukaachen.gpm.core.model.rule_network.Vertex;
import de.ukaachen.gpm.core.model.rule_network.VertexType;
import de.ukaachen.gpm.core.util.Pair;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

/**
 * This sorter sorts the guidelineItems in a way that the following properties are satisfied:
 * 1. The order is a topological order
 * 2. Whenever the order does not matter for the topological sorting, the vertex with the shortest
 *    remaining longest possible path is chosen. Only Activity-vertices count for the length of
 *    the path
 *
 * // TODO This class is super confusing and not performing well. While the performance is not an
 * // issue, since we only to it for small networks and only during initialization. The terrible
 * // readability will be a maintenance problem.
 * // Maybe do not even refactor, but throw away instead if something shall be changed.
 * // Maybe just do a BFS (so things close in the diagram are close to each other) and then
 * // Do something to fix topological order afterwards..
 */
public class GPMSorter implements TopologicalSorter {

  private final RuleNetwork ruleNetwork;
  private final Multimap<Vertex, Vertex> vToChildren;
  private final Multimap<Vertex, Vertex> vToParents;
  private final Multimap<Vertex, Edge> vToIncoming;
  private final Multimap<Vertex, Vertex> vToEarlierVs;
  private final SortedSetMultimap<Vertex, Pair<Vertex, Integer>> sortedAdjacency;

  public GPMSorter(final RuleNetwork ruleNetwork) {
    this.ruleNetwork = ruleNetwork;
    this.vToChildren = MultimapBuilder.hashKeys().hashSetValues().build();
    this.vToParents = MultimapBuilder.hashKeys().hashSetValues().build();
    this.vToIncoming = MultimapBuilder.hashKeys().hashSetValues().build();
    ruleNetwork.getEdges().forEach(edge -> vToChildren.put(edge.getSource(), edge.getTarget()));
    ruleNetwork.getEdges().forEach(edge -> vToParents.put(edge.getTarget(), edge.getSource()));
    ruleNetwork.getEdges().forEach(edge -> vToIncoming.put(edge.getTarget(), edge));

    cycleCheck();

    // Some computations for data structures later used in the algorithm
    this.vToEarlierVs = computeEarlierVerticesPerVertex();
    final HashMap<Vertex, Integer> vToPathLength = computeMaximumRemainingActivityPathLengthFromEachVertex();

    // Shortest path first. If path is equal size, smaller Vertex first.
    final Comparator<Pair<Vertex, Integer>> lengthComparator =
        Comparator.comparingInt((ToIntFunction<Pair<Vertex, Integer>>) Pair::s)
            .thenComparing(Pair::f)
            .reversed();

    this.sortedAdjacency =
        MultimapBuilder.hashKeys().treeSetValues(lengthComparator).build();
    for (final Vertex v : vToChildren.keySet()) {
      final Collection<Vertex> children = vToChildren.get(v);
      for (final Vertex child : children) {
        sortedAdjacency.put(v, Pair.create(child, vToPathLength.get(child)));
      }
    }
  }

  @Override
  public SortedItems sort() {
    // 02. Initialize dataStructures for recursive traversion of graph
    final List<Vertex> sortedVertices = new ArrayList<>(ruleNetwork.getVertices().size());
    final GpmSortStack stack = new GpmSortStack();

    // 03. Initialize stack children of first element, initialize result with first element
    final Set<Vertex> initialElements = ruleNetwork.getVertices().stream()
        .filter(v -> !vToEarlierVs.keySet().contains(v))
        .collect(Collectors.toSet());
    if (initialElements.size() != 1) {
      throw new IllegalArgumentException("Given network does not have exactly one start vertex.");
    }
    final Vertex initialVertex = initialElements.iterator().next();
    for (final Pair<Vertex, Integer> childPair : sortedAdjacency.get(initialVertex)) {
      final Vertex child = childPair.f();
      stack.push(child);
    }
    sortedVertices.add(initialVertex);

    // 04. Actual iteration
    while (!stack.isEmpty()) {
      final Vertex parent = stack.popConditional(sortedVertices);
      sortedVertices.add(parent);
      for (final Pair<Vertex, Integer> childPair : sortedAdjacency.get(parent)) {
        final Vertex child = childPair.f();
        if (!sortedVertices.contains(child)) {
          stack.push(child);
        }
      }
    }

    // 05. Put vertices that merge into the same gateway next to each other
    // (i.e. shift the earlier one down)
    final List<Vertex> result = groupVerticesMergingIntoTheSameGateway(sortedVertices);

    final List<String> idResult = result.stream().map(Vertex::getId)
        .collect(Collectors.toList());
    return new SortedItems(idResult);
  }

  private List<Vertex> groupVerticesMergingIntoTheSameGateway(final List<Vertex> sortedVertices) {
    final Map<Vertex, Integer> vertexToIndex = new LinkedHashMap<>();
    int i = 0;
    for (final Vertex v : sortedVertices) {
      vertexToIndex.put(v, i);
      i++;
    }

    final List<Vertex> result = new ArrayList<>(sortedVertices);

    for (final Vertex vertex : this.ruleNetwork.getVertices()) {
      if (vertex.getType() == VertexType.EXCLUSIVE_GATEWAY
          || vertex.getType() == VertexType.PARALLEL_GATEWAY) {
        final Collection<Vertex> parents = vToParents.get(vertex);
        final int indexOfGateway = vertexToIndex.get(vertex);
        final List<Vertex> sortedParents = parents.stream()
            .sorted((v1, v2) -> vertexToIndex.get(v2) - vertexToIndex.get(v1))
            .collect(Collectors.toList());
        for (final Vertex parent : sortedParents) {
          // This potentially destroys topological sorting, which needs to be corrected later.
          result.remove(parent);
          result.add(indexOfGateway - 1, parent);
        }
      }
    }

    // This fixes the topological order without changing more than necessary in the order
    Vertex vertexToMove;
    int indexToMoveTo = -1;
    do {
      vertexToMove = null;
      for (final Vertex v : result) {
        indexToMoveTo = dependsOnSomethingThatComesLater(v, result);
        if (indexToMoveTo != -1) {
          vertexToMove = v;
          break;
        }
      }
      if (vertexToMove != null) {
        result.remove(vertexToMove);
        result.add(indexToMoveTo, vertexToMove);
      }
    } while (vertexToMove != null);

    return result;
  }

  private int dependsOnSomethingThatComesLater(final Vertex v,
      final List<Vertex> currentOrder) {
    final Set<Integer> indicesOfDependencies = new HashSet<>();
    final int indexOfV = currentOrder.indexOf(v);
    for (final Vertex dependency : vToEarlierVs.get(v)) {
      indicesOfDependencies.add(currentOrder.indexOf(dependency));
    }
    @SuppressWarnings("OptionalGetWithoutIsPresent") final int maximumIndex = indicesOfDependencies
        .stream().mapToInt(i -> i).max().orElse(-1);

    if (maximumIndex > indexOfV) {
      return maximumIndex;
    } else {
      return -1;
    }
  }

  private Multimap<Vertex, Vertex> computeEarlierVerticesPerVertex() {
    @SuppressWarnings("UnstableApiUsage") final Multimap<Vertex, Vertex> result = MultimapBuilder
        .hashKeys().hashSetValues().build();

    for (final Vertex v : ruleNetwork.getVertices()) {
      final Set<Vertex> alreadyVisited = new HashSet<>();
      final Deque<Vertex> stack = new ArrayDeque<>();

      stack.push(v);

      while (!stack.isEmpty()) {
        final Vertex child = stack.pop();
        final Collection<Vertex> parents = vToParents.get(child);
        for (final Vertex parent : parents) {
          if (!alreadyVisited.contains(parent)) {
            alreadyVisited.add(parent);
            result.put(child, parent);
            stack.push(parent);
          }
        }
      }
    }

    return result;
  }

  private HashMap<Vertex, Integer> computeMaximumRemainingActivityPathLengthFromEachVertex() {
    // Initialize data structures
    final HashMap<Vertex, Integer> result = new HashMap<>();
    final Deque<Edge> stack = new ArrayDeque<>();
    final Set<Vertex> visitedVertices = new HashSet<>();

    // Initialize data
    ruleNetwork.getVertices().stream()
        .filter(vertex -> vToChildren.get(vertex).size() == 0)
        .map(vertex -> ruleNetwork.getEdges().stream()
            .filter(edge -> edge.getTarget().equals(vertex))
            .collect(Collectors.toSet()))
        .flatMap(Set::stream)
        .forEach(stack::push);

    ruleNetwork.getVertices().stream()
        .filter(vertex -> vToChildren.get(vertex).size() == 0)
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
        vToIncoming.get(currentE.getSource()).forEach(stack::push);
      }
    }

    return result;
  }

  private void cycleCheck() {
    // For each vertex, try to find a back-edge. If one can be found somewhere, the graph
    // Contains a cycle. Bad runtime, but since the graph is small and we do it once, it is OK
    // It is readable instead :-).
    for (final Vertex v : this.ruleNetwork.getVertices()) {
      // 01. Init dataStructures
      final Deque<Vertex> stack = new ArrayDeque<>();
      final Set<Vertex> visitedVertices = new HashSet<>();

      // 02. Init stack
      stack.push(v);

      // 03. DFS like search to find back-edges
      while (!stack.isEmpty()) {
        final Vertex theV = stack.pop();
        visitedVertices.add(theV);
        final Collection<Vertex> children = vToChildren.get(theV);
        for (final Vertex child : children) {
          if (child.equals(v)) {
            throw new CyclicGraphException();
          }
          if (!visitedVertices.contains(child)) {
            stack.push(child);
          }
        }
      }
    }
  }

  /**
   * We have a stack here with additional props:
   * 1. O(1) contains check
   * 2. pop of the highest element of which all elements it depends on are already in the result
   */
  private class GpmSortStack {

    private final Set<Vertex> stackContent;
    private final Deque<Vertex> stack;

    GpmSortStack() {
      this.stack = new ArrayDeque<>();
      this.stackContent = new HashSet<>();
    }

    public boolean isEmpty() {
      return stack.isEmpty();
    }

    public boolean contains(final Vertex v) {
      return stackContent.contains(v);
    }

    public void push(final Vertex vertex) {
      // Move vertex up in the stack if it is already contained somewhere in the stack
      if (stackContent.contains(vertex)) {
        stack.remove(vertex);
        stackContent.remove(vertex);
      }
      stack.push(vertex);
      stackContent.add(vertex);
    }

    Vertex popConditional(final Collection<Vertex> allowedDependencies) {
      Vertex vertexToPop = null;
      for (final Vertex v : stack) {
        if (allowedDependencies.containsAll(vToEarlierVs.get(v))) {
          vertexToPop = v;
          break;
        }
      }

      if (vertexToPop == null) {
        throw new IllegalStateException("Could not pop an item from the stack, because for all "
            + "remaining items the dependencies are not satisfied.");
      }

      stack.remove(vertexToPop);
      stackContent.remove(vertexToPop);
      return vertexToPop;
    }
  }
}
