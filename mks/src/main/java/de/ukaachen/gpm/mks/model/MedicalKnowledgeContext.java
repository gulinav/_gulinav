package de.ukaachen.gpm.mks.model;

import java.time.Duration;
import java.util.List;

public interface MedicalKnowledgeContext {

  List<PatientProperty> provideValue(final String k, final Duration interval);

}
