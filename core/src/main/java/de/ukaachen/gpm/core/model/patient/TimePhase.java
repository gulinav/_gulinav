package de.ukaachen.gpm.core.model.patient;


import lombok.Getter;

public enum TimePhase {

  NOT_CLASSIFIABLE(0),
  UNREACHABLE(50),
  PAST(100),
  PRESENT(200),
  FUTURE(300);

  @Getter
  private final int position;

  TimePhase(final int position) {
    this.position = position;
  }

  public boolean comesAfter(final TimePhase timePhase) {
    return this.position < timePhase.position;
  }

  public boolean comesBefore(final TimePhase timePhase) {
    return timePhase.position < this.position;
  }
}
