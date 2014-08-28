package com.openhouseautomation.model;

import com.google.common.base.Objects;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;


/**
 * Class representing a controller device.
 * 
 * @author jfmontesdeoca@google.com (Jose Montes de Oca)
 */
@Entity
@Index
public class Controller {
  /**
   * Enum for the type Device
   */
  public enum Type {
    THERMOSTAT,
    GARAGEDOOR,
    ALARM,
    LIGHT,
    SPRINKLER;
  }
  
  @Id String id;
  String owner;
  String location;
  String zone;
  Type type;
  String name;

  /**
   * Empty constructor for objectify.
   */
  private Controller() {}

  /**
   * Returns the {@code id} of the {@link Controller}.
   */
  public String getId() {
    return id;
  }

  /**
   * Sets the {@code id} of the {@link Controller}.
   *
   * @param id the Id to set
   */
  public void setId(String id) {
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
    return Objects.hashCode(id,
        owner,
        location,
        zone,
        name);
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof Controller)) {
      return false;
    }
    
    Controller otherController = (Controller) obj;
    return Objects.equal(this.id, otherController.getId())
        && Objects.equal(this.owner, otherController.getOwner())
        && Objects.equal(this.location, otherController.getLocation())
        && Objects.equal(this.zone, otherController.getZone())
        && Objects.equal(this.type, otherController.getType())
        && Objects.equal(this.name, otherController.getName());
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
        .toString();
  }
}
