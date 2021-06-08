package de.ukaachen.gpm.core.model.rule_network;

import de.ukaachen.gpm.core.model.patient.Performer;
import de.ukaachen.gpm.core.util.Pair;
import java.util.Comparator;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;


@ToString
@EqualsAndHashCode
public class Vertex implements Comparable<Vertex> {

  private static final Comparator<Vertex> COMPARATOR =
      Comparator.comparing(Vertex::getId).thenComparing(Vertex::getType);

  @Getter
  private final String id;

  @Getter
  private final Pair<String, String> handledProperty;

  @Getter
  private final VertexType type;

  @Getter
  private final String description;

  @Getter
  private final Performer performer;

  @Getter
  private final String detailText;

  public Vertex(final String id,
      final Pair<String, String> handledProperty,
      final VertexType type,
      final String description,
      final Performer performer,
      final String detailText) {
    this.id = id;
    this.handledProperty = handledProperty;
    this.type = type;
    this.description = description;
    this.performer = performer;
    this.detailText = detailText;
  }

  public static VertexBuilder withId(final String id) {
    return new VertexBuilder(id);
  }

  @Override
  public int compareTo(final Vertex o) {
    return COMPARATOR.compare(this, o);
  }
}
