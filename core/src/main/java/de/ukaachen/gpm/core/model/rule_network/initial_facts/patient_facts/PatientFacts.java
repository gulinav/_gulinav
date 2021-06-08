package de.ukaachen.gpm.core.model.rule_network.initial_facts.patient_facts;

import java.util.Set;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public class PatientFacts {

  @Getter
  private final Set<ConditionSatisfied> satisfiedConditions;

  @Getter
  private final Set<PotentiallyConditionSatisfied> potentiallySatisfiedConditions;

  @Getter
  private final Set<VertexFinished> finishedVertices;

  public PatientFacts(
      final Set<ConditionSatisfied> satisfiedConditions,
      final Set<PotentiallyConditionSatisfied> potentiallySatisfiedConditions,
      final Set<VertexFinished> finishedVertices) {
    this.satisfiedConditions = satisfiedConditions;
    this.potentiallySatisfiedConditions = potentiallySatisfiedConditions;
    this.finishedVertices = finishedVertices;
  }
}
