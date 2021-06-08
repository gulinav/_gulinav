package de.ukaachen.gpm.core.procedures.task_sorting;

import static de.ukaachen.gpm.core.util.VertexUtil.*;
import static de.ukaachen.gpm.core.util.TestConstants.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.core.IsEqual.equalTo;


import de.ukaachen.gpm.core.conditions.True;
import de.ukaachen.gpm.core.model.patient.Performer;
import de.ukaachen.gpm.core.model.rule_network.Edge;
import de.ukaachen.gpm.core.model.rule_network.RuleNetwork;
import de.ukaachen.gpm.core.model.rule_network.RuleNetworkBuilder;
import de.ukaachen.gpm.core.model.rule_network.SortedItems;
import de.ukaachen.gpm.core.model.rule_network.TaskDetailTexts;
import de.ukaachen.gpm.core.model.rule_network.Vertex;
import de.ukaachen.gpm.core.model.rule_network.VertexType;
import de.ukaachen.gpm.core.procedures.RuleNetworkCreator;
import de.ukaachen.gpm.core.util.DependencyMapBuilder;
import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.Test;

public class RuleNetworkTopologicalSorterKahnsAlgorithmTest {

  @Test
  public void testWhenGivenPath_pathIsReturned() {
    // Given
    final Vertex a = Vertex.withId(A).type(VertexType.START_EVENT).build();
    final Vertex b = Vertex.withId(B).type(VertexType.ACTIVITY).performer(Performer.HUMAN).build();
    final Vertex c = Vertex.withId(C).type(VertexType.END_EVENT).build();
    final Set<Vertex> vertices = Stream.of(a, b, c).collect(Collectors.toSet());
    final Set<Edge> edges = Stream
        .of(new Edge(X, a, b, True.getInstance()),
            new Edge(Y, b, c, True.getInstance()))
        .collect(Collectors.toSet());
    final RuleNetwork ruleNetwork = new RuleNetwork(edges, vertices);

    // When
    final SortedItems actual = new RuleNetworkTopologicalSorterKahnsAlgorithm(ruleNetwork).sort();

    // Then
    assertThat(actual.getVertexIds().size(), is(3));
    assertThat(actual.getVertexIds().get(0), is(equalTo(A)));
    assertThat(actual.getVertexIds().get(1), is(equalTo(B)));
    assertThat(actual.getVertexIds().get(2), is(equalTo(C)));
  }

  @Test
  public void testWhenGivenWcp03Sync_correctOrderIsReturned() {
    // Given
    final BpmnModelInstance model = Bpmn.readModelFromFile(
        new File(getClass().getResource("../patterns/wcp-03-synch.bpmn").getFile()));
    final RuleNetwork ruleNetwork =
        new RuleNetworkCreator(model, new RuleNetworkBuilder(), new TaskDetailTexts()).create();

    final Map<String, Collection<String>> dependencyMap = new DependencyMapBuilder<String>()
        .item(S).dependsOnNothing()
        .item(A).dependsOn(S)
        .item(B).dependsOn(S)
        .item(GATEWAY_01).dependsOn(A, B)
        .item(C).dependsOn(GATEWAY_01)
        .item(T).dependsOn(A, B, C)
        .getDependencyMap();

    // When
    final SortedItems actual = new RuleNetworkTopologicalSorterKahnsAlgorithm(ruleNetwork).sort();

    // Then
    DependencyMapBuilder.assertDependencies(actual.getVertexIds(), dependencyMap);
  }

  @Test
  public void testWhenGivenNetworkWithSplitOfDifferentLength_theShorterRemainingPartComesFirst() {
    // Given
    final RuleNetwork ruleNetwork = new RuleNetworkBuilder()
        .addVertices(start(A), v(X), v(C), v(D), v(E), v(F), v(G), v(H), v(I))
        .addEdge(R, E, F)
        .addEdge(S, A, E)
        .addEdge(V, F, G)
        .addEdge(T, F, H)
        .addEdge(U, H, I)
        .addEdge(P, A, X)
        .addEdge(Q, X, C)
        .addEdge(W, X, D)
        .build();

    final Map<String, Collection<String>> dependencyMap = new DependencyMapBuilder<String>()
        .item(A).dependsOnNothing()
        .item(X).dependsOn(A)
        .item(G).dependsOn(F)
        .item(C).dependsOn(X)
        .item(D).dependsOn(X)
        .item(E).dependsOn(A)
        .item(F).dependsOn(E)
        .item(H).dependsOn(F)
        .item(I).dependsOn(H)
        .getDependencyMap();

    // When
    final List<String> actual =
        new RuleNetworkTopologicalSorterKahnsAlgorithm(ruleNetwork).sort().getVertexIds();

    // Then
    DependencyMapBuilder.assertDependencies(actual, dependencyMap);

    assertThat(actual.indexOf(X), is(lessThan(actual.indexOf(E))));
    assertThat(actual.indexOf(C), is(lessThan(actual.indexOf(E))));
    assertThat(actual.indexOf(D), is(lessThan(actual.indexOf(E))));
    assertThat(actual.indexOf(G), is(lessThan(actual.indexOf(H))));
  }


}
