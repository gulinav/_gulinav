package de.ukaachen.gpm.core.model.rule_network.inferences;


import de.ukaachen.gpm.core.model.patient.TimePhase;
import de.ukaachen.gpm.core.model.rule_network.initial_facts.network_facts.Vtx;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public class VertexTimePhase {

  @Getter
  private final Vtx vertex;

  @Getter
  private final TimePhase timePhase;

  public VertexTimePhase(final Vtx vertex,
      final TimePhase timePhase) {
    this.vertex = vertex;
    this.timePhase = timePhase;
  }
}
