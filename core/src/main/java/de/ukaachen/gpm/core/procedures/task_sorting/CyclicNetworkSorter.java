package de.ukaachen.gpm.core.procedures.task_sorting;


import de.ukaachen.gpm.core.model.rule_network.RuleNetwork;
import de.ukaachen.gpm.core.model.rule_network.SortedItems;
import de.ukaachen.gpm.core.model.task_sorting.EdgeColoring;
import de.ukaachen.gpm.core.model.task_sorting.RuleNetworkManipulatorReturnValue;
import java.util.function.Function;

public class CyclicNetworkSorter implements TopologicalSorter {

  private final RuleNetwork ruleNetwork;
  private final Function<RuleNetwork, TopologicalSorter> nonCyclicSorterSupplier;

  public CyclicNetworkSorter(
      final RuleNetwork ruleNetwork,
      final Function<RuleNetwork, TopologicalSorter> nonCyclicSorterSupplier) {
    this.ruleNetwork = ruleNetwork;
    this.nonCyclicSorterSupplier = nonCyclicSorterSupplier;
  }

  @Override
  public SortedItems sort() {
    final CycleDestroyer cycleDestroyer = new CycleDestroyer();
    final RuleNetworkManipulatorReturnValue<EdgeColoring> cycleDestroyerResult = cycleDestroyer
        .manipulate(this.ruleNetwork);

    final RuleNetwork cyclelessNetwork = cycleDestroyerResult.getManipulatedNetwork();

    return nonCyclicSorterSupplier.apply(cyclelessNetwork).sort();
  }
}
