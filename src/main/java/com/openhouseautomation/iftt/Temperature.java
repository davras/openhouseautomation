/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.openhouseautomation.iftt;

import com.openhouseautomation.logic.HouseFan;
import com.openhouseautomation.model.DatastoreConfig;
import com.openhouseautomation.notification.NotificationHandler;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author dras
 */
public class Temperature extends DeferredSensor {

  public static final Logger log = Logger.getLogger(Temperature.class.getName());
  @Override
  public void run() {
    Float fold = null, fnew = null;
    try {
      fold = Float.parseFloat(super.sensor.getPreviousReading());
      fnew = Float.parseFloat(super.sensor.getLastReading());
    } catch (NumberFormatException e) {
    }
    if (Objects.equals(fold, fnew)) {
      return;
    }

    if (sensor.getName().equals("Outside Temperature")
            || sensor.getName().equals("Inside Temperature")) {
      HouseFan hf = new HouseFan();
      hf.process();
      log.log(Level.INFO, "Decision:" + hf.getWeightedDecision().toMessage());
    }
  }
  
  public void logTestNotification() {
    if (super.sensor.getId() == 2154791004L) {
      NotificationHandler nhnotif = new NotificationHandler();
      nhnotif.setRecipient(DatastoreConfig.getValueForKey("admin"));
      nhnotif.setSubject("Outside Temperature");
      nhnotif.setBody("Outside Temperature: " + super.sensor.getLastReading());
      nhnotif.alwaysSend();
    }
  }
}
