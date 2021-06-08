package de.ukaachen.gpm.core.procedures.task_sorting;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import de.ukaachen.gpm.core.model.rule_network.RuleNetwork;
import de.ukaachen.gpm.core.model.rule_network.SortedItems;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Improves the topological sorting obtained by kahn's algorithm by grouping individual "paths"
 * that do not depend on anything else instead of them being randomly fragmented.
 */
public class RuleNetworkTopologicalSorterRetainPathSegments extends
    RuleNetworkTopologicalSorterKahnsAlgorithm {

  public RuleNetworkTopologicalSorterRetainPathSegments(final RuleNetwork ruleNetwork) {
    super(ruleNetwork);
  }

  @Override
  public SortedItems sort() {
    final List<String> sortedByKahn = new ArrayList<>(super.sort().getVertexIds());
    final List<String> reordered = reorderTopologicalSorting(sortedByKahn);
    return new SortedItems(reordered);
  }

  List<String> reorderTopologicalSorting(final List<String> preOrdered) {
    final SortedMap<String, String> vertexToEnforcedSuccessor =
        new TreeMap<>(Comparator.comparingInt(preOrdered::indexOf));

    final Multimap<String, String> vertexToSucceeding =
        MultimapBuilder.hashKeys().hashSetValues().build();
    final Multimap<String, String> vertexToPreceeding =
        MultimapBuilder.hashKeys().hashSetValues().build();

    ruleNetwork.getEdges().forEach(e -> {
      vertexToSucceeding.put(e.getSource().getId(), e.getTarget().getId());
      vertexToPreceeding.put(e.getTarget().getId(), e.getSource().getId());
    });

    preOrdered.forEach(vertex -> {
      if (vertexToPreceeding.get(vertex).size() == 1) {
        // If there is only on predecessor
        final String predecessor = vertexToPreceeding.get(vertex).iterator().next();
        // And the predecessor does not have any other successors..
        if (vertexToSucceeding.get(predecessor).size() == 1) {
          // We can enforce these buddies to follow each other
          vertexToEnforcedSuccessor.put(vertexToPreceeding.get(vertex).iterator().next(), vertex);
        }
      }
    });

    // Move all vertices with an enforced predecessor right in front of it
    // Since the map is sorted by index in the pre-sorted list, the earliest elements are shifted
    // First.
    vertexToEnforcedSuccessor.forEach((pre, succ) -> {
      final int indexOfPre = preOrdered.indexOf(pre);
      preOrdered.remove(succ);
      preOrdered.add(indexOfPre + 1, succ);
    });

    return preOrdered;
  }
}
