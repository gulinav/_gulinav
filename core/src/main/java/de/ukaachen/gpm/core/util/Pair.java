package de.ukaachen.gpm.core.util;

import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * This is a mere utility class for having more readable Pairs
 */
@ToString
@EqualsAndHashCode
public class Pair<T, E> {

  private final T first;
  private final E second;

  private Pair(final T first, final E second) {
    this.first = first;
    this.second = second;
  }

  public T f() {
    return first;
  }

  public E s() {
    return second;
  }

  public static <T, E> Pair<T, E> create(final T first, final E second) {

    return new Pair<>(first, second);
  }
}
