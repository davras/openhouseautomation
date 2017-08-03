package com.openhouseautomation.iftt;

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
    log.log(Level.INFO, "Alarm trigger: actual={0}, previous={1}, lastactualstatechange={2}",
            new Object[]{super.controller.getActualState(),
              super.controller.getPreviousState(),
              super.controller.getLastActualStateChange()});
    if (super.controller.getActualState().equals(super.controller.getPreviousState())) {
      return;
    }
    if (super.controller.getActualState().equals("Not Ready")
            && super.controller.getLastActualStateChange().plusMinutes(5).isBeforeNow()) {
      // a door or window was left open for > 5 mins
      NotificationHandler nhnotif = new NotificationHandler();
      nhnotif.setRecipient(DatastoreConfig.getValueForKey("admin", "bob@example.com"));
      nhnotif.setSubject("Alarm Not Ready");
      nhnotif.setBody("Door/Window left open");
      nhnotif.send();
    }
  }
}
