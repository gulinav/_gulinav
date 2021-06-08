package de.ukaachen.gulinav.rest.model;


import de.ukaachen.gpm.core.model.patient.GuidelineItem;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class GuidelineItemPayload {

  @Getter
  @Setter
  private String taskId;

  @Getter
  @Setter
  private String timePhase;

  @Getter
  @Setter
  private String shortDesc;

  @Getter
  @Setter
  private String longDesc;

  @Getter
  @Setter
  private String performer;

  public GuidelineItemPayload(final GuidelineItem item) {
    this.taskId = item.getTaskId();
    this.timePhase = item.getTimePhase().name();
    this.shortDesc = item.getDescription();
    this.longDesc = item.getDetailText();
    this.performer = item.getPerformer().name();
  }

}
