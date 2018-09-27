package com.openhouseautomation.logic;

import com.openhouseautomation.Convutils;
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

  // the more expensive checks are later on, so bail out early if possible.
  public void process() {
    // load controller
    if (!setup()) {
      return;
    }
    if (!considerStatePriority()) {
      return;
    }
    if (!considerControlMode()) {
      return;
    }
    // load 2 temperature readings
    if (!considerTemperatures()) {
      return;
    }
    if (hotterOutside()) {
      // forces the fan to turn off when it is hotter outside than inside
      processFanChange();
      return;
    }
    // load forecast readings
    if (!considerForecast()) {
      return;
    }
    considerSlope();
    computeDesiredSpeed();
    checkDoorWearInhibit();
    processFanChange();
  }

  public String notifyInManual() {
    // if the fan is in manual, but the house needs to be cooled down, notify admin
    if (!setup()) {
      return "";
    }
    if (controller.getDesiredStatePriority() != Controller.DesiredStatePriority.MANUAL) {
      return "";
    }
    considerTemperatures();
    if (hotterOutside()) {
      return "";
    }
    // load forecast readings
    if (!considerForecast()) {
      return "";
    }
    computeDesiredSpeed();
    newfanspeed = safeParseInt(wd.getTopValue());
    if (newfanspeed > 0) {
      String toret = "Outside: " + outsidetemp + "\nInside: " + insidetemp + "\n:Forecast: " + forecasthigh
              + "\nFan controller: " + Controller.DesiredStatePriority.MANUAL.name();
      log.log(Level.WARNING, toret);
      return toret;
    }
    return "";
  }

  public int safeParseInt(Object o) {
    if (o instanceof java.lang.String) {
      return Integer.parseInt((String) o);
    }
    return (Integer) o;
  }

  public boolean setup() {
    ofy().clear(); // clear session cache, not memcache
    controller = ofy().load().type(Controller.class).filter("name", "Whole House Fan").first().now();
    if (controller == null) {
      log.log(Level.SEVERE, "Controller not found: Whole House Fan");
      return false;
    }
    if (controller.isExpired()) {
      log.log(Level.WARNING, "Controller offline: {0}",
              Convutils.timeAgoToString(controller.getLastContactDate()));
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
      return false;
    }
    return true;
  }

  public boolean considerTemperatures() {
    // get the inside and outside temperatures
    outsidetemp = Utilities.getDoubleReading("Outside Temperature");
    insidetemp = Utilities.getDoubleReading("Inside Temperature");
    //log.log(Level.WARNING, "Outside: " + outsidetemp + " Inside: " + insidetemp);
    wd.addElement("Reading Outside Temperature", 1000, outsidetemp);
    wd.addElement("Reading Inside Temperature", 1000, insidetemp);
    if (outsidetemp == 0 || insidetemp == 0 || outsidetemp < -100
            || outsidetemp > 150 || insidetemp < -100 || insidetemp > 150) {
      log.log(Level.WARNING, "bad temperature read, outside={0}, inside={1}",
              new Object[]{outsidetemp, insidetemp});
      return false;
    }
    return true;
  }

  public boolean hotterOutside() {
    // do not run fan when outside is hotter than inside
    if (outsidetemp > (insidetemp - 1)) {
      wd.addElement("It is hotter outside than inside", 5, 0);
      return true; // return true so that fan speed processing will happen
    }
    return false;
  }

  public boolean considerForecast() {
    // if the forecast high tomorrow is less than 80F, don't cool house.
    forecasthigh = Utilities.getForecastHigh("95376");
    wd.addElement("Reading Forecast High", 1000, forecasthigh);
    if (forecasthigh == 0 || forecasthigh < -100 || forecasthigh > 150) {
      log.log(Level.INFO, "bad forecasthigh: {0}", forecasthigh);
      return false;
    }
    if (forecasthigh < 80) {
      wd.addElement("Forecast High < 80F", 5, 0);
      return false;
    }
    return true;
  }

  public void considerSlope() {
    // decrease fan speed if outside is warming up
    double outsidetemperatureslope = Utilities.getSlope("Outside Temperature", 60 * 60); // 1 hours readings
    wd.addElement("Reading Outside Temperature Slope", 1000, outsidetemperatureslope);
    double insidetemperatureslope = Utilities.getSlope("Inside Temperature", 60 * 60); // 1 hours readings
    wd.addElement("Reading Inside Temperature Slope", 1000, insidetemperatureslope);
    double outsidelightslope = Utilities.getSlope("Outside Light Level", 60 * 60); // 1 hours readings
    wd.addElement("Reading Outside Light Slope", 1000, outsidelightslope);

    if (outsidetemperatureslope > 0.01
            && insidetemperatureslope > 0.01
            && outsidelightslope > 0.01) {
      wd.addElement("Temperature Slope", 6, 0);
    }
  }

  public void computeDesiredSpeed() {
    // compute setpoint based on forecast high for tomorrow
    setpoint = (forecasthigh * -2 / 5) + 100;
    int desiredfanspeedtemperatureforecast = Math.min(new Double(insidetemp - setpoint).intValue(), 5);
    wd.addElement("Recording Setpoint", 1000, setpoint);
    wd.addElement("Fan Speed from Forecast", 20, desiredfanspeedtemperatureforecast);
  }

  public void shouldTurnOff() {
    // only turn the fan off if insidetemperature is more than 2 deg below setpoint
    if ((insidetemp + 1) < setpoint) {
      wd.addElement("Shutdown Door Wear Prevention", 7, olddesiredfanspeed);
    }
  }

  public void shouldTurnOn() {
    if ((insidetemp - 1) > setpoint) {
      wd.addElement("Startup Door Wear Prevention", 7, olddesiredfanspeed);
    }
  }

  public void checkDoorWearInhibit() {
    olddesiredfanspeed = safeParseInt(controller.getDesiredState());
    wd.addElement("Reading Old Fan Speed", 1000, olddesiredfanspeed);
    olddesiredfanspeed = safeParseInt(controller.getDesiredState());
    // now, what does the weighted decision say?
    newfanspeed = safeParseInt(wd.getTopValue());
    // check hysteresis
    if (newfanspeed == 0 && olddesiredfanspeed == 1) {
      shouldTurnOff();
    }
    if (newfanspeed == 1 && olddesiredfanspeed == 0) {
      shouldTurnOn();
    }
  }

  public void processFanChange() {
    // code to update the whf controllers' desired speed next

    log.log(Level.INFO, "trying for fan speed: " + safeParseInt(wd.getTopValue()) + " because of: " + wd.getTopName());
    // bounds checking
    newfanspeed = ensureRange(newfanspeed, 0, 5);
    // if no changes are necessary
    if (olddesiredfanspeed == newfanspeed) {
      log.log(Level.INFO, "No changes needed, old=" + olddesiredfanspeed + ", new=" + newfanspeed);
      return;
    }
    // otherwise, save new speed
    controller.setDesiredState(Integer.toString(newfanspeed));
    controller.setDecision(wd.toJSONString());
    ofy().save().entity(controller).now();
    log.log(Level.WARNING, "Changed fan speed: {0} -> {1}", new Object[]{olddesiredfanspeed, newfanspeed});
    if (olddesiredfanspeed == 0 || newfanspeed == 0) {
      // log the event
      EventLog etl = new EventLog();
      etl.setIp("127.0.0.1");
      etl.setNewState(Integer.toString(newfanspeed));
      etl.setPreviousState(Integer.toString(olddesiredfanspeed));
      etl.setType("Auto change fan speed");
      etl.setUser(this.getClass().getName());
      ofy().save().entity(etl);
      sendNotification();
    }
  }

  public int ensureRange(int value, int min, int max) {
    return Math.min(Math.max(value, min), max);
  }

  public void sendNotification() {
    NotificationHandler nhnotif = new NotificationHandler();
    nhnotif.setRecipient(DatastoreConfig.getValueForKey("admin", "bob@example.com"));
    nhnotif.setSubject("Fan Speed");
    nhnotif.setBody("Fan Speed change: " + olddesiredfanspeed + " -> " + newfanspeed);
    nhnotif.send();
  }

  public void saveDecision() {
    if (controller == null) {
      controller = ofy().load().type(Controller.class).filter("name", "Whole House Fan").first().now();
    }
  }
}
