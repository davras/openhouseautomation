/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.openhouseautomation.notification;

import static com.openhouseautomation.OfyService.ofy;
import com.openhouseautomation.model.NotificationLog;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.joda.time.DateTime;

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
    if (checkForNotificationInhibit()) {
      log.log(Level.INFO, "Not notifying");
      return;
    }
    // send xmpp first
    // if that fails, send e-mail
    XMPPNotification xmppnotif = new XMPPNotification();
    if (!xmppnotif.send(this)) {
      // then e-mail instead
      MailNotification mnotif = new MailNotification();
      mnotif.send(this);
    }
    // assuming one of the above was successful, add it to the notif log
    NotificationLog nl = new NotificationLog();
    nl.setLastnotification(new DateTime());
    nl.setRecipient(recipient);
    nl.setSubject(subject);
    ofy().save().entity(nl).now();
  }

  public boolean checkForNotificationInhibit() {
    // returning true aborts the notification
    // query for the last notification
    NotificationLog nl = ofy()
            .load()
            .type(NotificationLog.class)
            .filter("subject", subject)
            .order("-lastnotification")
            .first()
            .now();
    if (nl == null) {
      return true;
    }
    if (nl.getLastnotification().plusHours(1).isAfterNow()) {
      return false;
    }
    return false;
  }
}
