package de.ukaachen.gpm.core.model.rule_network.inferences;

import de.ukaachen.gpm.core.model.rule_network.initial_facts.network_facts.Vtx;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public class AtLeastPotentiallyReachableFromStart {

  @Getter
  private final Vtx vertex;

  public AtLeastPotentiallyReachableFromStart(final Vtx vertex) {
    this.vertex = vertex;
  }
}
