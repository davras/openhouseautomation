package com.openhouseautomation.iftt;

import com.openhouseautomation.logic.HouseFan;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author dave
 */
public class WholeHouseFan extends DeferredSensor {

  public static final Logger log = Logger.getLogger(WholeHouseFan.class.getName());

  public WholeHouseFan() {
  }

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

    if (sensor.getName().equals("Outside Temperature")
            || sensor.getName().equals("Inside Temperature")) {
      HouseFan hf = new HouseFan();
      hf.process();
      log.log(Level.INFO, "Decision:" + hf.getWeightedDecision().toMessage());
      new HouseFan().process();
    }
  }
}
