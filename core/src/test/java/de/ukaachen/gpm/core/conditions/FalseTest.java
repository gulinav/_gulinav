package de.ukaachen.gpm.core.conditions;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;


import de.ukaachen.gpm.core.model.patient.GPMPatient;
import java.util.Optional;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class FalseTest {

  @Mock
  private GPMPatient patient;

  @Test
  public void testThatFalseEvaluatesToFalseAndPatientIsNotTouched() {
    // Given
    final Condition cut = False.getInstance();

    // When
    final Optional<Boolean> actual = cut.evaluate(patient);

    // Then
    assertThat(actual, is(equalTo(Optional.of(false))));
    Mockito.verifyZeroInteractions(patient);
  }

  @Test
  public void testThatNotFalseBecomesTrue() {
    // Given
    final False cut = False.getInstance();

    // When
    final Condition actual = cut.invert();

    // Then
    assertThat(actual, is(instanceOf(True.class)));
  }

  @Test
  public void testFalseTrue() {
    // Given
    final Condition theTrue = True.getInstance();
    final Condition theFalse = False.getInstance();

    // When
    final Condition and = theFalse.and(theTrue);
    final Condition or = theFalse.or(theTrue);

    // Then
    assertThat(and.evaluate(patient), is(equalTo(Optional.of(false))));
    assertThat(or.evaluate(patient), is(equalTo(Optional.of(true))));
  }

  @Test
  public void testFalseFalse() {
    // Given
    final Condition theFalse1 = False.getInstance();
    final Condition theFalse2 = False.getInstance();

    // When
    final Condition or = theFalse1.or(theFalse2);
    final Condition and = theFalse1.and(theFalse2);

    // Then
    assertThat(or.evaluate(patient), is(equalTo(Optional.of(false))));
    assertThat(and.evaluate(patient), is(equalTo(Optional.of(false))));
    assertThat(theFalse1, is(sameInstance(theFalse2)));
  }
}
