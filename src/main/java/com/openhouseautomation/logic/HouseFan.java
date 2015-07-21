package com.openhouseautomation.logic;

import static com.openhouseautomation.OfyService.ofy;
import com.openhouseautomation.model.Controller;
import com.openhouseautomation.model.DatastoreConfig;
import com.openhouseautomation.notification.MailNotification;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author davras
 */
public class HouseFan {

  public static final Logger log = Logger.getLogger(HouseFan.class.getName());

  public void process() {
    // get the inside and outside temperatures
    double outsidetemp = Utilities.getDoubleReading("Outside Temperature");
    double insidetemp = Utilities.getDoubleReading("Inside Temperature");
    double tempslope = 0;
    double forecasthigh = 0;
    double setpoint = 0;
    int fanspeedchange = 0; // no change by default
    int desiredfanspeed = 0; // off by default

    // figure out if fan needs to speed up or slow down, store change in fanspeedchange.
    // is outside colder than inside?
    log.log(Level.INFO,
            "Outside Temperature: {0}" + "\n" + "Inside Temperature: {1}", new Object[]{outsidetemp, insidetemp});
    if (outsidetemp < (insidetemp - 1)) {
      // and the temperature outside is falling
      tempslope = Utilities.getSlope("Outside Temperature", 60 * 60 * 2); // 2 hours readings
      log.log(Level.INFO, "Outside Temperature Slope: {0}", Double.toString(tempslope));
      if (tempslope < 0) {
        forecasthigh = Utilities.getForecastHigh("95376");
        setpoint = (forecasthigh * -2 / 5) + 102;
        desiredfanspeed = Math.min(new Double(insidetemp - setpoint).intValue(), 5);
        log.log(Level.INFO, "Forecast High: {0}" + "\n" + "Setpoint: {1}" + "\n" + "Desired Speed: {2}",
                new Object[]{Double.toString(forecasthigh), Double.toString(setpoint), Integer.toString(desiredfanspeed)});
      } else {
        // it's warming up outside, so turn off the fan to preserve the cool
        fanspeedchange = -1;
      }
    } else {
      // hotter outside than inside
      fanspeedchange = -1;
    }
    // code to update the whf controllers' desired speed next
    Controller controller = ofy().load().type(Controller.class).filter("name", "Whole House Fan").first().now();
    if (controller.getDesiredStatePriority() == Controller.DesiredStatePriority.AUTO) {
      int oldfanspeed = Integer.parseInt(controller.getDesiredState());
      if (oldfanspeed < desiredfanspeed) {
        fanspeedchange = 1;
      }
      int newfanspeed = oldfanspeed + fanspeedchange;
      if (newfanspeed < 0) {
        newfanspeed = 0;
      }
      if (newfanspeed > 5) {
        newfanspeed = 5;
      }
      if (oldfanspeed == desiredfanspeed) {
        return;
      }
      controller.setDesiredState(Integer.toString(newfanspeed));
      ofy().save().entity(controller);

      // if fan speed changed, send notification
      // yes, it will send a lot of debug mail during this testing phase
      // in the future, either send only 2 notifs/day (on and off), or use IM or pub/sub
      MailNotification mnotif = new MailNotification();
      mnotif.setBody(
              "Outside Temperature: " + outsidetemp + "\n"
              + "Inside Temperature: " + insidetemp + "\n"
              + "Outside Temperature Slope: " + tempslope + "\n"
              + "Forecast High: " + forecasthigh + "\n"
              + "Setpoint: " + setpoint + "\n"
              + "New Fan speed: " + newfanspeed + "\n"
              + "Old Fan speed was: " + oldfanspeed + "\n"
              + "Controller Desired State Priority: " + controller.getDesiredStatePriority().toString() + "\n"
      );
      mnotif.setRecipient(DatastoreConfig.getValueForKey("e-mail sender", "davras@gmail.com"));
      mnotif.setSubject("Fan Speed change: " + oldfanspeed + " -> " + newfanspeed);
      mnotif.sendNotification();
    }
  }
}
