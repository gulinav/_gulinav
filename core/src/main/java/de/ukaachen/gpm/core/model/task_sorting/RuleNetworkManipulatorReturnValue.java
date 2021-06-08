package de.ukaachen.gpm.core.model.task_sorting;


import de.ukaachen.gpm.core.model.rule_network.RuleNetwork;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public class RuleNetworkManipulatorReturnValue< T > {

  @Getter
  private final RuleNetwork manipulatedNetwork;

  @Getter
  private final T manipulationInfo;

  public RuleNetworkManipulatorReturnValue(
      final RuleNetwork manipulatedNetwork, final T manipulationInfo) {
    this.manipulatedNetwork = manipulatedNetwork;
    this.manipulationInfo = manipulationInfo;
  }
}
