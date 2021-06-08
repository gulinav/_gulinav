package de.ukaachen.gpm.core.procedures;

import de.ukaachen.gpm.core.exceptions.BpmnNotProcessableException;
import java.io.File;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;


public class BpmnVerifierTest {


  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  private BpmnModelInstance getModelForFile(final String fileName) {
    return Bpmn.readModelFromFile(new File(getClass().getResource(fileName).getFile()));
  }

  @Test
  public void testThatTheVerifierThrowsAnExceptionIfTheSameKeyIsUsedForDifferentCodes()
      throws BpmnNotProcessableException {
    // Given
    final BpmnModelInstance model = getModelForFile("verifier_duplicate_key.bpmn");
    expectedException.expect(BpmnNotProcessableException.class);

    // When
    BpmnVerifier.forBpmnModel(model).verifyProcessability();

    // Then Throws Exception
  }

  @Test
  public void testThatTheVerifierThrowsAnExceptionIfTheBpmnContainsOtherTasksThanServiceTasks()
      throws BpmnNotProcessableException {
    // Given
    final BpmnModelInstance model = getModelForFile("verifier_wrong_task_type.bpmn");
    expectedException.expect(BpmnNotProcessableException.class);

    // When
    BpmnVerifier.forBpmnModel(model).verifyProcessability();

    // Then Throws Exception
  }

  @Test
  public void testThatTheVerifierThrowsAnExceptionIfTheBpmnContainsElementsThatAreNotYetSupported()
      throws BpmnNotProcessableException {
    // Given
    final BpmnModelInstance model = getModelForFile("verifier_non_supported_element.bpmn");
    expectedException.expect(BpmnNotProcessableException.class);

    // When
    BpmnVerifier.forBpmnModel(model).verifyProcessability();

    // Then Throws Exception
  }

  @Test
  public void testThatTheVerifierAcceptsAValidModel() {
    // Given
    final BpmnModelInstance model = getModelForFile("verifier_valid.bpmn");

    // When
    try {
      BpmnVerifier.forBpmnModel(model).verifyProcessability();
    } catch (final BpmnNotProcessableException e) {
      throw new AssertionError("Verifier has thrown an exception even if "
          + "the given BPMN-Model was valid");
    }

    // Then no exception should have been thrown
  }

  @Test
  public void testThatTheVerifierChecksTheIntegrityOfTheConditionExpressions()
      throws BpmnNotProcessableException {
    // Given
    final BpmnModelInstance model = getModelForFile("verifier_wrong_condition_expression.bpmn");
    expectedException.expect(BpmnNotProcessableException.class);

    // When
    BpmnVerifier.forBpmnModel(model).verifyProcessability();

    // Then
    // Should have thrown exception
  }
}
