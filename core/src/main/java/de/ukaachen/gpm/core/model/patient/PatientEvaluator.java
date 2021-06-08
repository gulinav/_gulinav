package de.ukaachen.gpm.core.model.patient;

import de.ukaachen.gpm.mks.model.Code;
import de.ukaachen.gpm.mks.model.PatientProperty;
import java.time.Duration;
import java.util.List;

/**
 * TODO
 */
public interface PatientEvaluator {

  /**
   * TODO
   */
  List<PatientProperty> evaluatePatient(
      final Code code, final GPMPatient patient, final Duration interval);
}
