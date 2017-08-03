/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.openhouseautomation.notification;

import com.openhouseautomation.model.DatastoreConfig;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 *
 * @author dras
 */
public class MailNotification {

  private static final long serialVersionUID = 1L;
  private static final Logger log = Logger.getLogger(MailNotification.class.getName());

  public void send(NotificationHandler nh) {
    //String sender = "admin@" + ApiProxy.getCurrentEnvironment().getAppId().substring(2) + ".appspotmail.com (OpenHouseAutomation Notification)";
    String sender = "davras@gmail.com";
    String recipient = nh.getRecipient();

    if ("".equals(sender)) {
      // will change s~gautoard to gautoard with substring
      // will not work on Master-Slave apps
      sender = DatastoreConfig.getValueForKey("admin", "bob@example.com");
      if (null == sender || "".equals(sender)) {
        return;
      }
    }
    try {
      Properties props = new Properties();
      Session session = Session.getDefaultInstance(props, null);
      Message msg = new MimeMessage(session);
      msg.setFrom(new InternetAddress(sender));
      msg.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
      msg.setSubject(nh.getSubject());
      msg.setText(nh.getBody());
      Transport.send(msg);
      log.log(Level.INFO, "sent e-mail:\nFrom:" + sender + "\nTo:" + recipient
              + "\nSubject:" + nh.getSubject() + "\nBody:" + nh.getBody());
      // put in a log of successful notification
    } catch (MessagingException e) {
      // put in a log of failed notification
      log.log(Level.SEVERE, "Failed to send e-mail:\nFrom:" + sender + "\nTo:" + recipient
              + "\nSubject:" + nh.getSubject() + "\nBody:" + nh.getBody());
    }
  }
}
