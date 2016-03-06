/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.openhouseautomation.iftt;

import com.openhouseautomation.model.DatastoreConfig;
import com.openhouseautomation.notification.NotificationHandler;

/**
 *
 * @author dave
 */
public class Alarm extends DeferredController {

  public Alarm() {
  }

  @Override
  public void run() {
    if (super.oldcontroller.actualstate.equals(super.newcontroller.actualstate)) {
      return;
    }
    NotificationHandler nhnotif = new NotificationHandler();
    nhnotif.setRecipient(DatastoreConfig.getValueForKey("notificationsto"));
    nhnotif.setSubject("Alarm State Change");
    nhnotif.setBody("Alarm: " + super.newcontroller.actualstate);
    nhnotif.sendWithoutNotificationLogging();
  }
}
