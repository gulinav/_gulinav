package de.ukaachen.gulinav.rest.model;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@EqualsAndHashCode
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class EvaluationPayload {

  @Getter
  @Setter
  private String guidelineId;

  @Getter
  @Setter
  private List<PatientPayload> patientPayload;

}
