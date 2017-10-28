/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.openhouseautomation.iftt;

import com.openhouseautomation.Convutils;
import static com.openhouseautomation.OfyService.ofy;
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
public class Light extends DeferredSensor {

  public static final Logger log = Logger.getLogger(Light.class.getName());

  public Light() {
  }

  @Override
  public void run() {
    Float outsidelight = null;
    try {
      outsidelight = Float.parseFloat(super.sensor.getLastReading());
    } catch (NumberFormatException e) {
    }
    DateTime now = Convutils.getNewDateTime();
    int curhour = now.getHourOfDay();
    boolean lights = false;
    if (outsidelight < 70) {
      lights = true;
    }
    // turn off between midnight and 6am
    if (curhour < 6) {
      lights = false;
    }
    // turn off if alarm is armed
    Controller controller = ofy().load().type(Controller.class).id(3964578029L).now();
    if (controller != null && controller.getActualState().equals("Away")) {
      lights = false;
    }
    if (lights) {
      setController(3640433672L, "1");
      log.log(Level.INFO, "Den light on");
    } else {
      setController(3640433672L, "0");
      log.log(Level.INFO, "Den light off");
    }
  }

  public void setController(Long controllerid, String state) {
    if (com.openhouseautomation.Flags.clearCache) {
      ofy().clear(); // clear the session cache, not the memcache
    }
    Controller controller = ofy().load().type(Controller.class).id(controllerid).now();
    if (controller != null && !controller.getDesiredState().equals(state)) {
      controller.setDesiredState(state);
      ofy().save().entity(controller).now();
      NotificationHandler nhnotif = new NotificationHandler();
      nhnotif.setRecipient(DatastoreConfig.getValueForKey("admin", "bob@example.com"));
      nhnotif.setSubject("Den Lights");
      if ("1".equals(state)) {
        nhnotif.setBody("Lights On");
      } else {
        nhnotif.setBody("Lights Off");
      }
      nhnotif.send();
    }
  }
}
