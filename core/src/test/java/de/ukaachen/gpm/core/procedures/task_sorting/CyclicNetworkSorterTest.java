package de.ukaachen.gpm.core.procedures.task_sorting;


import static de.ukaachen.gpm.core.util.VertexUtil.*;
import static de.ukaachen.gpm.core.util.TestConstants.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertEquals;


import de.ukaachen.gpm.core.model.rule_network.RuleNetwork;
import de.ukaachen.gpm.core.model.rule_network.RuleNetworkBuilder;
import de.ukaachen.gpm.core.model.rule_network.TaskDetailTexts;
import de.ukaachen.gpm.core.procedures.RuleNetworkCreator;
import de.ukaachen.gpm.core.util.Zip;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.Test;

public class CyclicNetworkSorterTest {

  @Test
  public void testWhenNetworkContainsACycle_itIsProcessedCorrectly() {
    final RuleNetwork ruleNetwork = new RuleNetworkBuilder()
        .addVertices(start(S), v(A), v(B), v(C), v(D), v(E), end(T))
        .addEdge(P, S, A)
        .addEdge(Q, S, B)
        .addEdge(R, A, D)
        .addEdge(S, A, E)
        .addEdge(T, D, C)
        .addEdge(U, C, D)
        .addEdge(V, D, E)
        .addEdge(W, E, C)
        .addEdge(X, E, T)
        .addEdge(Y, C, A)
        .addEdge(Z, B, C)
        .build();

    // When
    final List<String> actual =
        new CyclicNetworkSorter(ruleNetwork, GPMSorter::new).sort().getVertexIds();

    // Then
    final List<String> expected = Arrays.asList(S, A, B, C, D, E, T);
    assertEquals(expected.size(), actual.size());
    Zip.consumeZip(actual, expected, (a, e) -> assertThat(a, is(equalTo(e))));
  }

  @Test
  public void testComplexCycleThing() {
    final RuleNetwork ruleNetwork = getRuleNetworkFromFile("time-classifier-cycle-complex.bpmn");

    // When
    final List<String> actual =
        new CyclicNetworkSorter(ruleNetwork, GPMSorter::new).sort().getVertexIds();

    // Then
    final List<String> expected = Arrays.asList(S, C, A, H, D, E, B, T, G, F);
    assertEquals(expected.size(), actual.size());
    Zip.consumeZip(actual, expected, (a, e) -> assertThat(a, is(equalTo(e))));
  }

  @SuppressWarnings("SameParameterValue")
  private RuleNetwork getRuleNetworkFromFile(final String fileName) {
    final File file = new File(getClass().getResource("../" + fileName).getFile());
    final BpmnModelInstance bpmnModelInstance = Bpmn.readModelFromFile(file);

    return new RuleNetworkCreator(bpmnModelInstance, new RuleNetworkBuilder(), new TaskDetailTexts())
        .create();
  }
}
