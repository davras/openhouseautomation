/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.openhouseautomation.notification;

import com.google.apphosting.api.ApiProxy;
import com.openhouseautomation.model.DatastoreConfig;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 *
 * @author dras
 */
public class MailNotification {

  public void send(NotificationHandler nh) {
    String sender = nh.getSender();
    String recipient = nh.getRecipient();
    
    if ("".equals(sender)) {
      // will change s~gautoard to gautoard with substring
      // will not work on Master-Slave apps
      sender = DatastoreConfig.getValueForKey("e-mail sender", "notification@" + ApiProxy.getCurrentEnvironment().getAppId().substring(2) + ".appspotmail.com (OpenHouseAutomation Notification)");
    }
    // TODO don't send frequent notifications (> 1/hr)
    try {
      Properties props = new Properties();
      Session session = Session.getDefaultInstance(props, null);
      Message msg = new MimeMessage(session);
      msg.setFrom(new InternetAddress(sender));
      msg.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
      msg.setSubject(nh.getSubject());
      msg.setText(nh.getBody());
      Transport.send(msg);
      // put in a log of successful notification
    } catch (Exception e) {
      // put in a log of failed notification
    }
  }
}
