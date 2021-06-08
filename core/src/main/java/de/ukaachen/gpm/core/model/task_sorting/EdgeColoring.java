package de.ukaachen.gpm.core.model.task_sorting;

import de.ukaachen.gpm.core.model.rule_network.Edge;
import java.util.Set;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;


@EqualsAndHashCode
@ToString
public final class EdgeColoring {

  @Getter
  private final Set<Edge> notRemovedCycleEdges;

  @Getter
  private final Set<Edge> nonCycleEdges;

  @Getter
  private final Set<Edge> removedEdges;

  public EdgeColoring(final Set<Edge> notRemovedCycleEdges,
      final Set<Edge> nonCycleEdges,
      final Set<Edge> removedEdges) {
    this.notRemovedCycleEdges = notRemovedCycleEdges;
    this.nonCycleEdges = nonCycleEdges;
    this.removedEdges = removedEdges;
  }
}
