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
      fold = Float.parseFloat(super.oldsensor.getLastReading());
      fnew = Float.parseFloat(super.newsensor.getLastReading());
    } catch (NumberFormatException e) {
    }
    if (Objects.equals(fold, fnew)) {
      return;
    }

    if (oldsensor.getName().equals("Outside Temperature") || oldsensor.getName().equals("Inside Temperature")) {
      new HouseFan().process();
    }
  }
}
