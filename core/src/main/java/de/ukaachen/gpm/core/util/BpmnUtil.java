package de.ukaachen.gpm.core.util;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.camunda.bpm.model.bpmn.instance.BaseElement;
import org.camunda.bpm.model.bpmn.instance.ExtensionElements;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperties;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperty;

final public class BpmnUtil {

  private static BpmnUtil instance;

  private BpmnUtil() {

  }

  public static BpmnUtil instance() {
    if (instance == null) {
      instance = new BpmnUtil();
    }
    return instance;
  }

  public Optional<CamundaProperties> getCamundaProperties(final BaseElement element) {
    final ExtensionElements extensionElements = element.getExtensionElements();
    if (extensionElements == null) {
      return Optional.empty();
    }
    final Collection<CamundaProperties> camundaProperties = extensionElements
        .getChildElementsByType(CamundaProperties.class);
    if (camundaProperties == null || camundaProperties.size() == 0) {
      return Optional.empty();
    }
    if (camundaProperties.size() != 1) {
      throw new IllegalStateException("Element " + element.getId() + " has more than one list of "
          + "camundaProperties.");
    }
    final CamundaProperties theCamundaProperties = camundaProperties.iterator().next();

    return Optional.ofNullable(theCamundaProperties);
  }

  public Optional<Pair<String, String>> getHandledProperty(final BaseElement element) {
    final Optional<CamundaProperties> theCamundaProperties = getCamundaProperties(element);

    if(theCamundaProperties.isPresent()) {
      final Set<CamundaProperty> filteredCamundaProperties =
          theCamundaProperties.get().getCamundaProperties().stream()
              .filter(prop -> !prop.getCamundaName().startsWith("GPM"))
              .collect(Collectors.toSet());

      if (filteredCamundaProperties.size() != 1) {
        throw new IllegalStateException("The CamundaProperties of element " + element.getId() + " "
            + "has more than one property, which is not allowed.");
      }

      final CamundaProperty theProperty = filteredCamundaProperties.iterator().next();

      return Optional.of(Pair.create(theProperty.getCamundaName(), theProperty.getCamundaValue()));
    } else {
      return Optional.empty();
    }
  }
}
