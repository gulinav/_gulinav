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
public class LiteralTest {

  @Mock
  private GPMPatient patient;

  @Before
  public void setUp() {
    Mockito.when(patient.getKnownHumanReadableKeys())
        .thenReturn(Stream.of(A, B, C, D, E).collect(Collectors.toSet()));
  }

  @Test
  public void testThatEvaluatingALiteralToTrueWorks() {
    // Given
    final String condition = "${A < 5}";
    final Literal cut = Literal.of(condition);

    // Mocked data
    Mockito.when(patient.findByHumanReadableKey(A)).thenReturn(new PatientProperty( 3));

    // When
    final Optional<Boolean> actual = cut.evaluate(patient);

    // Then
    assertThat(actual, is(equalTo(Optional.of(true))));
  }

  @Test
  public void testThatEvaluatingALiteralToFalseWorks() {
    // Given
    final String condition = "${A < 5}";
    final Literal cut = Literal.of(condition);
    Mockito.when(patient.findByHumanReadableKey(A)).thenReturn(new PatientProperty( 8));

    // When
    final Optional<Boolean> actual = cut.evaluate(patient);

    // Then
    assertThat(actual, is(equalTo(Optional.of(false))));
  }

  @Test
  public void testThatEvaluatingALiteralToAnEmptyOptionalWorks() {
    // Given
    final String condition = "${A < 5}";
    final Literal cut = Literal.of(condition);
    Mockito.when(patient.findByHumanReadableKey(A)).thenReturn(new PatientProperty( null));

    // When
    final Optional<Boolean> actual = cut.evaluate(patient);

    // Then
    assertThat(actual, is(equalTo(Optional.empty())));
  }

  @Test
  public void testThatInvertingALiteralWorks() {
    // Given
    final String condition = "${A < 5}";
    final Condition cut = Literal.of(condition).invert();
    Mockito.when(patient.findByHumanReadableKey(A)).thenReturn(new PatientProperty( 3));

    // When
    final Optional<Boolean> actual = cut.evaluate(patient);

    // Then
    assertThat(actual, is(equalTo(Optional.of(false))));
  }

  @Test
  public void testThatConnectingALiteralByAndWorks() {
    // Given
    final String condition = "${A < 5}";
    final String condition2 = "${B >= 5}";
    final Condition cut = Literal.of(condition).and(Literal.of(condition2));
    Mockito.when(patient.findByHumanReadableKey(A)).thenReturn(new PatientProperty( 3));
    Mockito.when(patient.findByHumanReadableKey(B)).thenReturn(new PatientProperty( 6));

    // When
    final Optional<Boolean> actual = cut.evaluate(patient);

    // Then
    assertThat(actual, is(equalTo(Optional.of(true))));
  }

  @Test
  public void testThatConnectingALiteralByOrWorks() {
    // Given
    final String condition = "${A < 5}";
    final String condition2 = "${B >= 5}";
    final Condition cut = Literal.of(condition).or(Literal.of(condition2));
    Mockito.when(patient.findByHumanReadableKey(A)).thenReturn(new PatientProperty( 6));
    Mockito.when(patient.findByHumanReadableKey(B)).thenReturn(new PatientProperty( 2));

    // When
    final Optional<Boolean> actual = cut.evaluate(patient);

    // Then
    assertThat(actual, is(equalTo(Optional.of(false))));
  }

  @Test
  public void testThatLazyEvaluationWorks() {
    // Given
    final String condition = "${A < 5}";
    final String condition2 = "${B >= 5}";
    final Condition cut = Literal.of(condition).or(Literal.of(condition2));
    Mockito.when(patient.findByHumanReadableKey(A)).thenReturn(new PatientProperty( null));
    Mockito.when(patient.findByHumanReadableKey(B)).thenReturn(new PatientProperty( 6));

    // When
    final Optional<Boolean> actual = cut.evaluate(patient);

    // Then
    assertThat(actual, is(equalTo(Optional.of(true))));
  }
}
