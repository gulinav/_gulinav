package de.ukaachen.gpm.core.model.patient;

import de.ukaachen.gpm.mks.model.Code;
import de.ukaachen.gpm.mks.model.PatientProperty;
import java.time.Duration;
import java.util.List;
import java.util.Set;

/**
 * An interface that shall be used to wrap a @{link Patient} from the core library for making the
 * use within the GuidelineProcessor easier.
 */
public interface GPMPatient {

  /**
   * Find a value of one of the patient's properties (for timelines, this yields the most recent
   * value)
   * @param humanReadableKey The key of the value to be retrieved. Note that this is a human
   * readable key for ich a mapping to a technical key has to be set before.
   * @return The value, wrapped in a POJO together with a timestamp of when the value was taken and
   * and expirationTime for the validity of the value (can be Instant.MAX for infinitely long
   * validity). If the value is not present/cannot be derived, null is returned (unwrapped)
   */
  PatientProperty findByHumanReadableKey(final String humanReadableKey);

  /**
   * Find a value of one of the patient's properties  (for timelines, this yields the most recent
   * value)
   * @param code The codeSystem and the CodeValue of the patient's property, colon-separated
   * (e.g. LOINC:20077-4 for the patient's PEEP)
   * @return The value, wrapped in a POJO together with a timestamp of when the value was taken and
   * and expirationTime for the validity of the value (can be Instant.MAX for infinitely long
   * validity). If the value is not present/cannot be derived, null is returned (unwrapped)
   */
  PatientProperty findByCode(final Code code);

  /**
   * Find a list of values from a patient property that is a timeline.
   * @param humanReadableKey The key of the value to be retrieved. Note that this is a human
   *    * readable key for ich a mapping to a technical key has to be set before.
   * @param interval The timespan until now which shall be returned (e.g. from the last 3 days
   * until now)
   * @return The list of values. Each value is exactly as described in the non-timeline method of
   * this interface.
   */
  List<PatientProperty> findTimelineByHumanReadableKey(final String humanReadableKey,
      final Duration interval);

  /**
   * Exactly as the human readable version of this method, but instead the the code
   * @param code The code
   * (e.g. LOINC:20077-4 for the patient's PEEP)
   * @return The list of values. Each value is exactly as described in the non-timeline method of
   * this interface.
   */
  List<PatientProperty> findTimelineByCode(final Code code, final Duration interval);

  /**
   * Returns all human readable keys for which a mapping to a code (such as a LOINC code) is known.
   * @return All human readable keys for which a mapping to a code is known.s
   */
  Set<String> getKnownHumanReadableKeys();

  GPMPatient addProperties(final String humanKey, final Code code,
      final List<PatientProperty> timeline);

  GPMPatient registerPatientEvaluator(final String id, final PatientEvaluator patientEvaluator);
}