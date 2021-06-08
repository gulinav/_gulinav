package de.ukaachen.gpm.core.model.rule_network.initial_facts.network_facts;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public class Edg {

  @Getter
  private final String id;
  @Getter
  private final Vtx source;
  @Getter
  private final Vtx target;

  public Edg(final String id, final Vtx source, final Vtx target) {
    this.id = id;
    this.source = source;
    this.target = target;
  }
}
