package de.ukaachen.gpm.core.model.rule_network.initial_facts.network_facts;

import de.ukaachen.gpm.core.model.rule_network.VertexType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public class Vtx {

  @Getter
  private final String id;

  @Getter
  private final VertexType type;

  public Vtx(final String id, final VertexType type) {
    this.id = id;
    this.type = type;
  }
}
