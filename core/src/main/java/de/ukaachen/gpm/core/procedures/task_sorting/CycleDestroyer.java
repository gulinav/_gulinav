package de.ukaachen.gpm.core.procedures.task_sorting;


import de.ukaachen.gpm.core.exceptions.GraphException;
import de.ukaachen.gpm.core.model.rule_network.Edge;
import de.ukaachen.gpm.core.model.rule_network.RuleNetwork;
import de.ukaachen.gpm.core.model.rule_network.RuleNetworkBuilder;
import de.ukaachen.gpm.core.model.rule_network.VertexType;
import de.ukaachen.gpm.core.model.task_sorting.EdgeColoring;
import de.ukaachen.gpm.core.model.task_sorting.RuleNetworkManipulatorReturnValue;
import de.ukaachen.gpm.core.util.Pair;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.jgrapht.alg.cycle.TiernanSimpleCycles;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;


/**
 * This class removes edges from a given network in order to make it cycle-less
 * When a cycle is seen, the edge that is removed from the cycle is always the one with the longest
 * distance to the start of the network (s.t. after having removed the cycle, the  network is still
 * flowable from the start and the topological order yields the vertices of the circle first if they
 * are closer to the start.
 */
public class CycleDestroyer implements RuleNetworkManipulator<EdgeColoring> {

  private final Map<String, Set<String>> adjacencyList;

  private final Set<Pair<String, String>> anyCycleEdgesEverFound;

  public CycleDestroyer() {
    adjacencyList = new HashMap<>();
    anyCycleEdgesEverFound = new HashSet<>();
  }

  @Override
  public RuleNetworkManipulatorReturnValue<EdgeColoring> manipulate(final RuleNetwork ruleNetwork) {
    if (ruleNetwork == null) {
      throw new IllegalArgumentException("RuleNetwork must not be null.");
    }

    initializeAdjacencyList(ruleNetwork);
    final String startId =
        ruleNetwork
            .getVertices()
            .stream()
            .filter(v -> v.getType() == VertexType.START_EVENT)
            .findFirst()
            .orElseThrow(() -> new GraphException(() -> "Given network has no start event."))
            .getId();

    List<List<String>> chordLessCycles = findChordlessCycles();
    final Set<Pair<String, String>> removedEdges = new HashSet<>();

    // We successively remove all chordless cycles and by induction eventually remove all cycles
    while (!chordLessCycles.isEmpty()) {
      // Always remove the cycles first that are closes to the start
      final List<List<String>> cyclesByDistance = sortCyclesByDistanceAndRearrange(chordLessCycles, startId);
      for (final List<String> cycle : cyclesByDistance) {
        final String source = cycle.get(cycle.size() - 1);
        final String target = cycle.get(0);
        adjacencyList.get(source).remove(target);
        removedEdges.add(Pair.create(source, target));
      }

      chordLessCycles = findChordlessCycles();
    }

    final RuleNetwork resultNetwork = createResultNetwork(ruleNetwork);
    final EdgeColoring edgeColoring = createEdgeColoring(
        removedEdges, ruleNetwork);

    return new RuleNetworkManipulatorReturnValue<>(resultNetwork, edgeColoring);
  }

  private RuleNetwork createResultNetwork(final RuleNetwork ruleNetwork) {
    final RuleNetworkBuilder resultBuilder = new RuleNetworkBuilder();

    ruleNetwork.getVertices().forEach(resultBuilder::addVertex);
    ruleNetwork.getEdges().forEach(edge -> {
      if (adjacencyList.get(edge.getSource().getId()).contains(edge.getTarget().getId())) {
        resultBuilder.addEdge(edge);
      }
    });

    return resultBuilder.build();
  }

  private EdgeColoring createEdgeColoring(
      final Set<Pair<String, String>> removedEdges,
      final RuleNetwork ruleNetwork) {
    final Set<Edge> theCycleEdges = new HashSet<>();
    final Set<Edge> theRemovedCycleEdges = new HashSet<>();
    final Set<Edge> theNonCycleEdges;

    final Map<Pair<String, String>, Edge> pairToEdge = new HashMap<>();
    ruleNetwork.getEdges().forEach(e -> pairToEdge
        .put(Pair.create(e.getSource().getId(), e.getTarget().getId()), e));

    removedEdges.forEach(pair -> theRemovedCycleEdges.add(pairToEdge.get(pair)));
    anyCycleEdgesEverFound.forEach(pair -> {
      if(!removedEdges.contains(pair)) {
        theCycleEdges.add(pairToEdge.get(pair));
      }
    });

    theNonCycleEdges = ruleNetwork.getEdges().stream()
        .filter(edge -> !theRemovedCycleEdges.contains(edge))
        .filter(edge -> !theCycleEdges.contains(edge))
        .collect(Collectors.toSet());

    return new EdgeColoring(theCycleEdges, theNonCycleEdges, theRemovedCycleEdges);
  }

  /**
   * Earliest cycle comes first and first vertex of each list ist the earliest vertex of that cycle
   * @param chordlessCycles The cycles to be sorted and rearranged
   * @param startId Id of the start vertex
   * @return as described.
   */
  private List<List<String>> sortCyclesByDistanceAndRearrange(
      final List<List<String>> chordlessCycles,
      final String startId) {

    final Map<String, Integer> vertexToDistance = new HashMap<>();
    final Consumer<String> putMaxIntValue = v -> vertexToDistance.put(v, Integer.MAX_VALUE);
    adjacencyList.keySet().forEach(putMaxIntValue);
    vertexToDistance.put(startId, 0);

    final Deque<Pair<String, Integer>> stack = new ArrayDeque<>();
    stack.push(Pair.create(startId, 0));

    while (!stack.isEmpty()) {
      final Pair<String, Integer> currentVertex = stack.pop();
      final Collection<String> adjVs = adjacencyList.get(currentVertex.f());
      for (final String adjV : adjVs) {
        if (vertexToDistance.get(adjV) > currentVertex.s() + 1) {
          stack.push(Pair.create(adjV, currentVertex.s() + 1));
          vertexToDistance.put(adjV, currentVertex.s() + 1);
        }
      }
    }

    final List<List<String>> sortedCycles = chordlessCycles.stream().sorted((o1, o2) -> {
      final int o1Distance =
          o1.stream().mapToInt(vertexToDistance::get).min().orElseThrow(RuntimeException::new);
      final int o2Distance =
          o2.stream().mapToInt(vertexToDistance::get).min().orElseThrow(RuntimeException::new);

      final int distanceComparison = Integer.compare(o1Distance, o2Distance);
      if (distanceComparison != 0) {
        return distanceComparison;
      } else {
        return Integer.compare(o1.size(), o2.size());
      }
    }).collect(Collectors.toList());

    // Now the cycles are in the correct order, but we need to make sure each individual cycle
    // starts with the earliest vertex.
    final List<List<String>> result = new ArrayList<>(sortedCycles.size());
    for(final List<String> cycle: sortedCycles) {
      final String min = cycle.stream().min(Comparator.comparingInt(vertexToDistance::get))
          .orElseThrow(() -> new IllegalStateException("Cycle with length 0 not valid."));

      final int index = cycle.indexOf(min);

      final List<String> reorderedCycle = new ArrayList<>(cycle.size());
      for(int i = index; i < index + cycle.size(); i++) {
        reorderedCycle.add(cycle.get(i % cycle.size()));
      }

      result.add(reorderedCycle);
    }

    return result;
  }

  private void initializeAdjacencyList(final RuleNetwork ruleNetwork) {
    adjacencyList.clear();
    ruleNetwork.getVertices().forEach(v -> adjacencyList.put(v.getId(), new HashSet<>()));

    ruleNetwork.getEdges()
        .forEach(edge -> adjacencyList.get(edge.getSource().getId()).add(edge.getTarget().getId()));
  }

  private List<List<String>> findChordlessCycles() {
    final DefaultDirectedGraph<String, DefaultEdge> graph = new DefaultDirectedGraph<>(
        DefaultEdge.class);

    adjacencyList.keySet().forEach(graph::addVertex);
    adjacencyList.keySet().forEach(source -> {
      final Set<String> values = adjacencyList.get(source);
      values.forEach(target -> graph.addEdge(source, target));
    });

    final List<List<String>> simpleCycles = new TiernanSimpleCycles<>(graph).findSimpleCycles();
    simpleCycles.forEach(cycle -> {
      int i = 0;
      for (final String vertex : cycle) {
        anyCycleEdgesEverFound.add(Pair.create(vertex, cycle.get((i + 1) % cycle.size())));
        i++;
      }
    });

    return simpleCycles.stream()
        .filter(this::isChordLess)
        .collect(Collectors.toList());
  }

  private boolean isChordLess(final List<String> cycle) {
    // Init help data structures
    final Set<String> cycleVertices = new HashSet<>(cycle);
    final HashMap<String, Integer> indexOfVertex = new HashMap<>();

    int i = 0;
    for (final String vertex : cycle) {
      indexOfVertex.put(vertex, i);
      i++;
    }

    // Perform chord check
    for (final String vertex : cycle) {
      final Collection<String> adjVs = adjacencyList.get(vertex);
      for (final String adjV : adjVs) {
        if (cycleVertices.contains(adjV) && (indexOfVertex.get(adjV)
            != (indexOfVertex.get(vertex) + 1) % cycle.size())) {
          return false;
        }
      }
    }

    // If no chord found, there is none.
    return true;
  }
}
