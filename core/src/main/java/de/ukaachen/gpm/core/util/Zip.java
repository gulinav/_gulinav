package de.ukaachen.gpm.core.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public class Zip {

  public static <A, B, C> List<C> zip(final List<A> a, final List<B> b,
      final BiFunction<A, B, C> fnct) {
    if (a.size() != b.size()) {
      throw new IllegalArgumentException("Lists have to have same size.");
    }

    final List<C> result = new ArrayList<>(a.size());

    for (int i = 0; i < a.size(); i++) {
      result.add(fnct.apply(a.get(i), b.get(i)));
    }

    return result;
  }

  public static <A, B> void consumeZip(final List<A> a, final List<B> b, final BiConsumer<A, B> fnct) {
    if (a.size() != b.size()) {
      throw new IllegalArgumentException("Lists have to have same size.");
    }

    for (int i = 0; i < a.size(); i++) {
      fnct.accept(a.get(i), b.get(i));
    }
  }

}
