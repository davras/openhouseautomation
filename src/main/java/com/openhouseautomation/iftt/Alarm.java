package com.openhouseautomation.iftt;

import static com.openhouseautomation.OfyService.ofy;
import com.openhouseautomation.model.Controller;
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
    // turn off the Den Light when the Alarm is set (leaving home)
    if ("Away".equals(super.controller.getActualState()) &&
            !"Away".equals(super.controller.getPreviousState())) {
      setController(3640433672L, "0");
      setController(28131427L, "#000000");
      log.log(Level.INFO, "lights off");
    }
    if ("Disarm".equals(super.controller.getActualState()) &&
            !"Disarm".equals(super.controller.getPreviousState())) {
      setController(3640433672L, "1");
      setController(28131427L, "#ffffff");
      log.log(Level.INFO, "lights on");
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
      nhnotif.setSubject("Lights");
      if ("1".equals(state)) {
        nhnotif.setBody("Lights On");
      } else {
        nhnotif.setBody("Lights Off");
      }
      nhnotif.send();
    }
  }
}
