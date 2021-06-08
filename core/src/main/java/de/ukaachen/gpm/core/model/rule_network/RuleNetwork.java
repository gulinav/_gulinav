package de.ukaachen.gpm.core.model.rule_network;

import java.util.Set;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public class RuleNetwork {

  @Getter
  private final Set<Edge> edges;

  @Getter
  private final Set<Vertex> vertices;

  public RuleNetwork(final Set<Edge> edges,
      final Set<Vertex> vertices) {
    this.edges = edges;
    this.vertices = vertices;
  }
}
