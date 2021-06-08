package de.ukaachen.gpm.core.model.patient_evaluators;

import de.ukaachen.gpm.mks.model.Code;
import de.ukaachen.gpm.core.model.patient.GPMMedicalKnowledgeContext;
import de.ukaachen.gpm.core.model.patient.GPMPatient;
import de.ukaachen.gpm.core.model.patient.PatientEvaluator;
import de.ukaachen.gpm.mks.model.PatientProperty;
import de.ukaachen.gpm.mks.model.DemoMedicalKnowledgeService;
import de.ukaachen.gpm.mks.model.MedicalKnowledgeModule;
import de.ukaachen.gpm.mks.model.TernaryEvaluationResponse;
import java.time.Duration;
import java.util.Collections;
import java.util.List;

public class MedicalKnowledgeModuleEvaluator implements PatientEvaluator {

  private DemoMedicalKnowledgeService knowledgeService = new DemoMedicalKnowledgeService();

  @Override
  public List<PatientProperty> evaluatePatient(final Code code, final GPMPatient patient,
      final Duration interval) {

    final GPMMedicalKnowledgeContext context = new GPMMedicalKnowledgeContext(patient);
    final MedicalKnowledgeModule<TernaryEvaluationResponse> medicalKnowledgeModule =
        knowledgeService.getModules().get(code.getValue());

    final TernaryEvaluationResponse response = medicalKnowledgeModule.evaluate(context);

    return Collections.singletonList(new PatientProperty(code, response));
  }
}
