package com.openhouseautomation.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import org.joda.time.DateTime;
import com.google.common.base.Objects;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Ignore;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.OnLoad;
import com.googlecode.objectify.annotation.OnSave;
import com.openhouseautomation.Convutils;
import com.openhouseautomation.iftt.DeferredController;
import java.io.Serializable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class representing a controller device.
 *
 * @author jfmontesdeoca@google.com (Jose Montes de Oca)
 */
@Entity
@Cache
public class Controller implements Serializable {

  private static final long serialVersionUID = 27L;
  private static final Logger log = Logger.getLogger(Controller.class.getName());

  /**
   * Enum for the Device type
   */
  public enum Type {

    ALARM("Alarm"),
    GARAGEDOOR("Garage Door"),
    LIGHTS("Lights"),
    PROJECTOR("Projector"),
    SPRINKLER("Sprinkler"),
    THERMOSTAT("Thermostat"),
    WHOLEHOUSEFAN("Whole House Fan");

    private final String text;

    private Type(final String text) {
      this.text = text;
    }

    @Override
    public String toString() {
      return text;
    }

    public static Type getTypebyName(String longname) {
      for (Type t : Type.values()) {
        if (t.toString().equals(longname)) {
          return t;
        }
      }
      throw new IllegalArgumentException("Non-existant type name: " + longname);
    }
  }

  public enum DesiredStatePriority { // lowest priority is on top, order is important!

    AUTO,
    MANUAL,
    LOCAL,
    EMERGENCY;
  }

  @Id
  public Long id; //id from CRC32 hash of owner, location, zone, and salt.
  public String owner;//Owner of the device
  public String location; //Place where the device is located
  public String zone; //Zone where the device is located
  @Index
  public Type type; //The type of device
  @Index
  public String name; // The name of the device
  @JsonIgnore
  public String desiredstate; //What the controller wants the state to be
  @JsonIgnore
  public String actualstate; //The actual state of the device in real life
  @JsonIgnore
  public DesiredStatePriority desiredstatepriority;  // The priority of the desired state, lower priority changes should be ignored
  @JsonIgnore
  public DateTime lastdesiredstatechange; // The Date the last time the desired state changed
  @JsonIgnore
  public DateTime lastactualstatechange; // The Date the last time the desired state changed
  @JsonIgnore
  public List validstates; // the list of valid states for the desired and actual states
  @JsonIgnore
  public DateTime lastcontactdate; // Date lastReading was last updated
  @Ignore
  public boolean expired;
  public Integer expirationtime; // if no update occurs within this time, the controller is 'expired'
  @Ignore
  private String previousstate; // the previous actual state

  /**
   * Empty constructor for objectify.
   */
  public Controller() {
  }

  @JsonIgnore
  private boolean postprocessing = false;

  @OnSave
  void handlePostProcessing() {
    if (!needsPostprocessing()) {
      return;
    }
    if (Objects.equal(getActualState(), getPreviousState())
            && Objects.equal(getDesiredState(), getActualState())) {
      log.log(Level.INFO, "No changes, no task");
      return;
    }
    
    // Add the task to the default queue.
    Queue queue = QueueFactory.getDefaultQueue();
    DeferredController dfc = null;
    String classtoget = "com.openhouseautomation.iftt." + Convutils.toTitleCase(this.getType().name());
    log.log(Level.INFO, "creating class: {0}", classtoget);
    try {
      dfc = (DeferredController) Class.forName(classtoget).newInstance();
      // i.e.: com.openhouseautomation.iftt.ALARM class
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
      // obviously, don't enqueue the task
      log.log(Level.WARNING, "I could not create the class needed: {0}"
              + "\n" + "Please make sure the class exists and is accessible before enabling postprocessing on controller id: {1}",
              new Object[]{classtoget, this.getId()}
      );
      dfc = null;
    }
    if (dfc == null) {
      // bail early
      return;
    }
    dfc.setController(this);
    queue.add(TaskOptions.Builder.withPayload(dfc));
  }

  @OnLoad
  void backupLastReading() {
    setPreviousState(getActualState());
  }

  @OnLoad
  void updateExpired() {
    if (lastcontactdate == null) {
      setLastContactDate(Convutils.getNewDateTime().minusMonths(1));
    }
    if (expirationtime == null) {
      this.expired = false;
      expirationtime = 60 * 60; // 1 hour default
    }
    if (expirationtime == 0) {
      this.expired = false;
    } else {
      this.expired = lastcontactdate.plusSeconds(getExpirationtime()).isBeforeNow();
    }
  }

  /**
   * @return the previousstate
   */
  public String getPreviousState() {
    return previousstate;
  }

  /**
   * @param previousstate the previousstate to set
   */
  public void setPreviousState(String previousstate) {
    this.previousstate = previousstate;
  }

  public boolean isExpired() {
    return expired;
  }

  // Accessors below
  /**
   * @return the expirationtime in seconds
   */
  public Integer getExpirationtime() {
    return expirationtime;
  }

  /**
   * @param expirationtime the expirationtime in seconds to set
   */
  public void setExpirationtime(Integer expirationtime) {
    this.expirationtime = expirationtime;
  }

  /**
   * @return the lastcontactdate
   */
  public DateTime getLastContactDate() {
    return lastcontactdate;
  }

  /**
   * @param lastcontactdate the lastcontactdate to set
   */
  public void setLastContactDate(DateTime lastcontactdate) {
    this.lastcontactdate = lastcontactdate;
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

  /**
   * Returns the {@code id} of the {@link Controller}.
   */
  public Long getId() {
    return id;
  }

  /**
   * Sets the {@code id} of the {@link Controller}.
   *
   * @param id the Id to set
   */
  public void setId(Long id) {
    this.id = id;
  }

  /**
   * Returns the {@code owner} of the {@link Controller}.
   */
  public String getOwner() {
    return owner;
  }

  /**
   * Sets the {@code owner} of the {@link Controller}.
   *
   * @param owner the Owner to set
   */
  public void setOwner(String owner) {
    this.owner = owner;
  }

  /**
   * Returns the {@code location} of the {@link Controller}.
   */
  public String getLocation() {
    return location;
  }

  /**
   * Sets the {@code location} of the {@link Controller}.
   *
   * @param location the location to set
   */
  public void setLocation(String location) {
    this.location = location;
  }

  /**
   * Returns the {@code zone} of the {@link Controller}.
   */
  public String getZone() {
    return zone;
  }

  /**
   * Sets the {@code zone} of the {@link Controller}.
   *
   * @param zone the zone to set
   */
  public void setZone(String zone) {
    this.zone = zone;
  }

  /**
   * Returns the {@code type} of the {@link Controller}.
   */
  public Type getType() {
    return type;
  }

  /**
   * Sets the {@code type} of the {@link Controller}.
   *
   * @param type the type to set
   */
  public void setType(Type type) {
    this.type = type;
  }

  /**
   * Returns the {@code name} of the {@link Controller}.
   */
  public String getName() {
    return name;
  }

  /**
   * Sets the {@code name} of the {@link Controller}.
   *
   * @param name the name to set
   */
  public void setName(String name) {
    this.name = name;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(getId(), getOwner(), getLocation(), getZone(), getName());
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof Controller)) {
      return false;
    }

    Controller otherController = (Controller) obj;
    return Objects.equal(this.getId(), otherController.getId())
            && Objects.equal(this.getOwner(), otherController.getOwner())
            && Objects.equal(this.getLocation(), otherController.getLocation())
            && Objects.equal(this.getZone(), otherController.getZone())
            && Objects.equal(this.getType(), otherController.getType())
            && Objects.equal(this.getName(), otherController.getName());
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(getClass().getName())
            .add("id", getId())
            .add("owner", getOwner())
            .add("location", getLocation())
            .add("zone", getZone())
            .add("type", getType())
            .add("name", getName())
            .add("desiredstate", getDesiredState())
            .add("actualstate", getActualState())
            .add("desiredstatepriority", getDesiredStatePriority())
            .add("lastdesiredstatechange", getLastDesiredStateChange())
            .add("lastactualstatechange", getLastActualStateChange())
            .add("lastcontactdate", getLastContactDate())
            .add("validstates", getValidStates())
            .add("expirationtime", getExpirationtime())
            .toString();
  }

  /**
   * @return the desiredstate
   */
  public String getDesiredState() {
    if (null == desiredstate || "".equals(desiredstate)) {
      desiredstate = "0";
    }
    return desiredstate;
  }

  /**
   * @param desiredstate the desiredstate to set
   * @see setDesiredState(String, DesiredStatePriority) instead
   */
  public void setDesiredState(String desiredstate) {
    this.desiredstate = desiredstate;
    this.lastdesiredstatechange = Convutils.getNewDateTime();
  }

  public void setDesiredStatePriority(DesiredStatePriority dsp) {
    this.desiredstatepriority = dsp;
  }

  /**
   * @return the actualstate
   */
  public String getActualState() {
    return actualstate;
  }

  /**
   * @param actualstate the actualstate to set
   */
  public void setActualState(String actualstate) {
    this.actualstate = actualstate;
    this.lastactualstatechange = Convutils.getNewDateTime();
  }

  /**
   * @return the desiredstatepriority
   */
  public DesiredStatePriority getDesiredStatePriority() {
    return desiredstatepriority;
  }

  /**
   * @return the lastdesiredstatechange
   */
  public DateTime getLastDesiredStateChange() {
    return lastdesiredstatechange;
  }

  /**
   * @param lastdesiredstatechange the lastdesiredstatechange to set
   */
  public void setLastDesiredStateChange(DateTime lastdesiredstatechange) {
    this.lastdesiredstatechange = lastdesiredstatechange;
  }

  /**
   * @return the lastactualstatechange
   */
  public DateTime getLastActualStateChange() {
    return lastactualstatechange;
  }

  /**
   * @param lastactualstatechange the lastactualstatechange to set
   */
  public void setLastActualStateChange(DateTime lastactualstatechange) {
    this.lastactualstatechange = lastactualstatechange;
  }

  /**
   * @return the validstates
   */
  public List getValidStates() {
    return validstates;
  }

  /**
   * @param validstates the validstates to set
   */
  public void setValidStates(List validstates) {
    this.validstates = validstates;
  }

}
