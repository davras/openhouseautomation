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
    NotificationHandler nhnotif = new NotificationHandler();
    nhnotif.setRecipient(DatastoreConfig.getValueForKey("admin"));
    nhnotif.setSubject("Alarm State Change");
    nhnotif.setBody("Alarm: " + super.controller.getActualState());
    nhnotif.sendWithoutNotificationLogging();
  }
}
