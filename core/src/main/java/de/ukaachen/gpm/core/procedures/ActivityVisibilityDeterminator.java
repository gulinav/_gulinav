package de.ukaachen.gpm.core.procedures;

import de.ukaachen.gpm.core.util.BpmnUtil;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.Task;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperties;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperty;

public class ActivityVisibilityDeterminator {

  private final BpmnModelInstance bpmnModelInstance;

  public ActivityVisibilityDeterminator(final BpmnModelInstance bpmnModelInstance) {
    this.bpmnModelInstance = bpmnModelInstance;
  }

  public Set<String> determineIdsOfInvisibleActivities() {
    final Collection<Task> tasks = bpmnModelInstance
        .getModelElementsByType(Task.class);

    final Set<String> result = new HashSet<>();

    for(final Task task: tasks) {
      final Optional<CamundaProperties> camundaProperties = BpmnUtil.instance()
          .getCamundaProperties(task);

      if(camundaProperties.isPresent()) {
        final Collection<CamundaProperty> theProperties =
            camundaProperties.get().getCamundaProperties();

        for(final CamundaProperty property: theProperties) {
          if(property.getCamundaName().equals("GPM:VISIBLE")
              && property.getCamundaValue().equalsIgnoreCase("FALSE")) {
            result.add(task.getId());
          }
        }
      }
    }

    return result;
  }
}
