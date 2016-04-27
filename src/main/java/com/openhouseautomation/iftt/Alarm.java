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
      nhnotif.sendWithoutNotificationLogging();
      return;
    }
    NotificationHandler nhnotif = new NotificationHandler();
    nhnotif.setRecipient(DatastoreConfig.getValueForKey("admin"));
    nhnotif.setSubject("Alarm State Change");
    nhnotif.setBody("Alarm: " + super.controller.getActualState());
    nhnotif.sendWithoutNotificationLogging();
  }
}
