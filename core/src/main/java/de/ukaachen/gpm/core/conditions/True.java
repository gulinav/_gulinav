package de.ukaachen.gpm.core.conditions;

import de.ukaachen.gpm.core.model.patient.GPMPatient;
import java.util.Optional;

public class True implements Condition, Negatable, Cloneable {

  private static True instance;

  private True() {

  }

  public static True getInstance() {
    if(instance == null) {
      instance = new True();
    }
    return instance;
  }

  @Override
  public Condition and(final Condition condition) {
    return condition;
  }

  @Override
  public Condition or(final Condition condition) {
    return this;
  }

  @Override
  public Optional<Boolean> evaluate(final GPMPatient patient) {
    return Optional.of(true);
  }

  @Override
  public Condition invert() {
    return False.getInstance();
  }

  @Override
  public Object clone() throws CloneNotSupportedException {
    super.clone();
    // No need to clone an immutable singleton
    return instance;
  }

  @Override
  public String toString() {
    return Boolean.TRUE.toString();
  }
}
