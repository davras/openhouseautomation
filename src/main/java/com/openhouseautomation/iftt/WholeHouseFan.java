package com.openhouseautomation.iftt;

import com.openhouseautomation.logic.HouseFan;
import java.util.Objects;

/**
 *
 * @author dave
 */
public class WholeHouseFan extends DeferredSensor {

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
      new HouseFan().process();
    }
  }
}
