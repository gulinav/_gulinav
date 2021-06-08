package de.ukaachen.gpm.core.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class DependencyMapBuilder<T> {

  public Map<T, Collection<T>> getDependencyMap() {
    return dependencyMap;
  }

  private final Map<T, Collection<T>> dependencyMap;
  private T currentItem;

  public DependencyMapBuilder() {
    dependencyMap = new HashMap<>();
  }

  public DependencyMapBuilder<T> item(final T item) {
    this.currentItem = item;
    return this;
  }

  public DependencyMapBuilder<T> dependsOnNothing() {
    dependencyMap.put(currentItem, Collections.emptySet());
    return this;
  }

  @SuppressWarnings("unchecked") // Only used in tests
  public DependencyMapBuilder<T> dependsOn(final T... dependingItems) {
    dependencyMap.computeIfAbsent(currentItem, (k) -> new HashSet<>())
        .addAll(Arrays.asList(dependingItems));
    return this;
  }

  public static <T> void assertDependencies(final List<T> actualGuidelineItems,
      final Map<T, Collection<T>> dependencyMap) {
    final Map<T, Integer> itemToIndex = new HashMap<>();
    int i = 0;
    for (final T item : actualGuidelineItems) {
      itemToIndex.put(item, i);
      i++;
    }

    for (final T dependingItem : actualGuidelineItems) {
      for (final T dependency : dependencyMap.get(dependingItem)) {
        assertThat(actualGuidelineItems.indexOf(dependingItem),
            is(greaterThan(itemToIndex.get(dependency))));
      }
    }
  }
}
