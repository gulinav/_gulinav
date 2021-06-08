package de.ukaachen.gpm.core.model.rule_network.initial_facts.patient_facts;

import de.ukaachen.gpm.core.model.rule_network.initial_facts.network_facts.Vtx;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public class VertexFinished {

  @Getter
  private final Vtx vertex;

  public VertexFinished(final Vtx vertex) {
    this.vertex = vertex;
  }
}
