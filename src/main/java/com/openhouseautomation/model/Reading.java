package com.openhouseautomation.model;

import com.google.common.base.MoreObjects;
import org.joda.time.DateTime;
import com.google.common.base.Objects;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.*;

/**
 * A class representing a reading from a sensor device.
 * 
 * @author jfmontesdeoca@google.com (Jose Montes de Oca)
 */
@Entity
@Cache
@Unindex
public class Reading {
  @Parent 
  @Index 
  Key<Sensor> sensor;
  
  @Id
  @Index
  Long id;
  
  String value;
  // todo, move @Index down here
  @Index
  DateTime timestamp;
  
  /**
   * Empty constructor for objectify.
   */
  public Reading() {}

  /**
   * Returns the parent {@code sensor} of the {@link Reading}.
   */
  public Key<Sensor> getSensor() {
    return sensor;
  }

  /**
   * Sets the {@code sensor} Parent Key of the {@link Reading}.
   *
   * @param sensor the sensor to set
   */
  public void setSensor(Key<Sensor> sensor) {
    this.sensor = sensor;
  }

  /**
   * Returns the {@code id} of the {@link Reading}.
   */
  public Long getId() {
    return id;
  }
  
  /**
   * Sets the {@code id} of the {@link Reading}.
   *
   * @param id the id to set
   */
  public void setId(Long id) {
    this.id = id;
  }
  
  /**
   * Returns the {@code value} of the {@link Reading}.
   */
  public String getValue() {
    return value;
  }
  
  /**
   * Sets the {@code value} of the {@link Reading}.
   *
   * @param value the id to set
   */
  public void setValue(String value) {
    this.value = value;
  }
  
  /**
   * Returns the {@code timestamp} of the {@link Reading}.
   */
  public DateTime getTimestamp() {
    return timestamp;
  }
  
  /**
   * Sets the {@code timestamp} of the {@link Reading}.
   *
   * @param id the id to set
   */
  public void setTimestamp(DateTime timestamp) {
    this.timestamp = timestamp;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(sensor,
        id,
        value,
        timestamp);
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof Reading)) {
      return false;
    }
    
    Reading otherReading = (Reading) obj;
    return Objects.equal(this.sensor, otherReading.getSensor())
        && Objects.equal(this.id, otherReading.getId())
        && Objects.equal(this.value, otherReading.getValue())
        && Objects.equal(this.timestamp, otherReading.getTimestamp());
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("sensor", sensor)
        .add("id", id)
        .add("value", value)
        .add("timestamp", timestamp)
        .toString();
  }
}
