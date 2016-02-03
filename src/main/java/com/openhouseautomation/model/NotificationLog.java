/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.openhouseautomation.model;

import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import org.joda.time.DateTime;

/**
 *
 * @author dras
 */
@Entity
@Cache
public class NotificationLog {

  @Id private Long id; //id for the log event, automatic
  private String recipient; // Who got the notification
  private DateTime lastnotification; // The Date the last time the notification was sent
  @Index private String subject;

  /**
   * Empty constructor for objectify.
   */
  public NotificationLog() {
  }

  /**
   * @return the recipient of the notification
   */
  public String getRecipient() {
    return recipient;
  }

  /**
   * @param recipient the recipient of the notification
   */
  public void setRecipient(String recipient) {
    this.recipient = recipient;
  }

  /**
   * @return the Date the last notification was sent
   */
  public DateTime getLastnotification() {
    return lastnotification;
  }

  /**
   * @param lastnotification the Date the last notification was sent
   */
  public void setLastnotification(DateTime lastnotification) {
    this.lastnotification = lastnotification;
  }

  /**
   * @return the subject
   */
  public String getSubject() {
    return subject;
  }

  /**
   * @param subject the subject to set
   */
  public void setSubject(String subject) {
    this.subject = subject;
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
  
  

}
