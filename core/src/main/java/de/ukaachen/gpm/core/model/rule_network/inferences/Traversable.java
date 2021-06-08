package de.ukaachen.gpm.core.model.rule_network.inferences;

import de.ukaachen.gpm.core.model.rule_network.initial_facts.network_facts.Edg;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.kie.api.definition.type.Position;

@ToString
@EqualsAndHashCode
public class Traversable {

  @Getter
  @Position(0)
  private final Edg edge;

  public Traversable(final Edg edge) {
    this.edge = edge;
  }
}
