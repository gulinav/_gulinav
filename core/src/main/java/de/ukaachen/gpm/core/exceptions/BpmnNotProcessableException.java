package de.ukaachen.gpm.core.exceptions;

/**
 * Thrown if a given @link{BpmnModelInstance} cannot be processed by the
 * RuleNetworkGpmGuidelinePatientProcessor due to the limitations we demand w.r.t. the BPMN Constructs
 */
public class BpmnNotProcessableException extends RuntimeException {

  public BpmnNotProcessableException(final String message) {
    super(message);
  }
}
