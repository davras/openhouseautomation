/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.openhouseautomation.iftt;

import com.openhouseautomation.model.DatastoreConfig;
import com.openhouseautomation.notification.NotificationHandler;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author dave
 */
public class Rain extends DeferredSensor {

  public static final Logger log = Logger.getLogger(Rain.class.getName());

  public Rain() {
  }

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

    // now that we are done sanity checking
    NotificationHandler nhnotif = new NotificationHandler();
    nhnotif.setRecipient(DatastoreConfig.getValueForKey("admin"));
    nhnotif.setSubject("Rain Sensor Change");
    if (fold < 0.011 && fnew > 0.023) {
      // takes 2 bucket tips to alert (0.022 inches of rain)
      nhnotif.setBody("Started Raining: " + fnew + " inches/hr");
    }
    if (fold < 0.1 && fnew > 0.1) {
      nhnotif.setBody("Raining Heavy: " + fnew + " inches/hr");
    }
    if (fold > 0.021 && fnew < 0.02) {
      nhnotif.setBody("Rain stopped");
    }
    if (nhnotif.getBody() != null && !"".equals(nhnotif.getBody())) {
      nhnotif.send();
    } else {
      log.log(Level.WARNING, "Rain tried to send an empty body");
    }

  }
}
