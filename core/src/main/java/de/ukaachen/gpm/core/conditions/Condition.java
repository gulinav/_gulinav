package de.ukaachen.gpm.core.conditions;

import de.ukaachen.gpm.core.model.patient.GPMPatient;
import java.util.Optional;

public interface Condition extends Cloneable {
  Condition and(final Condition condition);
  Condition or(final Condition condition);
  Optional<Boolean> evaluate(final GPMPatient patient);
  Object clone() throws CloneNotSupportedException;
}
