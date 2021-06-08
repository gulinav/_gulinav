package de.ukaachen.gpm.core.model.patient;

import de.ukaachen.gpm.mks.model.Code;
import de.ukaachen.gpm.mks.model.MedicalKnowledgeContext;
import de.ukaachen.gpm.mks.model.PatientProperty;
import java.time.Duration;
import java.util.Collections;
import java.util.List;

public class GPMMedicalKnowledgeContext implements MedicalKnowledgeContext {

  private final GPMPatient patient;

  public GPMMedicalKnowledgeContext(final GPMPatient patient) {
    this.patient = patient;
  }

  @Override
  public List<PatientProperty> provideValue(final String key, final Duration interval) {
    // The MKS uses colon-separated strings as keys, so we have to map to our representation here.
    final String[] mkKey = key.split(":");
    final Code gpmKey = new Code(mkKey[0], mkKey[1]);

    final List<PatientProperty> result = patient.findTimelineByCode(gpmKey, interval);

    return result != null ? result : Collections.emptyList();
  }
}
