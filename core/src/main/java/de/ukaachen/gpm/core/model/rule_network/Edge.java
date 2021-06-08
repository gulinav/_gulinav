package de.ukaachen.gpm.core.model.rule_network;

import de.ukaachen.gpm.core.conditions.Condition;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public class Edge {

  @Getter
  private final String id;

  @Getter
  private final Vertex source;

  @Getter
  private final Vertex target;

  @Getter
  private final Condition condition;

  public Edge(final String id, final Vertex source,
      final Vertex target, final Condition condition) {
    this.id = id;
    this.source = source;
    this.target = target;
    this.condition = condition;
  }
}
