package de.ukaachen.gulinav.rest.model;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class PatientPropertyPayload {

  @Getter
  @Setter
  private Instant time;

  @Getter
  @Setter
  private Object value;

}
