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
public class TrueTest {

  @Mock
  private GPMPatient patient;

  @Test
  public void testThatTrueEvaluatesToTrueAndPatientIsNotTouched() {
    // Given
    final True cut = True.getInstance();

    // When
    final Optional<Boolean> actual = cut.evaluate(patient);

    // Then
    assertThat(actual, is(equalTo(Optional.of(true))));
    Mockito.verifyZeroInteractions(patient);
  }

  @Test
  public void testThatNotTrueBecomesFalse() {
    // Given
    final True cut = True.getInstance();

    // When
    final Condition actual = cut.invert();

    // Then
    assertThat(actual, is(instanceOf(False.class)));
  }

  @Test
  public void testTrueFalse() {
    // Given
    final True theTrue = True.getInstance();
    final Condition theFalse = False.getInstance();

    // When
    final Condition and = theTrue.and(theFalse);
    final Condition or = theTrue.or(theFalse);

    // Then
    assertThat(and.evaluate(patient), is(equalTo(Optional.of(false))));
    assertThat(or.evaluate(patient), is(equalTo(Optional.of(true))));
  }

  @Test
  public void testTrueTrue() {
    // Given
    final True theTrue1 = True.getInstance();
    final True theTrue2 = True.getInstance();

    // When
    final Condition or = theTrue1.or(theTrue2);
    final Condition and = theTrue1.and(theTrue2);

    // Then
    assertThat(and.evaluate(patient), is(equalTo(Optional.of(true))));
    assertThat(or.evaluate(patient), is(equalTo(Optional.of(true))));
    assertThat(theTrue1, is(sameInstance(theTrue2)));
  }
}
