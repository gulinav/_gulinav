package de.ukaachen.gpm.core.conditions;

import de.ukaachen.gpm.core.model.patient.GPMPatient;
import java.util.Optional;

public class False implements Negatable, Condition {

  private static False instance;

  private False() {

  }

  public static False getInstance() {
    if(instance == null) {
      instance = new False();
    }

    return instance;
  }

  @Override
  public Condition and(final Condition condition) {
    return this;
  }

  @Override
  public Condition or(final Condition condition) {
    return condition;
  }

  @Override
  public Optional<Boolean> evaluate(final GPMPatient patient) {
    return Optional.of(false);
  }

  @Override
  public Condition invert() {
    return True.getInstance();
  }

  @Override
  public Object clone() throws CloneNotSupportedException {
    super.clone();
    // No need to clone an immutable singleton
    return instance;
  }

  @Override
  public String toString() {
    return Boolean.FALSE.toString();
  }
}
