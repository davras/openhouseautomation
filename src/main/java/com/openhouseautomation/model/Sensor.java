package com.openhouseautomation.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import org.joda.time.DateTime;
import com.google.common.base.Objects;
import com.googlecode.objectify.annotation.*;
import com.openhouseautomation.Convutils;
import com.openhouseautomation.iftt.DeferredSensor;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A class representing a sensor device.
 *
 * @author jfmontesdeoca@google.com (Jose Montes de Oca)
 */
@Entity
@Index
@Cache
public class Sensor implements Serializable {

  private static final long serialVersionUID = 101010L;
  private static final Logger log = Logger.getLogger(Sensor.class.getName());

  /**
   * @return the previousreading
   */
  public String getPreviousReading() {
    return previousreading;
  }

  /**
   * @param previousreading the previousreading to set
   */
  public void setPreviousReading(String previousreading) {
    this.previousreading = previousreading;
  }
  //TODO Fields for the type of reduction for history
  // like: Highs, Lows, Average, NonZeroAverage, NoReduction
  /**
   * Enum for the type of sensor. Self-explanatory
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
    RAIN,
    ACCELEROMETER, /**
     * < Gravity + linear acceleration
     */
    MAGNETIC_FIELD,
    ORIENTATION,
    GYROSCOPE,
    PROXIMITY,
    GRAVITY,
    LINEAR_ACCELERATION, /**
     * < Acceleration not including gravity
     */
    ROTATION_VECTOR,
    CURRENT,
    COLOR;
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
  @JsonIgnore
  DateTime lastReadingDate; // Date lastReading was last updated
  @JsonIgnore
  String secret; // the password for this sensor, used in SipHash
  Integer expirationtime; // if no update occurs within this time, the sensor is 'expired'
  //TODO: add boolean privacy flag (if true, requires auth)
  @Ignore
  String humanage;
  @Ignore
  boolean expired;
  @Ignore
  public String previousreading; // the reading when the entity was loaded
  @JsonIgnore
  private boolean postprocessing = false;
  
  @OnLoad
  void updateAge() {
    this.humanage = Convutils.timeAgoToString(getLastReadingDate().getMillis() / 1000);
  }

  @OnLoad
  void backupLastReading() {
    previousreading = lastReading;
  }

  @OnLoad
  void updateExpired() {
    if (lastReadingDate == null) {
      DateTime now = Convutils.getNewDateTime();
      lastReadingDate = now.minusMinutes(15);
      // gives a new sensor 15 mins to report
    }
    if (expirationtime == null) {
      this.expired = false;
      expirationtime = 60 * 60; // 1 hour default
    }
    if (expirationtime == 0) {
      this.expired = false;
    } else {
      this.expired = lastReadingDate.plusSeconds(getExpirationTime()).isBeforeNow();
    }
  }

  @OnSave
  public void doPostProcessing() {
    if (!needsPostprocessing()) {
      return;
    }
    if (Objects.equal(getPreviousReading(), getLastReading())) {
      return;
    }
    // Add the task to the default queue.
    Queue queue = QueueFactory.getDefaultQueue();
    DeferredSensor dfc = null;
    String classtoget = "com.openhouseautomation.iftt." + Convutils.toTitleCase(getType().name());
    log.log(Level.INFO, "creating class: {0}", classtoget);
    try {
      dfc = (DeferredSensor) Class.forName(classtoget).newInstance();
      // i.e.: com.openhouseautomation.iftt.Alarm class
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
      // obviously, don't enqueue the task
      log.log(Level.WARNING, "I could not create the class needed: {0}"
              + "\n" + "Please make sure the class exists and is accessible before enabling postprocessing on controller id: {1}",
              new Object[]{classtoget, getId()}
      );
    }
    if (dfc != null) {
      // grab the sensor and add the task
      dfc.setSensor(this);
      queue.add(TaskOptions.Builder.withPayload(dfc));
    }
  }

  public boolean isExpired() {
    return expired;
  }

  /**
   * @return the postprocessing
   */
  public boolean needsPostprocessing() {
    return postprocessing;
  }

  /**
   * @param postprocessing the postprocessing to set
   */
  public void setPostprocessing(boolean postprocessing) {
    this.postprocessing = postprocessing;
  }

  public String getHumanAge() {
    return humanage;
  }

  /**
   * Empty constructor for objectify.
   */
  public Sensor() {
  }

  /**
   * Returns the {@code id} of the {@link Sensor}.
   *
   * @return Long sensor id
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
   *
   * @return String owner's name/id
   */
  @JsonProperty("owner")
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
   *
   * @return String of location of sensor
   */
  @JsonProperty("location")
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
   *
   * @return String zone of the sensor
   */
  @JsonProperty("zone")
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
   *
   * @return Type of sensor, like temperature, pressure, etc. from the Sensor
   * ENUM
   */
  @JsonProperty("type")
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
   *
   * @return String the name of the sensor
   */
  @JsonProperty("name")
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
   *
   * @return String the units of the Sensor, like F, C, inHg, etc.
   */
  @JsonProperty("unit")
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
   *
   * @return String the last reading logged for this Sensor.
   */
  @JsonProperty("lastreading")
  public String getLastReading() {
    return lastReading;
  }

  /**
   * Returns the {@code lastReading} of the {@link Sensor} rounded to the
   * precision A precision of zero will return no decimal or places (i.e. 2.6 ->
   * 3, not 3.0)
   *
   * @param precision number of decimal places to round to
   * @return String of the rounded number
   */
  public String getLastReading(int precision) {
    double expon = Math.pow(10, precision);
    double d = Double.parseDouble(lastReading) * expon;
    int d2 = (int) Math.round(d);
    double d3 = d2 / expon;
    String s = Double.toString(d3);
    if (precision == 0) {
      return s.substring(0, s.indexOf("."));
    }
    return s;
  }

  /**
   * Sets the {@code lastReading} of the {@link Sensor}.
   *
   * @param lastReading the lastReading to set
   */
  public void setLastReading(String lastReading) {
    if (null == lastReading || "".equals(lastReading)) {
      return;
    }
    // oddly, this:
    //setLastReadingDate(Convutils.getNewDateTime());
    // causes:
    // Class 'class org.joda.time.chrono.ISOChronology' is not a registered @Subclass
    this.lastReading = lastReading;
  }

  /**
   * Returns the {@code lastReadingDate} of the {@link Sensor}.
   *
   * @return Date the last time this sensor was updated with a reading
   */
  @JsonIgnore
  public DateTime getLastReadingDate() {
    return lastReadingDate;
  }

  /**
   * Sets the {@code lastReadingDate} of the {@link Sensor}.
   *
   * @param lastReadingDate the lastReadingDate to set
   */
  public void setLastReadingDate(DateTime lastReadingDate) {
    this.lastReadingDate = lastReadingDate;
  }

  /**
   * Sets the {@code secret} for this {@link Sensor}.
   *
   * @param secret String to set
   */
  public void setSecret(String secret) {
    this.secret = secret;
  }

  /**
   * Returns the {@code secret} for this {@link Sensor}.
   *
   * @return String secret for this sensor used to authenticate devices'
   * updates.
   */
  public String getSecret() {
    return secret;
  }
  
  /**
   * Sets the {@code expirationtime} for this {@link Sensor}.
   *
   * @param expirationtime in seconds since last sensor reading
   */
  @JsonIgnore
  public void setExpirationTime(Integer expirationtime) {
    this.expirationtime = expirationtime;
  }

  /**
   * Returns the {@code expirationtime} for this {@link Sensor}.
   *
   * @return Integer expiration time in seconds
   */
  public Integer getExpirationTime() {
    return expirationtime;
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
