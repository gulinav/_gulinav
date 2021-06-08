package de.ukaachen.gpm.core.model.patient;

import de.ukaachen.gpm.mks.model.Code;
import de.ukaachen.gpm.mks.model.PatientProperty;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Getter;

public class SimplePatient implements GPMPatient {

  private final Map<String, List<PatientProperty>> hrToValue;
  private final Map<Code, List<PatientProperty>> codeToValue;
  private final Map<String, Code> keyToCode;

  @Getter
  private final HashMap<String, PatientEvaluator> codeSystemToEvaluator;


  public SimplePatient() {
    this.hrToValue = new HashMap<>();
    this.codeToValue = new HashMap<>();
    this.keyToCode = new HashMap<>();
    this.codeSystemToEvaluator = new HashMap<>();
  }

  @Override
  public PatientProperty findByHumanReadableKey(final String humanReadableKey) {
    return findByX(humanReadableKey, hrToValue);
  }

  @Override
  public PatientProperty findByCode(final Code code) {
    return findByX(code, codeToValue);
  }

  private <T> PatientProperty findByX(final T key,
      final Map<T, List<PatientProperty>> map) {
    final List<PatientProperty> timeline = findTimelineByX(key, map, null);
    if (timeline == null || timeline.size() == 0) {
      return null;
    } else {
      return timeline.get(timeline.size() - 1);
    }
  }

  @Override
  public List<PatientProperty> findTimelineByHumanReadableKey(final String humanReadableKey,
      final Duration interval) {
    return findTimelineByX(humanReadableKey, hrToValue, interval);
  }

  @Override
  public List<PatientProperty> findTimelineByCode(final Code code, final Duration interval) {
    return findTimelineByX(code, codeToValue, interval);
  }

  private <T> List<PatientProperty> findTimelineByX(
      final T key, final Map<T, List<PatientProperty>> map, final Duration interval) {

    final Code correspondingCode;
    if(!(key instanceof Code)) {
      if(!(key instanceof String)) {
        throw new IllegalStateException("Simple Patient is bugged.");
      }
      correspondingCode = this.keyToCode.get(key);
      if(correspondingCode == null) {
        throw new IllegalStateException("Key to code mapping is missing for key " + key);
      }
    } else {
      correspondingCode = (Code) key;
    }

    // This segment deals with keys that are not plain properties but are delegated to evaluators
    // #### SEG START
      if (this.codeSystemToEvaluator.containsKey(correspondingCode.getSystem())) {
        return this.codeSystemToEvaluator.get(correspondingCode.getSystem())
            .evaluatePatient(correspondingCode, this, interval);
      }
    // #### SEG END
    // if we did not return, the key is a simple patient prop that we can fetch ourselves

    final List<PatientProperty> timeline = map.get(key);
    if (timeline == null || timeline.size() == 0) {
      return null;
    }

    if (interval == null) {
      return timeline;
    } else {
      final Instant newerThan = Instant.now().minus(interval);
      return timeline.stream()
          .filter(pp -> pp.getTimestamp().isAfter(newerThan))
          .collect(Collectors.toList());
    }
  }

  @Override
  public Set<String> getKnownHumanReadableKeys() {
    return this.keyToCode.keySet();
  }

  @Override
  public SimplePatient addProperties(final String humanKey, final Code code,
      final List<PatientProperty> timeline) {

    this.keyToCode.put(humanKey, code);
    this.codeToValue.put(code, timeline);
    this.hrToValue.put(humanKey, timeline);

    return this;
  }

  @Override
  public GPMPatient registerPatientEvaluator(final String id,
      final PatientEvaluator patientEvaluator) {
    this.codeSystemToEvaluator.put(id, patientEvaluator);
    return this;
  }
}
