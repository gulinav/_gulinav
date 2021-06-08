package de.ukaachen.gulinav.rest.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;


@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class GuidelinePayload {

  @Getter
  @Setter
  private String id;
  @Getter
  @Setter
  private String guideline;

}
