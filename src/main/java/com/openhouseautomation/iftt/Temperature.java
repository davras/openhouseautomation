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
 * @author dras
 */
public class Temperature extends DeferredSensor {

  @Override
  public void run() {
    //logTestNotification();
  }
  
  public void logTestNotification() {
    if (super.sensor.getId() == 2154791004L) {
      NotificationHandler nhnotif = new NotificationHandler();
      nhnotif.setRecipient(DatastoreConfig.getValueForKey("admin"));
      nhnotif.setSubject("Outside Temperature");
      nhnotif.setBody("Outside Temperature: " + super.sensor.getLastReading());
      nhnotif.sendWithoutNotificationLogging();
    }
  }
}
