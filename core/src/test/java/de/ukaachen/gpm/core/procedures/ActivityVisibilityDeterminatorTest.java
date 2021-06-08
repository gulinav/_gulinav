package de.ukaachen.gpm.core.procedures;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;

import java.io.File;
import java.util.Set;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.Test;

public class ActivityVisibilityDeterminatorTest {

  private BpmnModelInstance getModelForFile(final String fileName) {
    return Bpmn.readModelFromFile(new File(getClass().getResource(fileName).getFile()));
  }


  @Test
  public void testWhenGivenUnmarkedActivities_theyAreConsideredVisible() {
    // Given
    final BpmnModelInstance model =
        getModelForFile("activity-visibility-determinator-unmarked.bpmn");
    final ActivityVisibilityDeterminator cut = new ActivityVisibilityDeterminator(model);

    // When
    final Set<String> actual = cut.determineIdsOfInvisibleActivities();

    // Then
    assertThat(actual, is(empty()));
  }

  @Test
  public void testWhenGivenActivitiesMarkedVisible_theyAreConsideredVisible() {
    final BpmnModelInstance model =
        getModelForFile("activity-visibility-determinator-marked-visible.bpmn");
    final ActivityVisibilityDeterminator cut = new ActivityVisibilityDeterminator(model);

    // When
    final Set<String> actual = cut.determineIdsOfInvisibleActivities();

    // Then
    assertThat(actual, is(empty()));
  }

  @Test
  public void testWhenGivenActivitiesMarkedInvisible_theyAreConsideredInvisible() {
    final BpmnModelInstance model =
        getModelForFile("activity-visibility-determinator-marked-invisible.bpmn");
    final ActivityVisibilityDeterminator cut = new ActivityVisibilityDeterminator(model);

    // When
    final Set<String> actual = cut.determineIdsOfInvisibleActivities();

    // Then
    assertThat(actual, containsInAnyOrder("Task_1wym2j6", "Task_12wfwt1"));
  }

  @Test
  public void testWhenGivenBpmnWithVariableMarkedActivities_theCorrectOnesAreConsideredInvisible() {
    final BpmnModelInstance model =
        getModelForFile("activity-visibility-determinator-marked-variably.bpmn");
    final ActivityVisibilityDeterminator cut = new ActivityVisibilityDeterminator(model);

    // When
    final Set<String> actual = cut.determineIdsOfInvisibleActivities();

    // Then
    assertThat(actual, contains("Task_12wfwt1"));
  }
}
