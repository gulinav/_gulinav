package de.ukaachen.gpm.core.model.rule_network.inferences;

import de.ukaachen.gpm.core.model.rule_network.initial_facts.network_facts.Edg;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;


@ToString
@EqualsAndHashCode
public class PotentiallyTraversable {

  @Getter
  private final Edg edge;

  public PotentiallyTraversable(final Edg edge) {
    this.edge = edge;
  }
}
