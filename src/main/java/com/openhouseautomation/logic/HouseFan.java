package com.openhouseautomation.logic;

import static com.openhouseautomation.OfyService.ofy;
import com.openhouseautomation.model.Controller;
import com.openhouseautomation.model.DatastoreConfig;
import com.openhouseautomation.model.EventLog;
import com.openhouseautomation.notification.NotificationHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author davras
 */
public class HouseFan {

  WeightedDecision wd = new WeightedDecision();
  Controller controller;
  double forecasthigh;
  double insidetemp;
  int olddesiredfanspeed;
  int newfanspeed;
  boolean tocontinue;
  double setpoint;
  double outsidetemp;
  public static final Logger log = Logger.getLogger(HouseFan.class.getName());

  // for testing
  public WeightedDecision getWeightedDecision() {
    return wd;
  }

  public void process() {
    if (!setup()) {
      return;
    }
    if (!considerStatePriority()) {
      return;
    }
    if (!considerControlMode()) {
      return;
    }
    if (!considerTemperatures()) {
      return;
    }
    considerSlope();
    considerForecast();
    computeDesiredSpeed();
    processFanChange();
  }

  public boolean setup() {
    ofy().clear(); // clear session cache, not memcache
    controller = ofy().load().type(Controller.class).filter("name", "Whole House Fan").first().now();
    if (controller == null) {
      log.log(Level.SEVERE, "null controller");
      return false;
    }
    return true;
  }

  public boolean considerStatePriority() {
    if (controller == null) {
      if (!setup()) {
        return false;
      }
    }
    // check for EMERGENCY
    if (controller.getDesiredStatePriority() == Controller.DesiredStatePriority.EMERGENCY) {
      wd.addElement("DesiredStatePriority", 1, 5); // full speed
      log.log(Level.WARNING, wd.toString());
      return false;
    }
    return true;
  }

  public boolean considerControlMode() {
    if (controller == null) {
      if (!setup()) {
        return false;
      }
    }
    // skip everything if the controller is not in AUTO
    if (controller.getDesiredStatePriority() != Controller.DesiredStatePriority.AUTO) {
      wd.addElement("Reading DesiredStatePriority", 1000, "Not in " + Controller.DesiredStatePriority.AUTO.name());
      log.log(Level.INFO, wd.toString());
      return false;
    }
    return true;
  }

  public boolean considerTemperatures() {
    // get the inside and outside temperatures
    outsidetemp = Utilities.getDoubleReading("Outside Temperature");
    wd.addElement("Reading Outside Temperature", 1000, outsidetemp);
    insidetemp = Utilities.getDoubleReading("Inside Temperature");
    wd.addElement("Reading Inside Temperature", 1000, insidetemp);
    if (outsidetemp == 0 || insidetemp == 0 || outsidetemp < -100 || outsidetemp > 150 || insidetemp < -100 || insidetemp > 150) {
      log.log(Level.INFO, "bad temperature read, outside={0}, inside={1}", new Object[]{outsidetemp, insidetemp});
      return false;
    }
    // do not run fan when outside is hotter than inside
    if (outsidetemp > (insidetemp - 1)) {
      wd.addElement("It is hotter outside than inside", 5, 0);
      log.log(Level.INFO, wd.toString());
      return false;
    }
    return true;
  }

  public void considerSlope() {
    // decrease fan speed if outside is warming up
    double tempslope = Utilities.getSlope("Outside Temperature", 60 * 60 * 2); // 2 hours readings
    wd.addElement("Reading Outside Temperature Slope", 1000, tempslope);
    if (tempslope >= 0.1) {
      // this will make the fan slow down if temperature outside is increasing, i.e. warming up
      // to avoid hysteresis, make sure the slope is > 0.1 (increasing)
      // but don't slow fan if outside is much colder than inside
      if ((Utilities.getDoubleReading("Outside Temperature") + 5) > Utilities.getDoubleReading("Inside Temperature")) {
        wd.addElement("Outside Temperature Slope", 10, 0);
      } else {
        if (Integer.parseInt(controller.getActualState()) > 0) {
          wd.addElement("Colder outside, no fan speed change", 20, controller.getActualState());
        }
      }
    }
  }

  public void considerForecast() {
    // if the forecast high tomorrow is less than 80F, don't cool house.
    forecasthigh = Utilities.getForecastHigh("95376");
    wd.addElement("Reading Forecast High", 1000, forecasthigh);
    if (forecasthigh == 0 || forecasthigh < -100 || forecasthigh > 150) {
      log.log(Level.INFO, "bad forecasthigh: {0}", forecasthigh);
      return;
    }
    if (forecasthigh < 80) {
      wd.addElement("Forecast High < 80F", 5, 0);
    }
  }

  public void computeDesiredSpeed() {
    // compute setpoint based on forecast high for tomorrow
    setpoint = (forecasthigh * -2 / 5) + 102;
    int desiredfanspeedtemperatureforecast = Math.min(new Double(insidetemp - setpoint).intValue(), 5);
    wd.addElement("Recording Setpoint", 1000, setpoint);
    wd.addElement("Fan Speed from Forecast", 20, desiredfanspeedtemperatureforecast);
  }

  public boolean shouldTurnOff() {
    // only turn the fan off if insidetemperature is more than 2 deg below setpoint
    if ((insidetemp + 1) < setpoint) {
      wd.addElement("Door Wear Prevention", newfanspeed, log);
      return true;
    }
    return false;
  }

  public boolean shouldTurnOn() {
    if ((insidetemp - 1) > setpoint) {
      return true;
    }
    return false;
  }

  public void processFanChange() {
    // code to update the whf controllers' desired speed next
    olddesiredfanspeed = Integer.parseInt(controller.getDesiredState());
    wd.addElement("Reading Old Fan Speed", 1000, olddesiredfanspeed);

    // now, what does the weighted decision say?
    newfanspeed = olddesiredfanspeed;
    int newdesiredfanspeed = (Integer) wd.getTopValue();
    log.log(Level.INFO, "trying for fan speed: " + newdesiredfanspeed + " because of: " + wd.getTopName());

    if (olddesiredfanspeed < newdesiredfanspeed) {
      newfanspeed++;
    }
    if (olddesiredfanspeed > newdesiredfanspeed) {
      newfanspeed--;
    }
    // bounds checking
    newfanspeed = ensureRange(newfanspeed, 0, 5);
    // create hysteresis to prevent damper door motor wear
    if ((olddesiredfanspeed == 0 && newfanspeed == 1 && !shouldTurnOn()) ||
    (olddesiredfanspeed == 1 && newfanspeed == 0 && !shouldTurnOff())) {
      newfanspeed = olddesiredfanspeed;
      wd.addElement("Damper Door Motor Wear Inhibitor", 8, newfanspeed);
    }
    // if no changes are necessary
    log.log(Level.INFO, wd.toString());
    if (olddesiredfanspeed == newfanspeed) {
      log.log(Level.INFO, "No changes needed");
      return;
    }
    // log the event
    EventLog etl = new EventLog();
    etl.setIp("127.0.0.1");
    etl.setNewState(Integer.toString(newfanspeed));
    etl.setPreviousState(Integer.toString(olddesiredfanspeed));
    etl.setType("Auto change fan speed");
    etl.setUser(this.getClass().getName());
    ofy().save().entity(etl);

    // save new speed
    controller.setDesiredState(Integer.toString(newfanspeed));
    ofy().save().entity(controller);
    log.log(Level.WARNING, "Changed fan speed: {0} -> {1}", new Object[]{olddesiredfanspeed, newfanspeed});
    //if (olddesiredfanspeed == 0 || newfanspeed == 0) {
      sendNotification();
    //}
  }

  public int ensureRange(int value, int min, int max) {
    return Math.min(Math.max(value, min), max);
  }

  public void sendNotification() {
    NotificationHandler nhnotif = new NotificationHandler();
    nhnotif.setBody(wd.toMessage());
    nhnotif.setRecipient(DatastoreConfig.getValueForKey("admin"));
    nhnotif.setSubject("Fan Speed");
    nhnotif.setBody("Fan Speed change: " + olddesiredfanspeed + " -> " + newfanspeed);
    nhnotif.send();
  }
}
