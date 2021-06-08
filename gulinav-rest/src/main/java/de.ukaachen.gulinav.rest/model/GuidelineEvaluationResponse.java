package de.ukaachen.gulinav.rest.model;

import de.ukaachen.gpm.core.model.patient.GuidelineItem;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@ToString
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class GuidelineEvaluationResponse {

  @Getter
  @Setter
  private List<GuidelineItemPayload> guidelineItems;
}
