package com.openhouseautomation.logic;

import com.openhouseautomation.model.DatastoreConfig;
import com.openhouseautomation.notification.NotificationHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author davras
 */
public class HouseFan {

  // triggered by ifttt for auto control
  // triggered by cron/15m for notification in manual
  
  public static final Logger log = Logger.getLogger(HouseFan.class.getName());
  WeightedDecision wd;
  HouseFanImpl hfl = new HouseFanImpl();
  
  private void process() {
    hfl.process();
    wd = hfl.getWeightedDecision();
  }

  public WeightedDecision getWeightedDecision() {
    return wd;
  }
  public void autoControlWHF() {
    process();
    // code to update the whf controllers' desired speed next
    Object toparsetopvalue = wd.getTopValue();
    if (null == toparsetopvalue) {
      return;
    }
    int newfanspeed = Utilities.safeParseInt(toparsetopvalue);
    log.log(Level.INFO, "trying for fan speed: " + newfanspeed + " because of: " + wd.getTopName());
    // bounds checking
    newfanspeed = ensureRange(newfanspeed, 0, 5);
    hfl.setDesiredState(Integer.toString(newfanspeed));
    sendNotification(newfanspeed);
  }
  
  public String notifyInManual() {
    // If it is in MANUAL, and it needs to run to cool, send page.
    // If desiredstatepriority != auto and FCH>80F and Outside<Inside, then page
    process();
    int newfanspeed = Utilities.safeParseInt(wd.getTopValue());
    if (newfanspeed > 0 && !hfl.getAutoControlFlag()) {
      String toret = "Recommend WholeHouseFan in AUTO to start cooling";
      log.log(Level.WARNING, toret);
      return toret;
    }
    return "";
  }

  public int ensureRange(int value, int min, int max) {
    return Math.min(Math.max(value, min), max);
  }

  public void sendNotification(int fanspeed) {
    NotificationHandler nhnotif = new NotificationHandler();
    nhnotif.setRecipient(DatastoreConfig.getValueForKey("admin", "bob@example.com"));
    nhnotif.setSubject("Fan Speed");
    nhnotif.setBody("Fan Speed change to " + fanspeed);
    nhnotif.send();
  }
}
