/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.openhouseautomation.notification;

/**
 *
 * @author dras
 */
public class NotificationHandler {
  // send xmpp first
  // if that fails, send e-mail

  String body = "";
  String recipient = "";
  String sender = "";
  String subject = "";

  public String getSender() {
    return sender;
  }

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
    XMPPNotification xmppnotif = new XMPPNotification();
    if (!xmppnotif.send(this)) {
      // then e-mail instead
      MailNotification mnotif = new MailNotification();
      mnotif.send(this);
    }
  }
}
