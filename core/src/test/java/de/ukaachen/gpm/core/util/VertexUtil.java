package de.ukaachen.gpm.core.util;


import de.ukaachen.gpm.core.model.patient.Performer;
import de.ukaachen.gpm.core.model.rule_network.Vertex;
import de.ukaachen.gpm.core.model.rule_network.VertexType;

public class VertexUtil {

  public static Vertex v(final String vertexId) {
    return Vertex.withId(vertexId)
        .performer(Performer.HUMAN)
        .description(vertexId)
        .type(VertexType.ACTIVITY)
        .build();
  }

  public static Vertex start(final String vertexId) {
    return Vertex.withId(vertexId)
        .performer(Performer.HUMAN)
        .description(vertexId)
        .type(VertexType.START_EVENT)
        .build();
  }

  public static Vertex end(final String vertexId) {
    return Vertex.withId(vertexId)
        .performer(Performer.HUMAN)
        .description(vertexId)
        .type(VertexType.END_EVENT)
        .build();
  }
}
