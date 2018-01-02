/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.openhouseautomation.notification;

import com.openhouseautomation.Convutils;
import static com.openhouseautomation.OfyService.ofy;
import com.openhouseautomation.model.DatastoreConfig;
import com.openhouseautomation.model.NotificationLog;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author dras
 */
public class NotificationHandler {

  private static final long serialVersionUID = 1L;
  private static final Logger log = Logger.getLogger(NotificationHandler.class.getName());

  String body = "";
  String recipient = "";
  String subject = "";

  public void setBody(String s) {
    this.body = s;
  }

  public String getBody() {
    return body;
  }

  public void setRecipient(String s) {
    this.recipient = s;
  }

  public String getRecipient() {
    return recipient;
  }

  public void setSubject(String s) {
    this.subject = s;
  }

  public String getSubject() {
    return subject;
  }

  public void send() {
    log.log(Level.INFO, "starting send(" + subject + ")");
    if (recipient == null || "".equals(recipient)) {
      recipient = DatastoreConfig.getValueForKey("admin", "bob@example.com");
    }
    if (!duplicatePage()) {
      // get the notification entry for this subject
      NotificationLog nl = ofy().load().type(NotificationLog.class)
              .filter("subject", subject).first().now();
      if (nl == null) {
        nl = new NotificationLog();
        nl.setRecipient(recipient);
        nl.setSubject(subject);
      }
      nl.setLastnotification(Convutils.getNewDateTime());
      nl.setBody(body);
      ofy().save().entity(nl).now();
    }
  }

  public void page() {
    if (!duplicatePage()) {
      recipient = DatastoreConfig.getValueForKey("pager", DatastoreConfig.getValueForKey("admin", "nobody@example.com"));
      MailNotification mn = new MailNotification();
      mn.send(this);
    } else {
      log.log(Level.INFO, "suppressing duplicate page");
    }
  }

  public boolean duplicatePage() {
    // get the notification entry for this subject
    NotificationLog nl = ofy().load().type(NotificationLog.class)
            .filter("subject", subject).first().now();
    if (nl == null) {
      // happens when there are no previous notifications
      return false;
    }
    nl.setLastnotification(Convutils.getNewDateTime());
    nl.setBody(body);
    ofy().save().entity(nl).now();
    // returns true if the last notification was within the last 4 hrs.
    // returns false otherwise
    return !nl.getLastnotification().isBefore(Convutils.getNewDateTime().minusHours(4));
  }
}
