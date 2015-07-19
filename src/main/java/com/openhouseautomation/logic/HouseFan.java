package com.openhouseautomation.logic;

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

    int fanspeed = 0; // off by default
    // make sure it's colder outside than inside.
    log.log(Level.INFO,
            "Outside Temperature: {0}" + "\n" + "Inside Temperature: {1}", new Object[]{outsidetemp, insidetemp});
    if (outsidetemp < (insidetemp - 1)) {
      // and the temperature outside is falling
      tempslope = Utilities.getSlope("Outside Temperature", 60 * 60 * 2); // 2 hours readings
      log.log(Level.INFO, "Outside Temperature Slope: {0}", Double.toString(tempslope));
      if (tempslope < 0) {
        double forecasthigh = Utilities.getForecastHigh("95376");
        double setpoint = (forecasthigh * -2 / 5) + 102;
        fanspeed = Math.min(new Double(insidetemp - setpoint).intValue(), 5);
        log.log(Level.INFO, "Forecast High: {0}" + "\n" + "Setpoint: {1}" + "\n" + "Speed: {2}",
                new Object[]{Double.toString(forecasthigh), Double.toString(setpoint), Integer.toString(fanspeed)});
      }
    }
    // code to update the whf controllers' desired speed next
  }
}
//    
//    MailNotification mnotif = new MailNotification();
//    mnotif.setBody(
//            "Outside Temperature is lower than Inside Temperature\n\n"
//            + "Outside Temperature: " + outsidetemp + "\n"
//            + "Inside Temperature: " + insidetemp + "\n"
//            + "Outside Temperature Slope: " + tempslope);
//    mnotif.setRecipient(
//            "davras@gmail.com");
//    mnotif.setSubject(
//            "A/C off, House Fan on");
//    //mnotif.sendNotification();
//    log.log(Level.INFO,
//            "mail sent:{0}", mnotif.getBody());
//  }
//}
//else {
//      log.log(Level.INFO, "no notification sent");
//
//    }
//  }
