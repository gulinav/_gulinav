package de.ukaachen.gpm.mks.mk_modules;

import de.ukaachen.gpm.mks.model.MedicalKnowledgeContext;
import de.ukaachen.gpm.mks.model.MedicalKnowledgeModule;
import de.ukaachen.gpm.mks.model.TernaryEvaluationResponse;

public class IndependentBloodSampleModule implements
    MedicalKnowledgeModule<TernaryEvaluationResponse> {

  @Override
  public TernaryEvaluationResponse evaluate(final MedicalKnowledgeContext context) {
    // For demonstrator purposes, this evaluation defaults to TRUE
    return TernaryEvaluationResponse.TRUE;
  }
}
