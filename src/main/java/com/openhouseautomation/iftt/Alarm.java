package com.openhouseautomation.iftt;

import com.google.apphosting.api.ApiProxy;
import com.openhouseautomation.model.DatastoreConfig;
import com.openhouseautomation.notification.NotificationHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author dave
 */
public class Alarm extends DeferredController {
  
  public static final Logger log = Logger.getLogger(Alarm.class.getName());
  
  public Alarm() {
  }

  @Override
  public void run() {
    if (super.controller.getActualState().equals(super.controller.getPreviousState())) {
      return;
    }
    if (super.controller.getActualState().equals("Not Ready")
            && super.controller.getLastActualStateChange().plusMinutes(5).isBeforeNow()) {
      // a door or window was left open for > 5 mins
      NotificationHandler nhnotif = new NotificationHandler();
      nhnotif.setRecipient(DatastoreConfig.getValueForKey("admin"));
      nhnotif.setSubject("Alarm Not Ready");
      nhnotif.setBody("Door/Window left open");
      nhnotif.alwaysSend();
      log.log(Level.FINE, "Remaining: " +  ApiProxy.getCurrentEnvironment().getRemainingMillis() + "ms");
      return;
    }
  }
}