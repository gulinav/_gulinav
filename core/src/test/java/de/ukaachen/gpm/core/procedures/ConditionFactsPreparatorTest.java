package de.ukaachen.gpm.core.procedures;


import static de.ukaachen.gpm.core.util.VertexUtil.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;

import static de.ukaachen.gpm.core.util.TestConstants.*;

import de.ukaachen.gpm.core.conditions.Literal;
import de.ukaachen.gpm.core.conditions.True;
import de.ukaachen.gpm.core.model.patient.GPMPatient;
import de.ukaachen.gpm.core.model.rule_network.RuleNetwork;
import de.ukaachen.gpm.core.model.rule_network.RuleNetworkBuilder;
import de.ukaachen.gpm.core.model.rule_network.initial_facts.network_facts.Edg;
import de.ukaachen.gpm.core.model.rule_network.initial_facts.network_facts.NetworkFacts;
import de.ukaachen.gpm.core.model.rule_network.initial_facts.patient_facts.ConditionSatisfied;
import de.ukaachen.gpm.core.model.rule_network.initial_facts.patient_facts.PotentiallyConditionSatisfied;
import de.ukaachen.gpm.core.util.Pair;
import de.ukaachen.gpm.mks.model.PatientProperty;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

// We know that the things are present in the mocked data
@SuppressWarnings("OptionalGetWithoutIsPresent")
@RunWith(MockitoJUnitRunner.class)
public class ConditionFactsPreparatorTest {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Test
  public void testWhenGivenEmptyNetwork_emptySetsAreReturned() {
    // Given
    final RuleNetwork emptyRuleNetwork = new RuleNetworkBuilder().build();
    final NetworkFacts nwFacts = new DroolsFactsHandler(emptyRuleNetwork).getNetworkFacts();
    final ConditionFactsPreparator cut = new ConditionFactsPreparator(emptyRuleNetwork, nwFacts);
    final GPMPatient guidelinePatient = Mockito.mock(GPMPatient.class);

    // When
    final Pair<Set<ConditionSatisfied>, Set<PotentiallyConditionSatisfied>> actual = cut
        .determineEdgesWhoseConditionIsSatisfied(guidelinePatient);

    // Then
    assertThat(actual.f(), is(empty()));
    assertThat(actual.s(), is(empty()));
  }

  @Test
  public void testWhenGivenSatisfiedEdgesOnly_onlyConditionSatisfiedsAreReturned() {
    // Given
    final RuleNetwork ruleNetwork = new RuleNetworkBuilder()
        .addVertices(v(A), v(B), v(C))
        .addEdge(X, A, B, Literal.of("${A == true}"))
        .addEdge(Y, B, C, True.getInstance())
        .build();
    final NetworkFacts nwFacts = new DroolsFactsHandler(ruleNetwork).getNetworkFacts();
    final Edg x =
        nwFacts.getEdges().stream().filter(e -> e.getId().equals(X)).findAny().get();
    final Edg y =
        nwFacts.getEdges().stream().filter(e -> e.getId().equals(Y)).findAny().get();

    final GPMPatient patient = Mockito.mock(GPMPatient.class);
    Mockito.when(patient.findByHumanReadableKey(A)).thenReturn(new PatientProperty(true));
    Mockito.when(patient.getKnownHumanReadableKeys()).thenReturn(Collections.singleton(A));
    final ConditionFactsPreparator cut = new ConditionFactsPreparator(ruleNetwork, nwFacts);

    // When
    final Pair<Set<ConditionSatisfied>, Set<PotentiallyConditionSatisfied>> actual = cut
        .determineEdgesWhoseConditionIsSatisfied(patient);

    // Then
    assertThat(actual.f(), containsInAnyOrder(new ConditionSatisfied(x),
        new ConditionSatisfied(y)));
    assertThat(actual.s(), is(empty()));
  }

  @Test
  public void testWhenGivenUnknownStateEdgesOnly_onlyPotentiallyConditionSatisfiedsAreReturned() {
    // Given
    final RuleNetwork ruleNetwork = new RuleNetworkBuilder()
        .addVertices(v(A), v(B), v(C))
        .addEdge(X, A, B, Literal.of("${A == true}"))
        .addEdge(Y, B, C, Literal.of("${B < 5}"))
        .build();
    final NetworkFacts nwFacts = new DroolsFactsHandler(ruleNetwork).getNetworkFacts();

    final Edg x = nwFacts.getEdges().stream().filter(e -> e.getId().equals(X)).findAny().get();
    final Edg y = nwFacts.getEdges().stream().filter(e -> e.getId().equals(Y)).findAny().get();

    final GPMPatient patient = Mockito.mock(GPMPatient.class);
    Mockito.when(patient.findByHumanReadableKey(A)).thenReturn(new PatientProperty(null));
    Mockito.when(patient.findByHumanReadableKey(B)).thenReturn(new PatientProperty(null));
    Mockito.when(patient.getKnownHumanReadableKeys())
        .thenReturn(Stream.of(A, B).collect(Collectors.toSet()));
    final ConditionFactsPreparator cut = new ConditionFactsPreparator(ruleNetwork,
        nwFacts);

    // When
    final Pair<Set<ConditionSatisfied>, Set<PotentiallyConditionSatisfied>> actual = cut
        .determineEdgesWhoseConditionIsSatisfied(patient);

    // Then
    assertThat(actual.f(), is(empty()));
    assertThat(actual.s(), containsInAnyOrder(new PotentiallyConditionSatisfied(x),
        new PotentiallyConditionSatisfied(y)));
  }

  @Test
  public void testWhenGivenOnlyUnsatisfiedEdges_emptySetsAreReturned() {
    // Given
    final RuleNetwork ruleNetwork = new RuleNetworkBuilder()
        .addVertices(v(A), v(B), v(C))
        .addEdge(X, A, B, Literal.of("${A == true}"))
        .addEdge(Y, B, C, Literal.of("${B < 5}"))
        .build();
    final NetworkFacts nwFacts = new DroolsFactsHandler(ruleNetwork).getNetworkFacts();

    final GPMPatient patient = Mockito.mock(GPMPatient.class);
    Mockito.when(patient.findByHumanReadableKey(A)).thenReturn(new PatientProperty(false));
    Mockito.when(patient.findByHumanReadableKey(B)).thenReturn(new PatientProperty(7.3));
    Mockito.when(patient.getKnownHumanReadableKeys())
        .thenReturn(Stream.of(A, B).collect(Collectors.toSet()));

    final ConditionFactsPreparator cut = new ConditionFactsPreparator(ruleNetwork,
        nwFacts);

    // When
    final Pair<Set<ConditionSatisfied>, Set<PotentiallyConditionSatisfied>> actual = cut
        .determineEdgesWhoseConditionIsSatisfied(patient);

    // Then
    assertThat(actual.f(), is(empty()));
    assertThat(actual.s(), is(empty()));
  }

  @Test
  public void testWhenGivenMixture_mixtureIsReturned() {
    // Given
    final RuleNetwork ruleNetwork = new RuleNetworkBuilder()
        .addVertices(v(A), v(B), v(C))
        .addEdge(X, A, B, Literal.of("${A == true}"))
        .addEdge(Y, B, C, Literal.of("${B < 5}"))
        .addEdge(Z, C, A, Literal.of("${C == true}"))
        .build();

    final NetworkFacts nwFacts = new DroolsFactsHandler(ruleNetwork).getNetworkFacts();

    final Edg x =
        nwFacts.getEdges().stream().filter(e -> e.getId().equals(X)).findAny().get();
    final Edg y =
        nwFacts.getEdges().stream().filter(e -> e.getId().equals(Y)).findAny().get();

    final GPMPatient patient = Mockito.mock(GPMPatient.class);
    Mockito.when(patient.findByHumanReadableKey(A)).thenReturn(new PatientProperty(true));
    Mockito.when(patient.findByHumanReadableKey(B)).thenReturn(new PatientProperty(null));
    Mockito.when(patient.findByHumanReadableKey(C)).thenReturn(new PatientProperty(false));
    Mockito.when(patient.getKnownHumanReadableKeys())
        .thenReturn(Stream.of(A, B, C).collect(Collectors.toSet()));

    final ConditionFactsPreparator cut = new ConditionFactsPreparator(ruleNetwork,
        nwFacts);

    // When
    final Pair<Set<ConditionSatisfied>, Set<PotentiallyConditionSatisfied>> actual = cut
        .determineEdgesWhoseConditionIsSatisfied(patient);

    // Then
    assertThat(actual.f(), containsInAnyOrder(new ConditionSatisfied(x)));
    assertThat(actual.s(), containsInAnyOrder(new PotentiallyConditionSatisfied(y)));
  }

  @Test
  public void testIllegalArgumentExceptionIfPatientIsNull() {
    expectedException.expect(IllegalArgumentException.class);

    // Given
    final RuleNetwork ruleNetwork = new RuleNetworkBuilder().build();
    final NetworkFacts nwFacts = new DroolsFactsHandler(ruleNetwork).getNetworkFacts();
    final ConditionFactsPreparator cut = new ConditionFactsPreparator(ruleNetwork, nwFacts);

    // When
    cut.determineEdgesWhoseConditionIsSatisfied(null);

    // Then should have thrown exception
  }
}
