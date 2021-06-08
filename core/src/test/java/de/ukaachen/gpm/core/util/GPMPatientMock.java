package de.ukaachen.gpm.core.util;


import de.ukaachen.gpm.core.model.patient.GPMPatient;
import de.ukaachen.gpm.core.model.patient.GuidelineItem;
import de.ukaachen.gpm.core.model.patient.PatientEvaluator;
import de.ukaachen.gpm.mks.model.Code;
import de.ukaachen.gpm.mks.model.PatientProperty;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

/**
 * Until the core library is stable, this is my dummy implementation of the patient
 */
public class GPMPatientMock implements GPMPatient {

  private final Map<String, String> codeMapping = new HashMap<>();
  private final Map<Code, String> codeToValue = new HashMap<>();


  @Getter
  @Setter
  private List<GuidelineItem> guidelineItems;

  public GPMPatientMock() {
  }

  @Override
  public PatientProperty findByHumanReadableKey(final String humanReadableKey) {
    final Code code;
    if(codeMapping.get(humanReadableKey) != null) {
      code = new Code(codeMapping.get(humanReadableKey));
    } else {
      code = null;
    }
    final String value = codeToValue.get(code);
    return mapToPatientProperty(value);
  }

  @Override
  public PatientProperty findByCode(final Code code) {
    return mapToPatientProperty(codeToValue.get(code));
  }

  private PatientProperty mapToPatientProperty(final String value) {
    if (value != null) {
      try {
        return new PatientProperty(Integer.parseInt(value));
      } catch (final Exception e) {
        try {
          return new PatientProperty(Boolean.parseBoolean(value));
        } catch (final Exception e1) {
          try {
            return new PatientProperty(Double.parseDouble(value));
          } catch (final Exception e2) {
            return new PatientProperty(value);
          }
        }
      }
    } else {
      return null;
    }
  }

  @Override
  public List<PatientProperty> findTimelineByHumanReadableKey(final String humanReadableKey,
      final Duration interval) {
    // TODO
    return null;
  }

  @Override
  public List<PatientProperty> findTimelineByCode(final Code code, final Duration interval) {
    // TODO
    return null;
  }

  /**
   * This method will vanish once the real implementation of the patient exists
   * @param code e.g. a loinc-code
   * @param value the patient's value for the loinc-code
   */
  public void putPatientProperty(final Code code, final String value) {
    codeToValue.put(code, value);
  }

  public void putPatientProperty(final String codeAsString, final String value) {
    final String[] theCode = codeAsString.split(":");
    final Code code = new Code(codeAsString);
    codeToValue.put(code, value);
  }

  public void addCodeMapping(final String humanReadable, final String actualCode) {
    if (codeMapping.containsKey(humanReadable) && !codeMapping.get(humanReadable)
        .equals(actualCode)) {
      throw new IllegalArgumentException(
          "The patient already contains a different mapping for " + humanReadable + ": " +
              codeMapping.get(humanReadable));
    }

    codeMapping.put(humanReadable, actualCode);
  }

  @Override
  public Set<String> getKnownHumanReadableKeys() {
    return codeMapping.keySet();
  }

  @Override
  public GPMPatient addProperties(final String humanKey, final Code code,
      final List<PatientProperty> timeline) {
    return null;
  }

  @Override
  public GPMPatient registerPatientEvaluator(final String id,
      final PatientEvaluator patientEvaluator) {
    return null;
  }
}
