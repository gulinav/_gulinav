package de.ukaachen.gpm.core.procedures;


import de.ukaachen.gpm.core.exceptions.BpmnNotProcessableException;
import de.ukaachen.gpm.core.util.BpmnUtil;
import de.ukaachen.gpm.core.util.Pair;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import lombok.extern.log4j.Log4j2;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.Association;
import org.camunda.bpm.model.bpmn.instance.BaseElement;
import org.camunda.bpm.model.bpmn.instance.ConditionExpression;
import org.camunda.bpm.model.bpmn.instance.EndEvent;
import org.camunda.bpm.model.bpmn.instance.ExclusiveGateway;
import org.camunda.bpm.model.bpmn.instance.ParallelGateway;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.bpm.model.bpmn.instance.SequenceFlow;
import org.camunda.bpm.model.bpmn.instance.ServiceTask;
import org.camunda.bpm.model.bpmn.instance.StartEvent;
import org.camunda.bpm.model.bpmn.instance.Task;
import org.camunda.bpm.model.bpmn.instance.TextAnnotation;
import org.camunda.bpm.model.bpmn.instance.UserTask;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;

@Log4j2
public class BpmnVerifier {

  private final BpmnModelInstance model;

  private BpmnVerifier(final BpmnModelInstance model) {
    this.model = model;
  }

  public static BpmnVerifier forBpmnModel(final BpmnModelInstance model) {
    return new BpmnVerifier(model);
  }

  public void verifyProcessability() throws BpmnNotProcessableException {
    log.info("Verifying processability of given BpmnModelInstance {}",
        () -> model);
    final Set<String> errors = new HashSet<>();

    checkInvalidTaskTypes().ifPresent(errors::add);
    checkUnsupportedElements().ifPresent(errors::add);
    checkSameKeyForDifferentCodes().ifPresent(errors::add);
    checkValidityOfConditionExpressions().ifPresent(errors::add);

    if (!errors.isEmpty()) {
      throw new BpmnNotProcessableException("Cannot process the BPMN: " + errors);
    } else {
      log.info("Verified the processability of the given BpmnModelInstance {}",
          () -> model);
    }
  }

  private Optional<String> checkInvalidTaskTypes() {
    final Collection<Task> tasks = model.getModelElementsByType(Task.class);
    final Set<String> falseTasks = new HashSet<>();

    tasks.forEach(task -> {
      if (!(task instanceof ServiceTask) && !(task instanceof UserTask)) {
        falseTasks.add(task.getId());
      }
    });

    if (falseTasks.isEmpty()) {
      return Optional.empty();
    } else {
      return Optional.of("Tasks with invalid types exist: " + falseTasks);
    }
  }

  private Optional<String> checkUnsupportedElements() {
    final Collection<BaseElement> allElements =
        model.getModelElementsByType(BaseElement.class);
    final Set<String> falseElements = new HashSet<>();

    final BiConsumer<ModelElementInstance, List<Class>> checker = (theElement, theClasses) -> {
      boolean isValidClass = false;
      for (final Class<?> clazz : theClasses) {
        if (clazz.isAssignableFrom(theElement.getClass())) {
          isValidClass = true;
        }
      }
      if (!isValidClass) {
        falseElements.add(theElement.getClass().getSimpleName());
      }
    };

    for (final ModelElementInstance element : allElements) {
      checker.accept(element, Arrays.asList(
          ServiceTask.class,
          UserTask.class,
          ExclusiveGateway.class,
          ParallelGateway.class,
          SequenceFlow.class,
          EndEvent.class,
          StartEvent.class,
          Process.class,
          Association.class,
          TextAnnotation.class,
          ConditionExpression.class));
    }

    if (falseElements.isEmpty()) {
      return Optional.empty();
    } else {
      return Optional.of("UnsupportedElements are in the given model: " + falseElements);
    }
  }

  private Optional<String> checkSameKeyForDifferentCodes() {
    final BpmnUtil conversionUtil = BpmnUtil.instance();
    final Map<String, String> keyToCode = new HashMap<>();
    final Set<Pair<String, String>> invalidKeyCodeMappings = new HashSet<>();

    final Collection<ServiceTask> serviceTasks = model.getModelElementsByType(ServiceTask.class);
    serviceTasks.stream()
        .map(conversionUtil::getHandledProperty)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .forEach(k -> {
          if (keyToCode.containsKey(k.f())) {
            if (!keyToCode.get(k.f()).equals(k.s())) {
              invalidKeyCodeMappings.add(k);
              invalidKeyCodeMappings.add(Pair.create(k.f(), keyToCode.get(k.f())));
            }
          } else {
            keyToCode.put(k.f(), k.s());
          }
        });

    if (invalidKeyCodeMappings.isEmpty()) {
      return Optional.empty();
    } else {
      return Optional.of("The same key in sequenceFlow conditions is used for different codes: " +
          invalidKeyCodeMappings);
    }
  }

  private Optional<String> checkValidityOfConditionExpressions() {
    final Collection<String> invalidConditions = model.getModelElementsByType(SequenceFlow.class)
        .stream()
        .map(SequenceFlow::getConditionExpression)
        .filter(Objects::nonNull)
        .map(ConditionExpression::getTextContent)
        .filter(expr -> !expr.startsWith("${"))
        .filter(expr -> !expr.endsWith("}"))
        .collect(Collectors.toSet());

    if (invalidConditions.isEmpty()) {
      return Optional.empty();
    } else {
      return Optional.of("All conditions need to be Immediate Evaluations (i.e. start with '${' and"
          + " end with '}', but the following expressions are present: " + invalidConditions);
    }
  }
}
