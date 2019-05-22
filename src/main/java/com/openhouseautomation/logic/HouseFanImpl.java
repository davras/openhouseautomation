/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.openhouseautomation.logic;

import com.openhouseautomation.Convutils;
import static com.openhouseautomation.OfyService.ofy;
import static com.openhouseautomation.logic.HouseFan.log;
import com.openhouseautomation.model.Controller;
import com.openhouseautomation.model.EventLog;
import com.openhouseautomation.model.Sensor;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author dave
 */
public class HouseFanImpl {

  WeightedDecision wd = new WeightedDecision();
  Controller controller;
  double forecasthigh;
  double insidetemp;
  int oldfanspeed;
  int newfanspeed;
  double setpoint;
  double outsidetemp;
  boolean autocontrolflag;
  public static final Logger LOG = Logger.getLogger(HouseFanImpl.class.getName());

  // for testing
  public WeightedDecision getWeightedDecision() {
    return wd;
  }

  public boolean getAutoControlFlag() {
    return autocontrolflag;
  }
  
  // the more expensive checks are later on, so bail out early if possible.
  // if the method returns true, processing continues
  // if it returns false, no further processing, to save datastore reads/writes
  public void process() {
    int stage = 0;
    switch (stage) {
      case 0:
        if (!setup()) {
          // if you do not have a WHF, this exits
          break;
        }
      case 1:
        if (!checkExpiration()) {
          // if the controller hasn't reported in awhile, exit
          break;
        }
      case 2:
        if (!considerStatePriority()) {
          // EMERGENCY has priority 1
          // Help clear smoke from house/cooking fire
          // If desiredstatepriority = emergency, fan speed=5
          break;
        }
      case 3:
        if (!considerControlMode()) {
          // MANUAL has priority 2
          // If it is in MANUAL or LOCAL, don’t automatically control
          // if desiredstatepriority != auto, do not alter fan speed
          break;
        }
      case 4:
        if (!stopInTheMorning()) {
          // priority 6
          
          break;
        }
      case 5:
        if (!considerForecast()) {
          // priority 5 to not run if FCH < 80F
          break;
        }
      case 6:
        if (!considerTemperatures()) {
          break;
        }
      case 7:
        if (!hotterOutside()) {
          // priority 5
          // Only use the fan if it is colder outside than inside
          // if outsidetemperature > insidetemperature, fan speed=0 (off)
          break;
        }
      case 8:
        if (!computeDesiredSpeed()) {
          // priority 20
          break;
        }
      case 9:
        if (!checkDoorWearInhibit()) {
          // priority 7
          // Don’t keep starting and stopping the fan (wears out the doors)
          // if fan speed > 0 and inside temperature < (setpoint-1) turn fan off
          // if fan speed ==0 and inside temperature > (setpoint+1) turn fan on
          break;
        }
      case 10:
        if (!checkFanSpeedChange()) {
          break;
        }
    }
  }

  public boolean setup() {
    ofy().clear(); // clear session cache, not memcache
    controller = ofy().load().type(Controller.class).filter("name", "Whole House Fan").first().now();
    if (controller == null) {
      LOG.log(Level.SEVERE, "Controller not found: Whole House Fan");
      return false;
    }
    return true;
  }

  public boolean checkExpiration() {
    if (controller.isExpired()) {
      LOG.log(Level.WARNING, "Controller offline: {0}",
              Convutils.timeAgoToString(controller.getLastContactDate()));
      return false;
    }
    return true;
  }

  public boolean considerStatePriority() {
    // check for EMERGENCY
    if (controller.getDesiredStatePriority() == Controller.DesiredStatePriority.EMERGENCY) {
      wd.addElement("DesiredStatePriority=EMERGENCY", 1, 5); // full speed
      return false;
    }
    return true;
  }

  public boolean considerControlMode() {
    // skip everything if the controller is not in AUTO
    if (controller.getDesiredStatePriority() != Controller.DesiredStatePriority.AUTO) {
      wd.addElement("DesiredStatePriority=MANUAL", 2, controller.getActualState());
      autocontrolflag = false;
    } else {
      wd.addElement("DesiredStatePriority=AUTO,current speed@" + controller.getActualState(), 1000, 0);
      autocontrolflag = true;
    }
    return autocontrolflag;
  }

  public boolean considerTemperatures() {
    // get the inside and outside temperatures
    //outsidetemp = Utilities.getDoubleReading("Outside Temperature");
    //insidetemp = Utilities.getDoubleReading("Inside Temperature");

    // TODO workaround because the static cache works for inside temp, but not outside.
    Sensor souttemp = ofy().load().type(Sensor.class).id(2587739430L).now();
    outsidetemp = Double.parseDouble(souttemp.getLastReading());
    Sensor sintemp = ofy().load().type(Sensor.class).id(395430086L).now();
    insidetemp = Double.parseDouble(sintemp.getLastReading());

    wd.addElement("Reading Outside Temperature", 1000, outsidetemp);
    wd.addElement("Reading Inside Temperature", 1000, insidetemp);
    if (outsidetemp == 0 || insidetemp == 0 || outsidetemp < -100
            || outsidetemp > 150 || insidetemp < -100 || insidetemp > 150) {
      LOG.log(Level.SEVERE, "bad temperature read, outside={0}, inside={1}",
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
    return true;
  }

  public boolean considerForecast() {
    // if the forecast high tomorrow is less than 80F, don't cool house.
    forecasthigh = Utilities.getForecastHigh("95376");
    wd.addElement("Reading Forecast High", 1000, forecasthigh);
    if (forecasthigh == 0 || forecasthigh < -100 || forecasthigh > 150) {
      LOG.log(Level.INFO, "bad forecasthigh: {0}", forecasthigh);
    }
    if (forecasthigh < 80) {
      wd.addElement("Forecast High < 80F", 5, 0);
      return false;
    }
    return true;
  }

  public boolean stopInTheMorning() {
    // stop fan in morning
    int currenthour = Convutils.getNewDateTime().getHourOfDay();
    if (currenthour > 6 && currenthour < 19) {
      wd.addElement("Off at 7am, On at 8pm, currently@" + currenthour, 6, 0);
      return false;
    }
    return true;
  }

  public boolean computeDesiredSpeed() {
    // compute setpoint based on forecast high for tomorrow
    setpoint = (forecasthigh * -2 / 5) + 100;
    int desiredfanspeedtemperatureforecast = Math.min(new Double(insidetemp - setpoint).intValue(), 5);
    wd.addElement("Recording Setpoint", 1000, setpoint);
    wd.addElement("Fan Speed from Forecast", 20, desiredfanspeedtemperatureforecast);
    return true;
  }

  public boolean shouldTurnOff() {
    // only turn the fan off if insidetemperature is more than 1 deg below setpoint
    if ((insidetemp + 1) > setpoint) {
      wd.addElement("Shutdown Door Wear Prevention", 7, oldfanspeed);
      return false;
    }
    return true;
  }

  public boolean shouldTurnOn() {
    if ((insidetemp - 1) < setpoint) {
      wd.addElement("Startup Door Wear Prevention", 7, oldfanspeed);
      return false;
    }
    return true;
  }

  public boolean checkDoorWearInhibit() {
    oldfanspeed = Utilities.safeParseInt(controller.getActualState());
    wd.addElement("Wear Inhibit, reading old fan speed", 1000, oldfanspeed);
    // now, what does the weighted decision say?
    newfanspeed = Utilities.safeParseInt(wd.getTopValue());
    // check hysteresis
    if (newfanspeed == 0 && oldfanspeed == 1) {
      if (!shouldTurnOff()) {
        return false;
      }
    }
    if (newfanspeed == 1 && oldfanspeed == 0) {
      if (!shouldTurnOn()) {
        return false;
      }
    }
    return true;
  }

  public boolean checkFanSpeedChange() {
    // return true if a fan speed change is needed
    // return false if fan speed is unchanged
    return Utilities.safeParseInt(controller.getActualState()) != Utilities.safeParseInt(wd.getTopValue());
  }

  public void setDesiredState(String desstate) {
    if (controller.getActualState().equals(desstate)) {
      // no changes needed
      log.log(Level.INFO, "No change in fan speed: {0} = {1}", new Object[]{oldfanspeed, newfanspeed});
      return;
    }
    //save new speed
    controller.setDesiredState(desstate);
    ofy().save().entity(controller).now();
    log.log(Level.WARNING, "Changed fan speed: {0} -> {1}", new Object[]{oldfanspeed, newfanspeed});
    if (oldfanspeed == 0 || newfanspeed == 0) {
      // log the event
      EventLog etl = new EventLog();
      etl.setIp("127.0.0.1");
      etl.setNewState(Integer.toString(newfanspeed));
      etl.setPreviousState(Integer.toString(oldfanspeed));
      etl.setType("Auto change fan speed");
      etl.setUser(this.getClass().getName());
      ofy().save().entity(etl).now();
    }
  }
}
