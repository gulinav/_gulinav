package de.ukaachen.gpm.core.model.patient;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import de.ukaachen.gpm.mks.model.Code;
import de.ukaachen.gpm.mks.model.PatientProperty;
import java.util.Collections;
import java.util.List;
import lombok.Getter;

public class PatientData {

  @Getter
  private final ListMultimap<Code, PatientProperty> codeToProperties;

  public PatientData() {
    this.codeToProperties = ArrayListMultimap.create();
  }

  public void add(final Code code, final PatientProperty patientProperty) {
    this.add(code, Collections.singletonList(patientProperty));
  }

  public void add(final Code code, final List<PatientProperty> patientProperties) {
    this.codeToProperties.putAll(code, patientProperties);
  }

  public List<PatientProperty> getProperties(final Code code) {
    return this.codeToProperties.get(code);
  }
}
