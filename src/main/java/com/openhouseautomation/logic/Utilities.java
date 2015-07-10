package com.openhouseautomation.logic;

import static com.openhouseautomation.OfyService.ofy;
import com.openhouseautomation.model.Reading;
import com.openhouseautomation.model.Sensor;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.math3.stat.regression.SimpleRegression;

/**
 *
 * @author dras
 */
public class Utilities {

  private static final Logger log = Logger.getLogger(Utilities.class.getName());

  public static double getSlope(Long id, int seconds) {
    // get the Sensor
    Sensor sens = ofy().load().type(Sensor.class).id(id).now();
    // get the readings for that sensor
    //TODO MUST convert to joda time
    log.log(Level.INFO, "checking readings after " + (System.currentTimeMillis() - (seconds * 1000)) + " ms-epoch-utc");
    List<Reading> readings = ofy().load().type(Reading.class)
            .ancestor(sens).filter("timestamp >", System.currentTimeMillis() - (seconds * 1000)).list();
    // setup the linear regression
    SimpleRegression sreg = new SimpleRegression(false); // do not compute the intercept, just the slope
    int loggedreadings = 0;
    for (Reading readhistory : readings) {
      // get the slope over hours, because:
      // -1F change in 3600 seconds = -0.00028
      // -1F change in 1 hour = -1
      // TODO but this means your lookback time is in seconds, and the returned slope is in minutes
      long histval = readhistory.getTimestamp().getMillis() / 1000 / 60;
      if (loggedreadings++ < 6) {
        log.log(Level.INFO, "read history: {0}->{1}={2}", new Object[]{readhistory.getTimestamp(), histval, readhistory.getValue()});
      }
      sreg.addData(readhistory.getTimestamp().getMillis() / 1000 / 60, Double.parseDouble(readhistory.getValue()));
    }

    return sreg.getSlope();
  }

// convenience method to use "Inside Temperature"
  public static
          double getSlope(String name, int seconds) {
    Sensor sens = ofy().load().type(Sensor.class
    ).filter("name", name).first().now();
    return getSlope(sens.getId(), seconds);
  }

  // convenience method to use long for id
  public static double getSlope(long id, int seconds) {
    return getSlope(new Long(id), seconds);
  }

  public static
          double getReading(String name) {
    Sensor sens = ofy().load().type(Sensor.class
    ).filter("name", name).first().now();
    return Double.parseDouble(sens.getLastReading());
  }
}
