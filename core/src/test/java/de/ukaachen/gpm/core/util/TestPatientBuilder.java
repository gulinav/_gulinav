package de.ukaachen.gpm.core.util;

import de.ukaachen.gpm.core.model.patient.GPMPatient;
import de.ukaachen.gpm.mks.model.Code;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TestPatientBuilder {

  private final Map<String, GPMPatientMock> testPatients;
  private String currentPatient;
  private boolean singlePatient = false;

  private TestPatientBuilder() {
    testPatients = new HashMap<>();
  }


  public static TestPatientBuilder initForPatient(final String firstPatientId) {
    final TestPatientBuilder result = new TestPatientBuilder();
    result.forTestPatient(firstPatientId);
    return result;
  }

  public static TestPatientBuilder initForOnePatient() {
    final TestPatientBuilder result = new TestPatientBuilder();
    result.forTestPatient(UUID.randomUUID().toString());
    result.singlePatient = true;
    return result;
  }

  public TestPatientBuilder forTestPatient(final String patientId) {
    if (singlePatient) {
      throw new IllegalStateException("Builder was initialized for only one patient!");
    }
    testPatients.putIfAbsent(patientId, new GPMPatientMock());
    currentPatient = patientId;
    return this;
  }

  public TestPatientBuilder property(final String key, final String code, final String value) {
    final Code theCode = new Code(code);
    testPatients.get(currentPatient).putPatientProperty(theCode, value);
    testPatients.get(currentPatient)
        .addCodeMapping(key, code);
    return this;
  }

  public GPMPatient getPatient() {
    if (!singlePatient) {
      throw new IllegalStateException("Builder was invert initialized for only one patient!");
    }
    return testPatients.get(testPatients.keySet().iterator().next());
  }

  public Map<String, GPMPatient> getTestPatients() {
    final Map<String, GPMPatient> result = new HashMap<>();
    testPatients.forEach(result::put);
    return result;
  }
}
