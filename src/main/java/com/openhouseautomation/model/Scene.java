package com.openhouseautomation.model;

import com.google.common.base.Objects;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

/**
 * Class representing a scene.
 *
 * @author davras@gmail.com (David Ras)
 */
@Entity
@Cache
public class Scene {

  @Id
  public Long id;
  public String name; // The displayable name of the scene
  public String config; // The JSON string for the scene configuration
    
  /**
   * Empty constructor for objectify.
   */
  public Scene() {
  }

  /**
   * Returns the {@code id} of the {@link Scene}.
   */
  public Long getId() {
    return id;
  }

  /**
   * Sets the {@code id} of the {@link Scene}.
   *
   * @param id the Id to set
   */
  public void setId(Long id) {
    this.id = id;
  }

  /**
   * Returns the {@code name} of the {@link Scene}.
   */
  public String getName() {
    return name;
  }

  /**
   * Sets the {@code name} of the {@link Scene}.
   *
   * @param name the name to set
   */
  public void setName(String name) {
    this.name = name;
  }

    /**
   * Returns the {@code name} of the {@link Scene}.
   */
  public String getConfig() {
    return config;
  }

  /**
   * Sets the {@code name} of the {@link Scene}.
   *
   * @param name the name to set
   */
  public void setConfig(String config) {
    this.config = config;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(getId(), getName());
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof Scene)) {
      return false;
    }

    Scene otherScene = (Scene) obj;
    return Objects.equal(this.getId(), otherScene.getId())
        && Objects.equal(this.getName(), otherScene.getName());
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(getClass().getName())
        .add("id", getId())
        .add("name", getName())
        .add("config", getConfig())
        .toString();
  }
}
