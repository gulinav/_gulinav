package de.ukaachen.gpm.core.model.rule_network;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;

public class TaskDetailTexts {

  @Getter
  private final Map<String, String> taskToText;

  public TaskDetailTexts(final InputStream detailTextsStream) {

    try {
      final TypeReference<HashMap<String, String>> typeReference
          = new TypeReference<HashMap<String, String>>() {};

      this.taskToText = new ObjectMapper().readValue(detailTextsStream, typeReference);
    } catch (final IOException e) {
      throw new IllegalArgumentException("The given stream is not a valid map from task ids to "
          + "detailed texts");
    }
  }

  public TaskDetailTexts() {
    taskToText = Collections.emptyMap();
  }
}
