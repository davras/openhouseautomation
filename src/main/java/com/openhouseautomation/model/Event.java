/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.openhouseautomation.model;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.OnSave;
import org.joda.time.DateTime;

/**
 *
 * @author dave
 */
@Entity
public class Event {

  @Id
  public Long id;
  // type of event, like an action, changing a controller, etc.
  public String type;
  // the user that caused the action
  public String user;
  // the src ip of the user that caused the action
  public String ip;
  // the previous state
  public String previousstate;
  // the new state
  public String newstate;
  // the time this event was logged, set automatically when the Event is saved and will not be overwritten by multiple saves
  @Index
  public DateTime eventtime;

  @OnSave
  void setEventTime() {
    if (eventtime == null) {
      eventtime = new DateTime();
    }
  }

  public DateTime getEventTime() {
    return eventtime;
  }

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
   * @return the type
   */
  public String getType() {
    return type;
  }

  /**
   * @param type the type to set
   */
  public void setType(String type) {
    this.type = type;
  }

  /**
   * @return the user
   */
  public String getUser() {
    return user;
  }

  /**
   * @param user the user to set
   */
  public void setUser(String user) {
    this.user = user;
  }

  /**
   * @return the ip
   */
  public String getIp() {
    return ip;
  }

  /**
   * @param ip the ip to set
   */
  public void setIp(String ip) {
    this.ip = ip;
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

  /**
   * @return the newstate
   */
  public String getNewState() {
    return newstate;
  }

  /**
   * @param newstate the newstate to set
   */
  public void setNewState(String newstate) {
    this.newstate = newstate;
  }
}
