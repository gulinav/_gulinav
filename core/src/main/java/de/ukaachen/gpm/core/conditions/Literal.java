package de.ukaachen.gpm.core.conditions;

import de.odysseus.el.ExpressionFactoryImpl;
import de.odysseus.el.util.SimpleContext;
import de.ukaachen.gpm.core.model.patient.GPMPatient;
import de.ukaachen.gpm.mks.model.PatientProperty;
import java.util.Optional;
import javax.el.ExpressionFactory;
import javax.el.PropertyNotFoundException;
import javax.el.ValueExpression;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode
public class Literal implements Condition, Negatable, Cloneable {

  @Getter
  private final String condition;

  private static final ExpressionFactory factory = new ExpressionFactoryImpl();

  @Getter
  private final boolean isNot;

  private Literal(final String condition) {
    this(condition, false);
  }

  private Literal(final String condition, final boolean isNot) {
    this.condition = condition;
    this.isNot = isNot;
  }

  public static Literal of(final String condition) {
    return new Literal(condition);
  }

  @Override
  public Condition and(final Condition condition) {
    return Term.and(this, condition);
  }

  @Override
  public Condition or(final Condition condition) {
    return Term.or(this, condition);
  }

  @Override
  public Condition invert() {
    return new Literal(this.condition, !isNot);
  }

  @Override
  public Optional<Boolean> evaluate(final GPMPatient patient) {
    final SimpleContext context = new SimpleContext();

    patient.getKnownHumanReadableKeys().forEach(key -> {
      final PatientProperty patientProp = patient.findByHumanReadableKey(key);
      if(patientProp != null && patientProp.getValue() != null) {
        context.setVariable(key, factory.createValueExpression(patientProp.getValue(), Object.class));
      }
    });

    final ValueExpression valueExpression = factory.createValueExpression(context, condition,
        Boolean.class);

    try {
      final Boolean evaluationResult = (Boolean) valueExpression.getValue(context);
      if (isNot) {
        return Optional.of(!evaluationResult);
      } else {
        return Optional.of(evaluationResult);
      }
    } catch (final PropertyNotFoundException e) {
      return Optional.empty();
    }
  }

  @Override
  public String toString() {
    if (isNot) {
      return "!(" + condition + ")";
    } else {
      return condition;
    }
  }

  @Override
  public Literal clone() throws CloneNotSupportedException {
    super.clone();
    return new Literal(condition, isNot);
  }
}
