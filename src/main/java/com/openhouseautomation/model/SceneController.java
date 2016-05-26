/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.openhouseautomation.model;

/**
 *
 * @author dave
 */
public class SceneController {
  public Long id; //id from CRC32 hash of owner, location, zone, and salt.
  public String desiredState; //What the controller wants the state to be
  public String name; // The name of the device

  /**
   * @return the id
   */
  public Long getId() {
    return id;
  }

  /**
   * @param id the id to set
   */
  public void setId(Long id) {
    this.id = id;
  }

  /**
   * @return the desiredstate
   */
  public String getDesiredState() {
    return desiredState;
  }

  /**
   * @param desiredState the desiredstate to set
   */
  public void setDesiredState(String desiredState) {
    this.desiredState = desiredState;
  }

  /**
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * @param name the name to set
   */
  public void setName(String name) {
    this.name = name;
  }
  
}
