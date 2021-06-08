package de.ukaachen.gpm.core.conditions;


import static de.ukaachen.gpm.core.conditions.Term.BinaryOperator.OR;
import static de.ukaachen.gpm.core.conditions.Term.BinaryOperator.AND;

import de.ukaachen.gpm.core.model.patient.GPMPatient;
import java.util.Optional;
import java.util.function.BiFunction;
import lombok.EqualsAndHashCode;
import lombok.ToString;


@EqualsAndHashCode
@ToString
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class Term implements Condition {

  public interface BinaryOperation {

    Optional<Boolean> eval(Optional<Boolean> left, Optional<Boolean> right);
  }

  public enum BinaryOperator implements BinaryOperation {
    AND((left, right) -> {
      if (left.isPresent()) {
        if (left.get()) {
          return right;
        } else {
          return left;
        }
      } else {
        if (right.isPresent() && !right.get()) {
          return right;
        } else {
          return left;
        }
      }
    }),
    OR((left, right) -> {
      if (left.isPresent()) {
        if (left.get()) {
          return left;
        } else {
          return right;
        }
      } else {
        if (right.isPresent()) {
          if (right.get()) {
            return right;
          }
        }

        return left;
      }
    });

    @Override
    public Optional<Boolean> eval(final Optional<Boolean> left, final Optional<Boolean> right) {
      return evaluator.apply(left, right);
    }

    BinaryOperator(final BiFunction<Optional<Boolean>, Optional<Boolean>, Optional<Boolean>> fnct) {
      evaluator = fnct;
    }

    private final BiFunction<Optional<Boolean>, Optional<Boolean>, Optional<Boolean>> evaluator;
  }

  private final Condition left;
  private final Condition right;
  private final BinaryOperator operator;


  private Term(final Condition left, final Condition right, final BinaryOperator operator) {
    this.left = left;
    this.right = right;
    this.operator = operator;
  }

  public static Condition and(final Condition con1, final Condition con2) {
    return new Term(con1, con2, AND);
  }

  public static Condition and(final String con1, final String con2) {
    return and(Literal.of(con1), Literal.of(con2));
  }

  public static Condition or(final Condition con1, final Condition con2) {
    return new Term(con1, con2, OR);
  }

  @Override
  public Condition and(final Condition condition) {
    return new Term(this, condition, AND);
  }

  @Override
  public Condition or(final Condition condition) {
    return new Term(this, condition, OR);
  }

  @Override
  public Optional<Boolean> evaluate(final GPMPatient patient) {
    final Optional<Boolean> leftResult = left.evaluate(patient);
    final Optional<Boolean> rightResult = right.evaluate(patient);

    return operator.eval(leftResult, rightResult);
  }

  @Override
  public Term clone() throws CloneNotSupportedException {
    super.clone();
    return new Term((Condition) left.clone(), (Condition) right.clone(), operator);
  }
}
