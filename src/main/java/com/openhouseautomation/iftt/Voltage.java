/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.openhouseautomation.iftt;

import com.openhouseautomation.Convutils;
import static com.openhouseautomation.OfyService.ofy;
import com.openhouseautomation.logic.Utilities;
import com.openhouseautomation.model.Controller;
import com.openhouseautomation.model.DatastoreConfig;
import com.openhouseautomation.notification.NotificationHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.joda.time.DateTime;

/**
 *
 * @author dave
 */
public class Voltage extends DeferredSensor {

  public static final Logger log = Logger.getLogger(Voltage.class.getName());

  public Voltage() {
  }

  @Override
  public void run() {
    Float outsidebattery = null;
    try {
      outsidebattery = Float.parseFloat(super.sensor.getLastReading());
    } catch (NumberFormatException e) {
    }
    DateTime now = Convutils.getNewDateTime();
    if (outsidebattery < 12.0) {
        setController(2624005855L, "1");
    }
    if (outsidebattery > 13.5) {
        setController(2624005855L, "0");
    }
  }

  public void setController(Long controllerid, String state) {
    if (com.openhouseautomation.Flags.clearCache) {
      //ofy().clear(); // clear the session cache, not the memcache
    }
    Controller controller = ofy().load().type(Controller.class).id(controllerid).now();
    if (controller != null && !controller.getDesiredState().equals(state)) {
      controller.setDesiredState(state);
      ofy().save().entity(controller).now();
      NotificationHandler nhnotif = new NotificationHandler();
      nhnotif.setRecipient(DatastoreConfig.getValueForKey("admin", "bob@example.com"));
      nhnotif.setSubject("Den Lights");
      if ("1".equals(state)) {
        nhnotif.setBody("Outside Weather Station Charger On");
      } else {
        nhnotif.setBody("Outside Weather Station Charger Off");
      }
      nhnotif.send();
    }
  }
}
