package de.ukaachen.gpm.core.procedures;

import de.ukaachen.gpm.core.model.patient.GPMPatient;
import de.ukaachen.gpm.core.model.rule_network.Edge;
import de.ukaachen.gpm.core.model.rule_network.RuleNetwork;
import de.ukaachen.gpm.core.model.rule_network.initial_facts.network_facts.Edg;
import de.ukaachen.gpm.core.model.rule_network.initial_facts.network_facts.NetworkFacts;
import de.ukaachen.gpm.core.model.rule_network.initial_facts.patient_facts.ConditionSatisfied;
import de.ukaachen.gpm.core.model.rule_network.initial_facts.patient_facts.PotentiallyConditionSatisfied;
import de.ukaachen.gpm.core.util.Pair;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;


public class ConditionFactsPreparator {

  private final RuleNetwork ruleNetwork;

  private final Map<String, Edg> idToEdg;

  public ConditionFactsPreparator(
      final RuleNetwork ruleNetwork,
      final NetworkFacts networkFacts) {
    this.ruleNetwork = ruleNetwork;

    this.idToEdg = new HashMap<>();
    networkFacts.getEdges().forEach(edg -> idToEdg.put(edg.getId(), edg));
  }

  /**
   * Banana Banana
   * @param patient
   * @return
   */
  public Pair<Set<ConditionSatisfied>, Set<PotentiallyConditionSatisfied>> determineEdgesWhoseConditionIsSatisfied(
      final GPMPatient patient) {
    if (patient == null) {
      throw new IllegalArgumentException("Patient must not be null.");
    }

    final Set<ConditionSatisfied> satisfiedEdges = ruleNetwork.getEdges()
        .stream()
        .filter(edge -> {
          final Optional<Boolean> evaluation = edge.getCondition().evaluate(patient);
          return evaluation.isPresent() && evaluation.get();
        })
        .map(Edge::getId)
        .map(idToEdg::get)
        .map(ConditionSatisfied::new)
        .collect(Collectors.toSet());

    final Set<PotentiallyConditionSatisfied> potentiallySatisfiedEdges = ruleNetwork.getEdges()
        .stream()
        .filter(edge -> edge.getCondition().evaluate(patient).isEmpty())
        .map(Edge::getId)
        .map(idToEdg::get)
        .map(PotentiallyConditionSatisfied::new)
        .collect(Collectors.toSet());

    return Pair.create(satisfiedEdges, potentiallySatisfiedEdges);
  }

}
