package de.ukaachen.gpm.core.conditions;

import static de.ukaachen.gpm.core.util.TestConstants.*;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

import de.ukaachen.gpm.core.model.patient.GPMPatient;
import de.ukaachen.gpm.mks.model.PatientProperty;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TermTest {

  @Mock
  private GPMPatient patient;

  @Before
  public void setUp() {
    Mockito.when(patient.getKnownHumanReadableKeys())
        .thenReturn(Stream.of(A, B, C, D, E).collect(Collectors.toSet()));
  }


  @Test
  public void testThatAndWorks() {
    // Given
    Mockito.when(patient.findByHumanReadableKey(A)).thenReturn(new PatientProperty( true));
    Mockito.when(patient.findByHumanReadableKey(B)).thenReturn(new PatientProperty( false));
    Mockito.when(patient.findByHumanReadableKey(C)).thenReturn(new PatientProperty( 6));
    Mockito.when(patient.findByHumanReadableKey(D)).thenReturn(null);
    final Condition con1 = Literal.of("${A == true}");
    final Condition con2 = Literal.of("${B == false}");
    final Condition con3 = Literal.of("${C < 5}");
    final Condition con4 = Literal.of("${D > 6}");
    final Condition con1AndCon2 = Term.and(con1, con2);
    final Condition con2AndCon3 = Term.and(con2, con3);
    final Condition con3AndCon4 = Term.and(con3, con4);
    final Condition con2AndCon4 = Term.and(con2, con4);
    final Condition and123 = con1AndCon2.and(con3);
    final Condition and124 = con1AndCon2.and(con4);
    final Condition and1234 = con1AndCon2.and(con3).and(con4);

    // When and Then
    evaluateConditionAndAssertResult(con1, true);
    evaluateConditionAndAssertResult(con2, true);
    evaluateConditionAndAssertResult(con3, false);
    evaluateConditionAndAssertResult(con4, null);
    evaluateConditionAndAssertResult(con1AndCon2, true);
    evaluateConditionAndAssertResult(con2AndCon3, false);
    evaluateConditionAndAssertResult(con3AndCon4, false);
    evaluateConditionAndAssertResult(con2AndCon4, null);
    evaluateConditionAndAssertResult(and123, false);
    evaluateConditionAndAssertResult(and124, null);
    evaluateConditionAndAssertResult(and1234, false);
  }

  @Test
  public void testThatOrWorks() {
    // Given
    Mockito.when(patient.findByHumanReadableKey(A)).thenReturn(new PatientProperty( true));
    Mockito.when(patient.findByHumanReadableKey(B)).thenReturn(new PatientProperty( true));
    Mockito.when(patient.findByHumanReadableKey(C)).thenReturn(new PatientProperty( 6));
    Mockito.when(patient.findByHumanReadableKey(D)).thenReturn(new PatientProperty( null));
    final Condition con1 = Literal.of("${A == true}");
    final Condition con2 = Literal.of("${B == false}");
    final Condition con3 = Literal.of("${C < 5}");
    final Condition con4 = Literal.of("${D > 6}");
    final Condition con1OrCon2 = Term.or(con1, con2);
    final Condition con2OrCon3 = Term.or(con2, con3);
    final Condition con3OrCon4 = Term.or(con3, con4);
    final Condition con1OrCon4 = Term.or(con1, con4);
    final Condition or123 = con1OrCon2.or(con3);
    final Condition or234 = con2OrCon3.or(con4);
    final Condition or1234 = con1OrCon2.or(con3).or(con4);

    // When and Then
    evaluateConditionAndAssertResult(con1, true);
    evaluateConditionAndAssertResult(con2, false);
    evaluateConditionAndAssertResult(con3, false);
    evaluateConditionAndAssertResult(con4, null);
    evaluateConditionAndAssertResult(con1OrCon2, true);
    evaluateConditionAndAssertResult(con2OrCon3, false);
    evaluateConditionAndAssertResult(con3OrCon4, null);
    evaluateConditionAndAssertResult(con1OrCon4, true);
    evaluateConditionAndAssertResult(or123, true);
    evaluateConditionAndAssertResult(or234, null);
    evaluateConditionAndAssertResult(or1234, true);
  }

  @Test
  public void testThatACombinationOfAndAndOrWorks() {
    // Given
    Mockito.when(patient.findByHumanReadableKey(A)).thenReturn(new PatientProperty( true));
    Mockito.when(patient.findByHumanReadableKey(B)).thenReturn(new PatientProperty( true));
    Mockito.when(patient.findByHumanReadableKey(C)).thenReturn(new PatientProperty( 6));
    Mockito.when(patient.findByHumanReadableKey(D)).thenReturn(new PatientProperty( null));
    final Condition con1 = Literal.of("${A == true}"); // True
    final Condition con2 = Literal.of("${B == false}"); // False
    final Condition con3 = Literal.of("${C < 5}"); // False
    final Condition con4 = Literal.of("${D > 6}"); // null

    // When
    final Condition con1AndCon2 = Term.and(con1, con2); // False
    final Condition con1AndCon2OrCon3 = con1AndCon2.or(con3); // False
    final Condition con1AndCon2OrCon3AndCon1 = con1AndCon2OrCon3.and(con1); // False
    final Condition c1And2Or3And1Or4 = con1AndCon2OrCon3AndCon1.or(con4); // null
    final Condition c1And2Or3And1Or4OrAnotherLongTerm =
        c1And2Or3And1Or4.or(con1AndCon2OrCon3AndCon1); // null

    final Condition evenLongerTerm = c1And2Or3And1Or4OrAnotherLongTerm.and(con1); // null
    final Condition evenLongerTerm2 = evenLongerTerm.or(con1); // True
    final Condition evenLongerTerm3 = evenLongerTerm2.and(c1And2Or3And1Or4OrAnotherLongTerm); // nul
    final Condition evenLongerTerm4 = evenLongerTerm3.or(con1AndCon2OrCon3AndCon1); // null
    final Condition evenLongerTerm5 = evenLongerTerm4.or(evenLongerTerm2); // true

    // Then
    evaluateConditionAndAssertResult(con1, true);
    evaluateConditionAndAssertResult(con2, false);
    evaluateConditionAndAssertResult(con3, false);
    evaluateConditionAndAssertResult(con4, null);
    evaluateConditionAndAssertResult(con1AndCon2, false);
    evaluateConditionAndAssertResult(con1AndCon2OrCon3, false);
    evaluateConditionAndAssertResult(con1AndCon2OrCon3AndCon1, false);
    evaluateConditionAndAssertResult(c1And2Or3And1Or4, null);
    evaluateConditionAndAssertResult(c1And2Or3And1Or4OrAnotherLongTerm, null);
    evaluateConditionAndAssertResult(evenLongerTerm, null);
    evaluateConditionAndAssertResult(evenLongerTerm2, true);
    evaluateConditionAndAssertResult(evenLongerTerm3, null);
    evaluateConditionAndAssertResult(evenLongerTerm4, null);
    evaluateConditionAndAssertResult(evenLongerTerm5, true);
  }

  private void evaluateConditionAndAssertResult(final Condition condition,
      final Boolean expectedResult) {
    assertThat(condition.evaluate(patient), is(equalTo(Optional.ofNullable(expectedResult))));
  }
}
