/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.openhouseautomation.notification;

import com.google.appengine.api.xmpp.JID;
import com.google.appengine.api.xmpp.Message;
import com.google.appengine.api.xmpp.MessageBuilder;
import com.google.appengine.api.xmpp.SendResponse;
import com.google.appengine.api.xmpp.XMPPService;
import com.google.appengine.api.xmpp.XMPPServiceFactory;
import com.google.apphosting.api.ApiProxy;
import com.openhouseautomation.model.DatastoreConfig;

/**
 *
 * @author dras
 */
public class XMPPNotification {

  public boolean send(NotificationHandler nh) {
    String sender = nh.getSender();
    String recipient = nh.getRecipient();
    
    if ("".equals(sender)) {
      // will change s~gautoard to gautoard with substring
      // will not work on Master-Slave apps
      sender = DatastoreConfig.getValueForKey("xmpp sender", "chat@" + ApiProxy.getCurrentEnvironment().getAppId().substring(2) + ".appspotchat.com");
    }
    // TODO don't send frequent notifications (> 1/hr)
    JID jidsender = new JID(sender);
    JID jidrecipient = new JID(recipient);
    Message msg = new MessageBuilder()
            .withRecipientJids(jidrecipient)
            .withBody(nh.getBody())
            .withFromJid(jidsender)
            .build();

    boolean messageSent = false;
    XMPPService xmpp = XMPPServiceFactory.getXMPPService();
    SendResponse status = xmpp.sendMessage(msg);
    messageSent = (status.getStatusMap().get(jidrecipient) == SendResponse.Status.SUCCESS);
    return messageSent;
  }
}
