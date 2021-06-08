package de.ukaachen.gpm.core.procedures.task_sorting;


import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static de.ukaachen.gpm.core.util.TestConstants.*;

import static de.ukaachen.gpm.core.util.VertexUtil.*;

import de.ukaachen.gpm.core.model.rule_network.RuleNetwork;
import de.ukaachen.gpm.core.model.rule_network.RuleNetworkBuilder;
import de.ukaachen.gpm.core.util.DependencyMapBuilder;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;

public class RuleNetworkTopologicalSorterRetainPathSegmentsTest {

  @Test
  public void testWhenGivenTreeNetwork_correctTopologicalOrderIsReturned() {
    // Given
    final RuleNetwork ruleNetwork = new RuleNetworkBuilder()
        .addVertices(start(A), v(B), v(C), v(D), v(E), v(F), v(K), v(L), v(G))
        .addEdge(R, A, C)
        .addEdge(S, C, D)
        .addEdge(T, A, E)
        .addEdge(U, E, F)
        .addEdge(V, F, L)
        .addEdge(W, E, K)
        .addEdge(X, K, G)
        .build();

    final Map<String, Collection<String>> dependencyMap = new DependencyMapBuilder<String>()
        .item(A).dependsOnNothing()
        .item(C).dependsOn(A)
        .item(E).dependsOn(A)
        .item(D).dependsOn(C)
        .item(F).dependsOn(E)
        .item(K).dependsOn(E)
        .item(L).dependsOn(F)
        .item(G).dependsOn(K)
        .getDependencyMap();

    // When
    final List<String> actual =
        new RuleNetworkTopologicalSorterRetainPathSegments(ruleNetwork).sort().getVertexIds();

    // Then
    // This asserts Kahn's algorithm..
    DependencyMapBuilder.assertDependencies(actual, dependencyMap);

    // This asserts the improvement that certain items have to come after each other
    assertThat(actual.indexOf(C), is(actual.indexOf(D) - 1));
    assertThat(actual.indexOf(F), is(actual.indexOf(L) - 1));
    assertThat(actual.indexOf(K), is(actual.indexOf(G) - 1));
  }

  @Test
  public void testWhenGivenAcyclicGraph_correctTopologicalOrderIsReturned() {
    // Given
    final RuleNetwork ruleNetwork = new RuleNetworkBuilder()
        .addVertices(start(B), v(G), v(D), v(A), v(F), v(E), v(C), v(X))
        .addEdge(R, B, G)
        .addEdge(S, B, D)
        .addEdge(T, G, A)
        .addEdge(U, D, X)
        .addEdge(Y, X, A)
        .addEdge(V, A, F)
        .addEdge(W, A, E)
        .addEdge(Y, E, C)
        .build();

    final Map<String, Collection<String>> dependencyMap = new DependencyMapBuilder<String>()
        .item(B).dependsOnNothing()
        .item(G).dependsOn(B)
        .item(D).dependsOn(B)
        .item(A).dependsOn(G, X)
        .item(F).dependsOn(A)
        .item(E).dependsOn(A)
        .item(C).dependsOn(E)
        .item(X).dependsOn(D)
        .getDependencyMap();

    // When
    final List<String> actual = new RuleNetworkTopologicalSorterRetainPathSegments(ruleNetwork)
        .sort()
        .getVertexIds();

    // Then
    // Assert Kahn's algorithm
    DependencyMapBuilder.assertDependencies(actual, dependencyMap);

    // This asserts the improvement that certain items have to come after each other
    assertThat(actual.indexOf(D), is(actual.indexOf(X) - 1));
    assertThat(actual.indexOf(C), is(actual.indexOf(E) + 1));
  }

  @Test
  public void testThenGivenInterleavedSorting_theInterleavingIsProperlyUnzipped() {
    // Given
    final RuleNetwork ruleNetwork = new RuleNetworkBuilder()
        .addVertices(start(A), v(B), v(C), v(D), v(E), v(F), v(G), v(H))
        .addEdge(I, A, F)
        .addEdge(J, F, C)
        .addEdge(K, C, D)
        .addEdge(L, A, E)
        .addEdge(M, E, B)
        .addEdge(N, B, G)
        .addEdge(O, G, H)
        .build();

    final List<String> topologicalSorting = Stream.of(A, F, E, C, B, G, D, H)
        .collect(Collectors.toList());

    final Map<String, Collection<String>> dependencyMap = new DependencyMapBuilder<String>()
        .item(A).dependsOnNothing()
        .item(F).dependsOn(A)
        .item(C).dependsOn(F)
        .item(D).dependsOn(C)
        .item(E).dependsOn(A)
        .item(B).dependsOn(E)
        .item(G).dependsOn(B)
        .item(H).dependsOn(G)
        .getDependencyMap();

    // When
    final List<String> actual = new RuleNetworkTopologicalSorterRetainPathSegments(ruleNetwork)
        .reorderTopologicalSorting(topologicalSorting);

    // Then
    DependencyMapBuilder.assertDependencies(actual, dependencyMap);
    assertThat(actual.indexOf(F), is(actual.indexOf(C) - 1));
    assertThat(actual.indexOf(C), is(actual.indexOf(D) - 1));
    assertThat(actual.indexOf(E), is(actual.indexOf(B) - 1));
    assertThat(actual.indexOf(B), is(actual.indexOf(G) - 1));
    assertThat(actual.indexOf(G), is(actual.indexOf(H) - 1));
  }


}
