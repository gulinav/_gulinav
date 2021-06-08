package de.ukaachen.gpm.mks.model;

import java.time.Instant;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;


/**
 * POJO for patient properties. They can either be observations of the patient such as simple
 * vital data (e.g. heart rate) or more complex derived data (such as the result of an ARDEN
 * evaluation)
 */
@ToString
@EqualsAndHashCode
public class PatientProperty {

  @Getter
  private final String externalId;

  @Getter
  private final Code code;

  @Getter
  private final Object value;

  @Getter
  private final Instant timestamp;

  @Getter
  private final Instant expirationTime;

  @Getter
  private final List<String> basedOn;

  @Getter
  private final String unit;

  public PatientProperty(final Code code, final Object value, final Instant timestamp,
      final Instant expirationTime, final List<String> basedOn,
      final String unit, final String externalId) {
    this.code = code;
    this.value = value;
    this.timestamp = timestamp;
    this.expirationTime = expirationTime;
    this.basedOn = basedOn;
    this.unit = unit;
    this.externalId = externalId;
  }

  public PatientProperty(final Code code, final Object value, final Instant timestamp,
      final Instant expirationTime, final List<String> basedOn) {
    this(code, value, timestamp, expirationTime, basedOn, "", null);
  }

  public PatientProperty(final Code code, final Object value) {
    this(code, value, Instant.now(), null, null);
  }

  public PatientProperty(final Object value) {
    this(null, value);
  }

  public static PatientPropertyBuilder withValue(final Object value) {
    final PatientPropertyBuilder result = new PatientPropertyBuilder();
    result.withValue(value);
    return result;
  }


}
