package com.openhouseautomation.model;

import com.google.common.base.Objects;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

import java.util.Date;

/**
 * A class representing a sensor device.
 *
 * @author jfmontesdeoca@google.com (Jose Montes de Oca)
 */
@Entity
@Index
public class Sensor {
    //TODO Fields for the type of reduction for history
  // like: Highs, Lows, Average, NonZeroAverage, NoReduction
  /**
   * Enum for the type of sensor.
   */
  public enum Type {

    TEMPERATURE,
    HUMIDITY,
    PRESSURE,
    MOVEMENT,
    LIGHT,
    WINDSPEED,
    WINDDIRECTION,
    VOLTAGE,
    RAIN;
  }

  @Id
  Long id; // hash for this unique sensor
  String owner; // your user name
  String location; // home, work, 
  String zone; // the specific place in location, like "Living Room", "Outside", "Garage"
  Type type; // See enum above
  String name;  // "Downstairs Temperature", "Wind Speed"
  String unit; // F, C, millibars, etc.
  String lastReading; // "89" for 89F
  Date lastReadingDate; // Date lastReading was last updated
  String secret; // the password for this sensor, used in SipHash
  //TODO: add boolean privacy flag (if true, requires auth)

  /**
   * Empty constructor for objectify.
   */
  public Sensor() {
  }

  /**
   * Returns the {@code id} of the {@link Sensor}.
   */
  public Long getId() {
    return id;
  }

  /**
   * Sets the {@code id} of the {@link Sensor}.
   *
   * @param id the Id to set
   */
  public void setId(Long id) {
    this.id = id;
  }

  /**
   * Returns the {@code owner} of the {@link Sensor}.
   */
  public String getOwner() {
    return owner;
  }

  /**
   * Sets the {@code owner} of the {@link Sensor}.
   *
   * @param owner the owner to set
   */
  public void setOwner(String owner) {
    this.owner = owner;
  }

  /**
   * Returns the {@code location} of the {@link Sensor}.
   */
  public String getLocation() {
    return location;
  }

  /**
   * Sets the {@code location} of the {@link Sensor}.
   *
   * @param location the location to set
   */
  public void setLocation(String location) {
    this.location = location;
  }

  /**
   * Returns the {@code zone} of the {@link Sensor}.
   */
  public String getZone() {
    return zone;
  }

  /**
   * Sets the {@code zone} of the {@link Sensor}.
   *
   * @param zone the zone to set
   */
  public void setZone(String zone) {
    this.zone = zone;
  }

  /**
   * Returns the {@code type} of the {@link Sensor}.
   */
  public Type getType() {
    return type;
  }

  /**
   * Sets the {@code type} of the {@link Sensor}.
   *
   * @param type the type to set
   */
  public void setType(Type type) {
    this.type = type;
  }

  /**
   * Returns the {@code name} of the {@link Sensor}.
   */
  public String getName() {
    return name;
  }

  /**
   * Sets the {@code name} of the {@link Sensor}.
   *
   * @param name the owner to set
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Returns the {@code unit} of the {@link Sensor}.
   */
  public String getUnit() {
    return unit;
  }

  /**
   * Sets the {@code unit} of the {@link Sensor}.
   *
   * @param unit the unit to set
   */
  public void setUnit(String unit) {
    this.unit = unit;
  }

  /**
   * Returns the {@code lastReading} of the {@link Sensor}.
   */
  public String getLastReading() {
    return lastReading;
  }

  /**
   * Sets the {@code lastReading} of the {@link Sensor}.
   *
   * @param lastReading the lastReading to set
   */
  public void setLastReading(String lastReading) {
    this.lastReading = lastReading;
  }

  public Date getLastReadingDate() {
    return lastReadingDate;
  }

  /**
   * Sets the {@code lastReadingDate} of the {@link Sensor}.
   *
   * @param lastReadingDate the lastReadingDate to set
   */
  public void setLastReadingDate(Date lastReadingDate) {
    this.lastReadingDate = lastReadingDate;
  }

  public void setSecret(String secret) {
    this.secret = secret;
  }
  
  public String getSecret() {
    return secret;
  }
  @Override
  public int hashCode() {
    return Objects.hashCode(id,
        owner,
        location,
        zone,
        type,
        name,
        unit,
        lastReading,
        lastReadingDate);
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof Sensor)) {
      return false;
    }

    Sensor otherSensor = (Sensor) obj;
    return Objects.equal(this.id, otherSensor.getId())
        && Objects.equal(this.owner, otherSensor.getOwner())
        && Objects.equal(this.location, otherSensor.getLocation())
        && Objects.equal(this.zone, otherSensor.getZone())
        && Objects.equal(this.type, otherSensor.getType())
        && Objects.equal(this.name, otherSensor.getName())
        && Objects.equal(this.unit, otherSensor.getUnit())
        && Objects.equal(this.lastReading, otherSensor.getLastReading())
        && Objects.equal(this.lastReadingDate, otherSensor.getLastReadingDate());
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(getClass().getName())
        .add("id", id)
        .add("owner", owner)
        .add("location", location)
        .add("zone", zone)
        .add("type", type)
        .add("name", name)
        .add("unit", unit)
        .add("lastReading", lastReading)
        .add("lastReadingDate", lastReadingDate)
        .toString();
  }
}
