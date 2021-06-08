package de.ukaachen.gpm.core.model.rule_network;

import de.ukaachen.gpm.core.model.patient.Performer;
import de.ukaachen.gpm.core.util.Pair;

public class VertexBuilder {


  private final String id;

  private Pair<String, String> handledProperty;

  private VertexType type;

  private String description;

  private Performer performer;

  private String detailText;

  VertexBuilder(final String id) {
    this.id = id;
  }

  public VertexBuilder handledProperty(final Pair<String, String> handledProperty) {
    this.handledProperty = handledProperty;
    return this;
  }

  public VertexBuilder type(final VertexType vertexType) {
    this.type = vertexType;
    return this;
  }

  public VertexBuilder description(final String description) {
    this.description = description;
    return this;
  }

  public VertexBuilder performer(final Performer performer) {
    this.performer = performer;
    return this;
  }

  public VertexBuilder detailText(final String detailText) {
    this.detailText = detailText;
    return this;
  }

  public Vertex build() {
    if (this.type == null || (this.performer == null && this.type == VertexType.ACTIVITY)) {
      throw new IllegalStateException("Cannot build vertex if type and/or performer is null.");
    }

    if (this.performer == null) {
      this.performer = Performer.NONE;
    }

    return new Vertex(id, handledProperty, type, description, performer, detailText);
  }

}
