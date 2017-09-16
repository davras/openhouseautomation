package com.openhouseautomation.iftt;

import com.openhouseautomation.model.DatastoreConfig;
import com.openhouseautomation.notification.NotificationHandler;
import java.util.Objects;

public class Wind extends DeferredSensor {

  public Wind() {
  }

  @Override
  public void run() {
    Float fold = null, fnew = null;
    try {
      fold = Float.parseFloat(super.sensor.getPreviousReading());
      fnew = Float.parseFloat(super.sensor.getLastReading());
    } catch (NumberFormatException e) {
    }
    if (Objects.equals(fold, fnew)) return;

    // now that we are done sanity checking
    NotificationHandler nhnotif = new NotificationHandler();
    nhnotif.setRecipient(DatastoreConfig.getValueForKey("admin", "bob@example.com"));
    nhnotif.setSubject("High Wind Warning");
    if (fold < 30 && fnew > 30) {
      nhnotif.setBody("High Winds: " + fnew + " mph");
    }
    nhnotif.page();
  }
}
