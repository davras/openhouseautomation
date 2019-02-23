/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.openhouseautomation.iftt;

import static com.openhouseautomation.OfyService.ofy;
import com.openhouseautomation.logic.HouseFan;
import com.openhouseautomation.model.Controller;
import com.openhouseautomation.model.Sensor;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author dras
 */
public class Temperature extends DeferredSensor {

  public static final Logger log = Logger.getLogger(Temperature.class.getName());

  @Override
  public void run() {
    Float fold = null, fnew = null;
    try {
      fold = Float.parseFloat(super.sensor.getPreviousReading());
      fnew = Float.parseFloat(super.sensor.getLastReading());
    } catch (NumberFormatException e) {
    }
    if (Objects.equals(fold, fnew)) {
      return;
    }

    if (sensor.getType().equals(Sensor.Type.TEMPERATURE)) {
      HouseFan hf = new HouseFan();
      hf.process();
      log.log(Level.INFO, "Temperature.WHF.Decision:" + hf.getWeightedDecision().toMessage());
    }
  }
}
