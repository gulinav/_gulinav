package de.ukaachen.gpm.core.util;

import de.ukaachen.gpm.core.model.patient.TimePhase;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class ExpectedTimePhaseBuilder {

  public Map<String, Map<String, TimePhase>> getExpectedTimePhasesPerPatient() {
    return expectedTimePhasesPerPatient;
  }

  private final Map<String, Map<String, TimePhase>> expectedTimePhasesPerPatient;

  private Consumer<String> forTimePhaseAdder;
  private String currentPatient;

  private ExpectedTimePhaseBuilder() {
    expectedTimePhasesPerPatient = new HashMap<>();
  }

  public Map<String, TimePhase> getExpectedTimePhases() {
    return expectedTimePhasesPerPatient.get(expectedTimePhasesPerPatient.keySet().iterator().next());
  }

  public static ExpectedTimePhaseBuilder forOnePatient() {
    final ExpectedTimePhaseBuilder result = new ExpectedTimePhaseBuilder();
    result.currentPatient = UUID.randomUUID().toString();
    return result;
  }

  public static ExpectedTimePhaseBuilder forInitialPatient(final String initialPatientId) {
    final ExpectedTimePhaseBuilder result = new ExpectedTimePhaseBuilder();
    result.currentPatient = initialPatientId;
    return result;
  }

  public ExpectedTimePhaseBuilder forPatient(final String patientId) {
    this.currentPatient = patientId;
    return this;
  }

  public ExpectedTimePhaseBuilder forTimePhase(final TimePhase timePhase) {
    this.forTimePhaseAdder = vertex -> expectedTimePhasesPerPatient
        .computeIfAbsent(currentPatient, (patient) -> new HashMap<>())
        .put(vertex, timePhase);
    return this;
  }

  public ExpectedTimePhaseBuilder addVertices(final String... vertices) {
    Arrays.asList(vertices).forEach(forTimePhaseAdder);
    return this;
  }

}
