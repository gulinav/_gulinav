package de.ukaachen.gpm.core.model.rule_network.inferences;

import de.ukaachen.gpm.core.model.rule_network.initial_facts.network_facts.Vtx;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public class Reachable {

  @Getter
  private final Vtx source;

  @Getter
  private final Vtx target;

  public Reachable(final Vtx source, final Vtx target) {
    this.source = source;
    this.target = target;
  }
}
