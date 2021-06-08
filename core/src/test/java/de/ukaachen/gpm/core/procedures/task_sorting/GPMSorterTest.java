package de.ukaachen.gpm.core.procedures.task_sorting;

import static de.ukaachen.gpm.core.util.VertexUtil.*;
import static de.ukaachen.gpm.core.util.TestConstants.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertEquals;

import de.ukaachen.gpm.core.exceptions.CyclicGraphException;
import de.ukaachen.gpm.core.model.rule_network.RuleNetwork;
import de.ukaachen.gpm.core.model.rule_network.RuleNetworkBuilder;
import de.ukaachen.gpm.core.model.rule_network.Vertex;
import de.ukaachen.gpm.core.model.rule_network.VertexType;
import de.ukaachen.gpm.core.util.Zip;
import java.util.Arrays;
import java.util.List;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class GPMSorterTest {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Test
  public void testWhenNetworkHasCycle_exceptionIsThrown() {
    expectedException.expect(CyclicGraphException.class);

    final RuleNetwork ruleNetwork = new RuleNetworkBuilder()
        .addVertices(v(A), v(B))
        .addEdge(X, A, B)
        .addEdge(Y, B, A)
        .build();

    // Should throw exception
    new GPMSorter(ruleNetwork).sort();
  }

  @Test
  public void testWithExample01_correctOrderIsFound() {
    // Given
    final RuleNetwork ruleNetwork = new RuleNetworkBuilder()
        .addVertices(v(A), v(X), v(C), v(D), v(E), v(F), v(H), v(I), v(G))
        .addEdge(Q, A, X)
        .addEdge(R, X, D)
        .addEdge(S, A, E)
        .addEdge(T, X, C)
        .addEdge(U, H, I)
        .addEdge(V, E, F)
        .addEdge(W, F, G)
        .addEdge(X, F, H)
        .build();

    // When
    final List<String> actual = new GPMSorter(ruleNetwork).sort().getVertexIds();

    // Then
    final List<String> expected = Arrays.asList(A, X, C, D, E, F, G, H, I);
    assertThat(actual.size(), is(expected.size()));
    Zip.consumeZip(actual, expected, (a, e) -> assertThat(a, is(equalTo(e))));
  }

  @Test
  public void testWithExample02_correctOrderIsFound() {
    // Given
    final RuleNetwork ruleNetwork = new RuleNetworkBuilder()
        .addVertices(start(A), v(B), v(C), v(D), v(E), v(F), v(G))
        .addEdge(P, A, B)
        .addEdge(Q, A, C)
        .addEdge(R, A, D)
        .addEdge(S, D, E)
        .addEdge(T, C, G)
        .addEdge(U, E, F)
        .build();

    // When
    final List<String> actual = new GPMSorter(ruleNetwork).sort().getVertexIds();

    // Then
    final List<String> expected = Arrays.asList(A, B, C, G, D, E, F);
    assertEquals(actual.size(), expected.size());
    Zip.consumeZip(actual, expected, (a, e) -> assertThat(a, is(equalTo(e))));
  }

  @Test
  public void testSecondaryCriterionIsVertexId() {
    // Given
    final RuleNetwork ruleNetwork = new RuleNetworkBuilder()
        .addVertices(v(D), v(B), v(C))
        .addEdge(P, D, C)
        .addEdge(Q, D, B)
        .build();

    // When
    final List<String> actual = new GPMSorter(ruleNetwork).sort().getVertexIds();

    // Then
    final List<String> expected = Arrays.asList(D, B, C);
    assertEquals(actual.size(), expected.size());
    Zip.consumeZip(actual, expected, (a, e) -> assertThat(a, is(equalTo(e))));
  }

  @Test
  public void testOnlyActivitiesAreCountedForPathLength() {
    // Given
    final Vertex gateway = Vertex.withId(GATEWAY_01).type(VertexType.PARALLEL_GATEWAY).build();
    final RuleNetwork ruleNetwork = new RuleNetworkBuilder()
        .addVertices(gateway, v(A), v(B), v(C), v(D), v(E), v(F), v(G), v(H))
        .addEdge(J, A, GATEWAY_01)
        .addEdge(K, A, C)
        .addEdge(L, GATEWAY_01, B)
        .addEdge(M, GATEWAY_01, D)
        .addEdge(N, B, E)
        .addEdge(O, C, F)
        .addEdge(P, F, H)
        .addEdge(Q, D, G)
        .build();

    // When
    final List<String> actual = new GPMSorter(ruleNetwork).sort().getVertexIds();

    // Then
    final List<String> expected = Arrays.asList(A, GATEWAY_01, B, E, D, G, C, F, H);
    assertEquals(actual.size(), expected.size());
    Zip.consumeZip(actual, expected, (a, e) -> assertThat(a, is(equalTo(e))));
  }

  // This behavior is not critical for the app and is disabled atm, since there are more urgent
  // matters
  @Test
  @Ignore
  public void testWhenVerticesMergeIntoTheSameGateway_thenTheyComeDirectlyAfterOneAnotherInTheSorting() {
    // Given
    final Vertex gateway = Vertex.withId(GATEWAY_01).type(VertexType.PARALLEL_GATEWAY).build();
    final RuleNetwork ruleNetwork = new RuleNetworkBuilder()
        .addVertices(gateway, v(A), v(B), v(C), v(D), v(E), v(F), v(G), v(X))
        .addEdge(P, A, B)
        .addEdge(Q, A, C)
        .addEdge(Z, C, X)
        .addEdge(R, B, D)
        .addEdge(U, C, GATEWAY_01)
        .addEdge(S, D, E)
        .addEdge(T, E, GATEWAY_01)
        .addEdge(V, GATEWAY_01, F)
        .addEdge(W, F, G)
        .build();

    // When
    final List<String> actual = new GPMSorter(ruleNetwork).sort().getVertexIds();

    // Then
    final List<String> expected = Arrays.asList(A, B, D, E, C, GATEWAY_01, X, F, G);
    assertEquals(expected.size(), actual.size());
    Zip.consumeZip(actual, expected, (a, e) -> assertThat(a, is(equalTo(e))));
  }
}
