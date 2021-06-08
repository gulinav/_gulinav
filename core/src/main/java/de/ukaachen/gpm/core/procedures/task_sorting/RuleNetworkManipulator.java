package de.ukaachen.gpm.core.procedures.task_sorting;
import de.ukaachen.gpm.core.model.rule_network.RuleNetwork;
import de.ukaachen.gpm.core.model.task_sorting.RuleNetworkManipulatorReturnValue;

public interface RuleNetworkManipulator<T> {

  RuleNetworkManipulatorReturnValue<T> manipulate(final RuleNetwork ruleNetwork);
}
