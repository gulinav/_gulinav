package de.ukaachen.gulinav.rest.model;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class PatientPayload {

  @Getter
  @Setter
  private String code;

  @Getter
  @Setter
  private List<PatientPropertyPayload> values;
}
