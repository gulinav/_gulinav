package de.ukaachen.gpm.mks.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@EqualsAndHashCode
@ToString
@AllArgsConstructor
public class Code {

  @Getter
  private final String system;

  @Getter
  private final String value;

  public Code(final String colonSeparated) {
    if(colonSeparated == null) {
      throw new IllegalArgumentException("Constructor argument must not be null");
    }
    final String[] split = colonSeparated.split(":");
    if(split.length != 2) {
      throw new IllegalArgumentException("Constructor argument must be system and value, "
          + "separated by colon. Was " + colonSeparated);
    }
    this.system = split[0];
    this.value = split[1];
  }
}
