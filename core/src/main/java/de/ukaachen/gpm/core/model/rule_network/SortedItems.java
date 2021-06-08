package de.ukaachen.gpm.core.model.rule_network;

import com.google.common.collect.ImmutableList;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;


/**
 * List of guideline-item ids that is supposed to be sorted. This is just the wrapper class
 */
@EqualsAndHashCode
@ToString
public final class SortedItems {

  @Getter
  private final ImmutableList<String> vertexIds;

  public SortedItems(final List<String> vertexIds) throws IllegalArgumentException {
    if (vertexIds == null) {
      throw new IllegalArgumentException("Given vertexIds must not be null");
    }

    this.vertexIds = ImmutableList.copyOf(vertexIds);
  }
}
