package de.ukaachen.gpm.core.model.rule_network.inferences;

import de.ukaachen.gpm.core.model.rule_network.initial_facts.network_facts.Vtx;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public class PotentiallyReachable {

  @Getter
  private final Vtx source;

  @Getter
  private final Vtx target;

  public PotentiallyReachable(final Vtx source,
      final Vtx target) {
    this.source = source;
    this.target = target;
  }
}
