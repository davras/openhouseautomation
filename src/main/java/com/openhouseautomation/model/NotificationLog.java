package com.openhouseautomation.model;

import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.openhouseautomation.Convutils;
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
  @Index private DateTime lastnotification; // The Date the last time the notification was sent
  @Index private String subject;
  private String body;

  /**
   * Empty constructor for objectify.
   */
  public NotificationLog() {
  }

  /** 
   * Sets the body of the message
   * @param body String with message to send as body
   */
  public void setBody(String body) {
    this.body = body;
  }
  
  /** 
   * Gets the body of the message
   * @return String with body of message
   */
  public String getBody() {
    return body;
  }
  /**
   * Gets the recipient of the message
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
   * Gets the DateTime the last notification was sent
   * @return the DateTime the last notification was sent
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
   * Returns a human readable string with the last notification time
   * @return String with human readable time, like "2 hours ago"
   */
  public String getHumantime() {
    return Convutils.timeAgoToString(this.lastnotification.getMillis() / 1000);
  }

  /**
   * Returns millisecond timestamp of the message
   * @return long with millisecond timestamp
   */
  public long getNotificationtime() {
    return lastnotification.getMillis();
  }
  /**
   * Gets the subject of the message
   * @return the subject
   */
  public String getSubject() {
    return subject;
  }

  /**
   * Sets the subject of the message
   * @param subject the subject to set
   */
  public void setSubject(String subject) {
    this.subject = subject;
  }

  /**
   * Gets the ID of this notification message
   * @return Long
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
