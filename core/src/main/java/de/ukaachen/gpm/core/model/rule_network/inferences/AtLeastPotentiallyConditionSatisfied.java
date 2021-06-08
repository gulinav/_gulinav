package de.ukaachen.gpm.core.model.rule_network.inferences;

import de.ukaachen.gpm.core.model.rule_network.initial_facts.network_facts.Edg;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public class AtLeastPotentiallyConditionSatisfied {

  @Getter
  private final Edg edge;

  public AtLeastPotentiallyConditionSatisfied(
      final Edg edge) {
    this.edge = edge;
  }
}
