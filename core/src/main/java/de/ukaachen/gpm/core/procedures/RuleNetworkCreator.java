package de.ukaachen.gpm.core.procedures;


import static de.ukaachen.gpm.core.model.rule_network.VertexType.ACTIVITY;
import static de.ukaachen.gpm.core.model.rule_network.VertexType.END_EVENT;
import static de.ukaachen.gpm.core.model.rule_network.VertexType.EXCLUSIVE_GATEWAY;
import static de.ukaachen.gpm.core.model.rule_network.VertexType.PARALLEL_GATEWAY;
import static de.ukaachen.gpm.core.model.rule_network.VertexType.START_EVENT;

import de.ukaachen.gpm.core.conditions.Condition;
import de.ukaachen.gpm.core.conditions.Literal;
import de.ukaachen.gpm.core.conditions.True;
import de.ukaachen.gpm.core.model.patient.Performer;
import de.ukaachen.gpm.core.model.rule_network.RuleNetwork;
import de.ukaachen.gpm.core.model.rule_network.RuleNetworkBuilder;
import de.ukaachen.gpm.core.model.rule_network.TaskDetailTexts;
import de.ukaachen.gpm.core.model.rule_network.Vertex;
import de.ukaachen.gpm.core.model.rule_network.VertexType;
import de.ukaachen.gpm.core.util.BpmnUtil;
import de.ukaachen.gpm.core.util.Pair;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.log4j.Log4j2;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.Activity;
import org.camunda.bpm.model.bpmn.instance.Association;
import org.camunda.bpm.model.bpmn.instance.BaseElement;
import org.camunda.bpm.model.bpmn.instance.ConditionExpression;
import org.camunda.bpm.model.bpmn.instance.EndEvent;
import org.camunda.bpm.model.bpmn.instance.ExclusiveGateway;
import org.camunda.bpm.model.bpmn.instance.FlowNode;
import org.camunda.bpm.model.bpmn.instance.ParallelGateway;
import org.camunda.bpm.model.bpmn.instance.SequenceFlow;
import org.camunda.bpm.model.bpmn.instance.ServiceTask;
import org.camunda.bpm.model.bpmn.instance.StartEvent;
import org.camunda.bpm.model.bpmn.instance.TextAnnotation;
import org.camunda.bpm.model.bpmn.instance.UserTask;

@Log4j2
public class RuleNetworkCreator {

  private final BpmnModelInstance bpmnModelInstance;

  private final RuleNetworkBuilder ruleNetworkBuilder;
  private final TaskDetailTexts detailTextsByTask;

  private boolean createMethodHasBeenCalled = false;

  public RuleNetworkCreator(final BpmnModelInstance bpmnModelInstance,
      final RuleNetworkBuilder ruleNetworkBuilder,
      final TaskDetailTexts detailTextsByTask) {
    this.bpmnModelInstance = bpmnModelInstance;
    this.ruleNetworkBuilder = ruleNetworkBuilder;
    this.detailTextsByTask = detailTextsByTask;
  }

  /**
   * Converts the BPM-Model from the file that was specified during instantiation into the internal
   * ruleNetwork model.
   * @return The internal ruleNetwork model that represents the BPM-Model from the given bpmn-file.
   * @throws IllegalStateException When the create method is called multiple times for the same
   * instance of the RuleNetworkCreator.
   */
  public RuleNetwork create() {
    if (createMethodHasBeenCalled) {
      throw new IllegalStateException("The create method has already been called on this instance"
          + " of RuleNetworkCreator.");
    }

    final Collection<FlowNode> vertexFlowNodes = new HashSet<>(
        bpmnModelInstance.getModelElementsByType(Activity.class));

    final Map<String, String> activityIdToDescription = getActivityDescriptions(vertexFlowNodes);

    vertexFlowNodes.addAll(bpmnModelInstance.getModelElementsByType(StartEvent.class));
    vertexFlowNodes.addAll(bpmnModelInstance.getModelElementsByType(EndEvent.class));
    vertexFlowNodes.addAll(bpmnModelInstance.getModelElementsByType(ParallelGateway.class));
    vertexFlowNodes.addAll(bpmnModelInstance.getModelElementsByType(ExclusiveGateway.class));

    vertexFlowNodes.forEach(
        flowNode -> addVertex(flowNode, activityIdToDescription.get(flowNode.getId())));
    vertexFlowNodes.forEach(this::addEdges);

    createMethodHasBeenCalled = true;
    return ruleNetworkBuilder.build();
  }

  private void addVertex(final BaseElement element, final String description) {
    final VertexType type;
    final Performer performer;

    final Pair<String, String> handledProperty =
        BpmnUtil.instance().getHandledProperty(element).orElse(null);

    if (element instanceof ServiceTask) {
      type = ACTIVITY;
      performer = Performer.DEVICE;
    } else if (element instanceof UserTask) {
      type = ACTIVITY;
      performer = Performer.HUMAN;
    } else if (element instanceof ParallelGateway) {
      type = PARALLEL_GATEWAY;
      performer = Performer.NONE;
    } else if (element instanceof ExclusiveGateway) {
      type = EXCLUSIVE_GATEWAY;
      performer = Performer.NONE;
    } else if (element instanceof EndEvent) {
      type = END_EVENT;
      performer = Performer.NONE;
    } else if (element instanceof StartEvent) {
      type = START_EVENT;
      performer = Performer.NONE;
    } else {
      throw new IllegalStateException("Unexpected vertex type: " + element.getClass().getName());
    }

    final String detailText = detailTextsByTask.getTaskToText().get(element.getId());

    final Vertex vertex = Vertex.withId(element.getId())
        .type(type)
        .description(description)
        .handledProperty(handledProperty)
        .performer(performer)
        .detailText(detailText)
        .build();

    ruleNetworkBuilder.addVertex(vertex);
  }

  private void addEdges(final FlowNode flowNode) {
    // Create one edge for each outgoing sequenceFlow. DefaultFlows make this more difficult
    // (generate inverse condition of all other conditions..)
    final Collection<Literal> conditions = new HashSet<>();
    final Collection<SequenceFlow> sequenceFlows = flowNode.getOutgoing();
    final String defaultFlowId;
    SequenceFlow defaultFlow = null;

    if (flowNode instanceof Activity) {
      defaultFlowId = ((Activity) flowNode).getDefault() != null ?
          ((Activity) flowNode).getDefault().getId() : null;
    } else {
      defaultFlowId = null;
    }

    for (final SequenceFlow osf : sequenceFlows) {
      if (!osf.getId().equals(defaultFlowId)) {
        addEdge(osf);
      } else {
        defaultFlow = osf;
      }
      if (defaultFlowId != null) {
        final ConditionExpression ce = osf.getConditionExpression();
        if (ce != null) {
          conditions.add(Literal.of(ce.getTextContent()));
        }
      }
    }

    if (defaultFlow != null) {
      Condition defaultFlowCondition = True.getInstance();
      for (final Literal condition : conditions) {
        defaultFlowCondition = defaultFlowCondition.and(condition.invert());
      }

      addEdge(defaultFlow, defaultFlowCondition);
    }

  }

  private void addEdgeOrAddAlternativeCondition(final String id,
      final String from,
      final String to,
      final Condition condition) {
    if (!ruleNetworkBuilder.containsEdgeWithId(id)) {
      ruleNetworkBuilder.addEdge(id,
          from,
          to,
          condition);
    } else {
      final Condition currentCondition =
          ruleNetworkBuilder.getConditionForEdgeWithId(id);
      ruleNetworkBuilder.replaceConditionForEdgeWithId(
          id,
          currentCondition.or(condition));
    }
  }

  /**
   * Adds the edge to the ruleNetwork with the given condition. If the ruleNetwork already contains the edge.
   * The condition is OR'd with the given condition
   * @param sequenceFlow The sequenceFlow to map to an edge
   * @param condition condition
   */
  private void addEdgeOrAddAlternativeCondition(final SequenceFlow sequenceFlow,
      final Condition condition) {
    addEdgeOrAddAlternativeCondition(sequenceFlow.getId(),
        sequenceFlow.getSource().getId(),
        sequenceFlow.getTarget().getId(),
        condition);
  }

  private void addEdge(final SequenceFlow sequenceFlow) {
    final ConditionExpression ce = sequenceFlow.getConditionExpression();
    final String condition;
    if (ce == null) {
      condition = null;
    } else {
      condition = ce.getTextContent();
    }

    if (condition == null) {
      ruleNetworkBuilder.addEdge(sequenceFlow.getId(),
          sequenceFlow.getSource().getId(),
          sequenceFlow.getTarget().getId(),
          True.getInstance());
    } else {
      addEdgeOrAddAlternativeCondition(sequenceFlow, Literal.of(condition));
    }
  }

  private void addEdge(final SequenceFlow sf, final Condition condition) {
    ruleNetworkBuilder.addEdge(
        sf.getId(),
        sf.getSource().getId(),
        sf.getTarget().getId(),
        condition);
  }

  private Map<String, String> getActivityDescriptions(final Collection<FlowNode> activities) {
    final Collection<Association> associations =
        bpmnModelInstance.getModelElementsByType(Association.class);

    final Set<String> activityIds = activities.stream().map(FlowNode::getId)
        .collect(Collectors.toSet());

    final HashMap<String, String> taskIdToDescription = new HashMap<>();
    associations.stream()
        .filter(association -> activityIds.contains(association.getSource().getId()))
        .forEach(association -> {
          if (association.getTarget() instanceof TextAnnotation) {
            taskIdToDescription.put(association.getSource().getId(),
                ((TextAnnotation) association.getTarget()).getText().getTextContent());
          }
        });

    activities.forEach(activity -> {
      if (!taskIdToDescription.containsKey(activity.getId())) {
        if (activity.getName() != null && !"".equals(activity.getName())) {
          taskIdToDescription.put(activity.getId(), activity.getName());
        } else {
          log.warn("Could not determine description for task {}. Falling back to "
              + "using the id as the description...", activity::getId);
          taskIdToDescription.put(activity.getId(), activity.getId());
        }
      }
    });

    return taskIdToDescription;
  }

}
