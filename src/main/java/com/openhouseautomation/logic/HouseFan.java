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

  WeightedDecision wd = new WeightedDecision();
  Controller controller;
  double forecasthigh;
  double insidetemp;
  int olddesiredfanspeed;
  int newfanspeed;
  boolean tocontinue;

  public static final Logger log = Logger.getLogger(HouseFan.class.getName());

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
    controller = ofy().load().type(Controller.class).filter("name", "Whole House Fan").first().now();
    if (controller == null) {
      log.log(Level.INFO, "null controller");
      return false;
    }
    return true;
  }
  public boolean considerStatePriority() {
    // check for EMERGENCY
    if (controller.getDesiredStatePriority() == Controller.DesiredStatePriority.EMERGENCY) {
      wd.addElement("DesiredStatePriority", 1, 5); // full speed
      log.log(Level.WARNING, wd.toString());
      return false;
    }
    return true;
  }

  public boolean considerControlMode() {
    // skip everything if the controller is not in AUTO
    if (controller.getDesiredStatePriority() != Controller.DesiredStatePriority.AUTO) {
      wd.addElement("DesiredStatePriority", 1000, "Not in " + Controller.DesiredStatePriority.AUTO.name());
      log.log(Level.INFO, wd.toString());
      return false;
    }
    return true;
  }

  public boolean considerTemperatures() {
    // get the inside and outside temperatures
    double outsidetemp = Utilities.getDoubleReading("Outside Temperature");
    wd.addElement("Outside Temperature", 1000, outsidetemp);
    insidetemp = Utilities.getDoubleReading("Inside Temperature");
    wd.addElement("Inside Temperature", 1000, insidetemp);
    if (outsidetemp == 0 || insidetemp == 0 || outsidetemp < -100 || outsidetemp > 150 || insidetemp < -100 || insidetemp > 150) {
      log.log(Level.INFO, "bad temperature read, outside={0}, inside={1}", new Object[]{outsidetemp, insidetemp});
      return false;
    }
    // do not run fan when outside is hotter than inside
    if (outsidetemp > (insidetemp - 1)) {
      wd.addElement("Outside vs Inside Temperature", 5, 0);
      log.log(Level.INFO, wd.toString());
      return false;
    }
    return true;
  }

  public void considerSlope() {
    // decrease fan speed if outside is warming up
    double tempslope = Utilities.getSlope("Outside Temperature", 60 * 60 * 2); // 2 hours readings
    wd.addElement("Outside Temperature Slope", 1000, tempslope);
    if (tempslope >= 1) {
      // this will make the fan slow down if temperature outside is increasing, i.e. warming up
      // to avoid hysteresis, make sure the slope is > 1 (increasing quickly)
      wd.addElement("Outside Temperature Slope", 10, -1);
    }
  }

  public void considerForecast() {
    // if the forecast high tomorrow is less than 80F, don't cool house.
    forecasthigh = Utilities.getForecastHigh("95376");
    wd.addElement("Forecast High", 1000, forecasthigh);
    if (forecasthigh == 0 || forecasthigh < -100 || forecasthigh > 150) {
      log.log(Level.INFO, "bad forecasthigh: {0}", forecasthigh);
      return;
    }
    if (forecasthigh < 80) {
      wd.addElement("Forecast High", 5, 0);
    }
  }

  public void computeDesiredSpeed() {
    // compute setpoint based on forecast high for tomorrow
    double setpoint = (forecasthigh * -2 / 5) + 102;
    int desiredfanspeedtemperatureforecast = Math.min(new Double(insidetemp - setpoint).intValue(), 5);
    wd.addElement("Setpoint", 1000, setpoint);
    wd.addElement("Fan Speed from Forecast", 20, desiredfanspeedtemperatureforecast);
  }

  public void processFanChange() {
    // code to update the whf controllers' desired speed next
    olddesiredfanspeed = Integer.parseInt(controller.getDesiredState());
    wd.addElement("Old Fan Speed", 1000, olddesiredfanspeed);

    // now, what does the weighted decision say?
    newfanspeed = olddesiredfanspeed;
    int newdesiredfanspeed = (Integer) wd.getTopValue();
    log.log(Level.INFO, "trying for fan speed: " + newdesiredfanspeed + " because of: "  + wd.getTopName());
    
    if (olddesiredfanspeed < newdesiredfanspeed) {
      newfanspeed++;
    }
    if (olddesiredfanspeed > newdesiredfanspeed) {
      newfanspeed--;
    }
    // bounds checking
    newfanspeed = Math.min(newfanspeed, 5);
    newfanspeed = Math.max(newfanspeed, 0);

    // if no changes are necessary
    if (olddesiredfanspeed == newfanspeed) {
      log.log(Level.INFO, wd.toString());
      log.log(Level.INFO, "No changes needed");
      return;
    }
    // save new speed
    controller.setDesiredState(Integer.toString(newfanspeed));
    ofy().save().entity(controller);
    log.log(Level.WARNING, "Changed fan speed: {0} -> {1}", new Object[]{olddesiredfanspeed, newfanspeed});
    sendNotification();
  }

  public void sendNotification() {
    // if fan speed changed, send notification
    // yes, it will send a lot of debug mail during this testing phase
    // in the future, either send only 2 notifs/day (on and off), or use IM or pub/sub
    boolean tosend = Boolean.parseBoolean(DatastoreConfig.getValueForKey("send mail", "true"));
    if (!tosend) return;
    MailNotification mnotif = new MailNotification();
    mnotif.setBody(wd.toMessage());
    mnotif.setRecipient(DatastoreConfig.getValueForKey("e-mail sender", "davras@gmail.com"));
    mnotif.setSubject("Fan Speed change: " + olddesiredfanspeed + " -> " + newfanspeed);
    mnotif.sendNotification();
  }
}
