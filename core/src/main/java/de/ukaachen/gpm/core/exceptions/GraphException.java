package de.ukaachen.gpm.core.exceptions;

import java.util.function.Supplier;

public class GraphException extends RuntimeException {

  public GraphException(final Supplier<String> message) {
    super(message.get());
  }

}
