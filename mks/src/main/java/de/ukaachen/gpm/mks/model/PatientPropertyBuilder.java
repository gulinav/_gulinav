package de.ukaachen.gpm.mks.model;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

public class PatientPropertyBuilder {

  private String externalId;
  private Code code;
  private Object value;
  private Instant timestamp;
  private Instant expirationTime;
  private List<String> basedOn;
  private String unit;

  public PatientPropertyBuilder withValue(final Object value) {
    this.value = value;
    return this;
  }

  public PatientPropertyBuilder withExternalId(final String externalId) {
    this.externalId = externalId;
    return this;
  }

  public PatientPropertyBuilder withCode(final Code code) {
    this.code = code;
    return this;
  }

  public PatientPropertyBuilder withTimestamp(final Instant timestamp) {
    this.timestamp = timestamp;
    return this;
  }

  public PatientPropertyBuilder withExpirationTime(final Instant expirationTime) {
    this.expirationTime = expirationTime;
    return this;
  }

  public PatientPropertyBuilder withBasedOn(final List<String> basedOn) {
    this.basedOn = basedOn;
    return this;
  }

  public PatientPropertyBuilder withBasedOn(final String basedOn) {
    this.basedOn = Collections.singletonList(basedOn);
    return this;
  }

  public PatientPropertyBuilder withUnit(final String unit) {
    this.unit = unit;
    return this;
  }

  public PatientProperty create() {
    return new PatientProperty(code, value, timestamp, expirationTime, basedOn, unit, externalId);
  }
}
