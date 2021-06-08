package de.ukaachen.gpm.core.model.rule_network.initial_facts.network_facts;

import java.util.Set;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public class NetworkFacts {

  @Getter
  private final Set<Vtx> vertices;
  @Getter
  private final Set<Edg> edges;

  public NetworkFacts(
      final Set<Vtx> vertices, final Set<Edg> edges) {
    this.vertices = vertices;
    this.edges = edges;
  }
}
