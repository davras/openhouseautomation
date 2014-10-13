package com.openhouseautomation.model;

import com.google.common.base.Objects;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import java.util.Date;

/**
 * Class representing a controller device.
 *
 * @author jfmontesdeoca@google.com (Jose Montes de Oca)
 */
@Entity
@Index
@Cache
public class Controller {

  /**
   * Enum for the Device type
   */
  public enum Type {

    THERMOSTAT,
    GARAGEDOOR,
    ALARM,
    LIGHT,
    SPRINKLER,
    WHOLEHOUSEFAN;
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
  public Type type; //The type of device
  public String name; // The name of the device
  public String desiredstate; //What the controller wants the state to be
  public String actualstate; //The actual state of the device in real life
  public DesiredStatePriority desiredstatepriority;  // The priority of the desired state, lower priority changes should be ignored
  public Date lastdesiredstatechange; // The Date the last time the desired state changed
  public Date lastactualstatechange; // The Date the last time the desired state changed

  /**
   * Empty constructor for objectify.
   */
  public Controller() {
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
        .toString();
  }

  /**
   * @return the desiredstate
   */
  public String getDesiredState() {
    return desiredstate;
  }

  /**
   * @param desiredstate the desiredstate to set
   * @see setDesiredState(String, DesiredStatePriority) instead
   */
  public void setDesiredstate(String desiredstate) {
    // don't use
  }

  public void setDesiredState(String desiredstate, DesiredStatePriority dsp) {
    // find current priority
    int curpri = getPriorityInt(desiredstatepriority);
    int setpri = getPriorityInt(dsp);
    if (curpri < setpri) { // higher number is higher priority
      this.desiredstate = desiredstate;
      this.desiredstatepriority = dsp;
    }
  }

  public int getPriorityInt(DesiredStatePriority dsp) {
    int i = -1;
    for (DesiredStatePriority dspit : DesiredStatePriority.values()) {
      i++;
      if (dspit == dsp) {
        break;
      }
    }
    return i;
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
  public Date getLastDesiredStateChange() {
    return lastdesiredstatechange;
  }

  /**
   * @param lastdesiredstatechange the lastdesiredstatechange to set
   */
  public void setLastDesiredStateChange(Date lastdesiredstatechange) {
    this.lastdesiredstatechange = lastdesiredstatechange;
  }

  /**
   * @return the lastactualstatechange
   */
  public Date getLastActualStateChange() {
    return lastactualstatechange;
  }

  /**
   * @param lastactualstatechange the lastactualstatechange to set
   */
  public void setLastActualStateChange(Date lastactualstatechange) {
    this.lastactualstatechange = lastactualstatechange;
  }
}
