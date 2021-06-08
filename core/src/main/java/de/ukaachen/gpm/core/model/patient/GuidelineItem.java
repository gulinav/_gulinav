package de.ukaachen.gpm.core.model.patient;

import de.ukaachen.gpm.core.util.Pair;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;

@EqualsAndHashCode
@ToString
@Log4j2
public class GuidelineItem {

  /**
   * The short ID of the FHIR resource or null, if that is not known
   */
  @Getter
  @Setter
  private String id;

  /**
   * The full ID of the FHIR resource or null, if that is not known.
   * This can be used for creating references to the underlying Task resource.
   */
  @Getter
  @Setter
  private String externalId;


  /**
   * The corresponding task in the workflow model
   */
  @Getter
  @Setter
  private String taskId;

  /**
   * The description to be shown to the medical staff (e.g. give medication XYZ)
   */
  @Getter
  @Setter
  private String description;

  /**
   * An optional more detailed description to be shown to the medical staff
   */
  @Getter
  @Setter
  private String detailText;

  /**
   * The timePhase in which this item is in (e.g. was the item already executed etc..)
   */
  @Getter
  @Setter
  private TimePhase timePhase;

  /**
   * The patient properties that this guidelineItem is associated with (i.e. which properties are
   * needed to evaluate the conditions of the outgoing sequenceFlows of the corresponding task)
   */
  @Getter
  @Setter
  private Map<String, String> handledProperties;

  /**
   * The values of the properties that this guidelineItem is associated with (humanReadableKey)
   */
  @Getter
  @Setter
  private Map<String, Object> properties;

  /**
   * The entity that shall perform the procedure of this item (human task or automatically..)
   */
  @Getter
  @Setter
  private Performer performer;

  /**
   * References to other entities that this item is based on (e.g. ids of FHIR-Observations)
   */
  @Getter
  @Setter
  private List<String> basedOnExternalEntity;

  private GuidelineItem() {
    this.properties = new HashMap<>();
  }

  public static GuidelineItem task(final String taskId) {
    final GuidelineItem result = new GuidelineItem();
    result.taskId = taskId;
    result.handledProperties = Collections.emptyMap();
    return result;
  }

  public GuidelineItem desc(final String description) {
    if (this.description != null) {
      throw new IllegalStateException("Description has already been set.");
    }
    this.description = description;
    return this;
  }

  public GuidelineItem timePhase(final TimePhase timePhase) {
    if (this.timePhase != null) {
      log.warn("TimePhase was changed from " + this.timePhase + " to " + timePhase + " for the "
          + "guidelineItem with description " + this.description + ".");
    }
    this.timePhase = timePhase;
    return this;
  }

  public final GuidelineItem noHandledProperties() {
    if (this.handledProperties != null && this.handledProperties.size() != 0) {
      throw new IllegalArgumentException("AssociatedAttributes has already been set.");
    }
    this.handledProperties = new HashMap<>();
    return this;
  }

  public final GuidelineItem property(final Pair<String, String> handledProperty) {
    if (handledProperty == null) {
      return noHandledProperties();
    }

    if (this.handledProperties != null && this.handledProperties.size() != 0) {
      throw new IllegalArgumentException("AssociatedAttributes has already been set.");
    }

    this.handledProperties = new HashMap<>();
    this.handledProperties.put(handledProperty.f(), handledProperty.s());
    return this;
  }

  public final GuidelineItem handledProperties(final Map<String, String> handledProperties) {
    if (this.handledProperties != null && this.handledProperties.size() != 0) {
      throw new IllegalArgumentException("AssociatedAttributes has already been set.");
    }

    this.handledProperties = handledProperties;
    return this;
  }

  public GuidelineItem value(final String humanReadableKey, final Object value) {
    this.properties.put(humanReadableKey, value);
    return this;
  }

  public GuidelineItem values(final Map<String, Object> values) {
    if (!this.properties.isEmpty()) {
      throw new IllegalArgumentException("Values have already been set.");
    }

    this.properties = values;
    return this;
  }

  public GuidelineItem performer(final Performer performer) {
    if (this.performer != null) {
      throw new IllegalArgumentException("Performer was already set to " + this.performer);
    }

    this.performer = performer;
    return this;
  }


  public GuidelineItem detailText(final String detailText) {
    if (this.detailText != null) {
      throw new IllegalArgumentException("DetailText was already set to " + this.detailText);
    }

    this.detailText = detailText;
    return this;
  }

  public Optional<Pair<String, Object>> getSinglePropertyValue() {
    return getSingleKeyValue(this.properties);
  }

  public Optional<Pair<String, String>> getSingleHandledProperty() {
    return getSingleKeyValue(this.handledProperties);
  }

  public GuidelineItem basedOnExternalEntity(final String reference) {
    if (this.basedOnExternalEntity == null) {
      this.basedOnExternalEntity = new LinkedList<>();
    }

    this.basedOnExternalEntity.add(reference);
    return this;
  }

  public GuidelineItem basedOnExternalEntities(final List<String> externalEntityIds) {
    if (this.basedOnExternalEntity != null) {
      throw new IllegalArgumentException("External Entities have already been set.");
    }

    this.basedOnExternalEntity = externalEntityIds;
    return this;
  }

  private static <K, V> Optional<Pair<K, V>> getSingleKeyValue(final Map<K, V> map) {
    if (map.size() != 1) {
      return Optional.empty();
    }

    final K key = map.keySet().iterator().next();
    final V value = map.get(key);

    return Optional.of(Pair.create(key, value));
  }

}
