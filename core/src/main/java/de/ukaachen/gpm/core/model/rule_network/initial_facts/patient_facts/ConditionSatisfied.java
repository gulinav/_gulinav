package de.ukaachen.gpm.core.model.rule_network.initial_facts.patient_facts;

import de.ukaachen.gpm.core.model.rule_network.initial_facts.network_facts.Edg;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public class ConditionSatisfied {

  @Getter
  private final Edg edge;

  public ConditionSatisfied(final Edg edge) {
    this.edge = edge;
  }
}
