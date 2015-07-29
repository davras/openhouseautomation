package com.openhouseautomation.logic;

import static com.openhouseautomation.OfyService.ofy;
import com.openhouseautomation.model.Forecast;
import com.openhouseautomation.model.Reading;
import com.openhouseautomation.model.Sensor;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.joda.time.DateTime;

/**
 *
 * @author dras
 */
public class Utilities {

  private static final Logger log = Logger.getLogger(Utilities.class.getName());

  /**
   *
   * @param id
   * @param seconds
   * @return
   */
  public static double getSlope(Long id, int seconds) {
    // get the Sensor
    Sensor sens = ofy().load().type(Sensor.class).id(id).now();
    // get the readings for that sensor
    DateTime dt = new DateTime().minusSeconds(seconds);
    List<Reading> readings = ofy().load().type(Reading.class)
            .ancestor(sens).filter("timestamp >", dt).list();
    // setup the linear regression
    SimpleRegression sreg = new SimpleRegression(true);
    int loggedreadings = 0;
    double firsttime = 0;
    for (Reading readhistory : readings) {
      double histval = readhistory.getTimestamp().getMillis() / 1000 / 60 / 100;
      // reference later times to the zero time of the first reading
      if (firsttime == 0) {
        firsttime = histval;
        histval = 0;
      } else {
        histval -= firsttime;
      }
      sreg.addData(histval, Double.parseDouble(readhistory.getValue()));
      loggedreadings++;
    }
    log.log(Level.INFO, "read {0} values\nslope is: delta T of {1}F/hr", new Object[]{loggedreadings, sreg.getSlope()});
    return sreg.getSlope();
  }

// convenience method to use "Inside Temperature"
  /**
   * Convenience method to use a string name of sensor like "Inside Temperature"
   *
   * @param name of the sensor
   * @param seconds number of seconds to look back in history to determine slope
   * @return the slope of the readings from (now-seconds) to now
   */
  public static double getSlope(String name, int seconds) {
    Sensor sens = ofy().load().type(Sensor.class).filter("name", name).first().now();
    return getSlope(sens.getId(), seconds);
  }

  // convenience method to use long for id
  /**
   *
   * @param id
   * @param seconds
   * @return
   */
  public static double getSlope(long id, int seconds) {
    return getSlope(new Long(id), seconds);
  }

  /**
   *
   * @param name
   * @return
   */
  public static double getDoubleReading(String name) {
    Sensor sens = ofy().load().type(Sensor.class).filter("name", name).first().now();
    return Double.parseDouble(sens.getLastReading());
  }

  public static double getForecastHigh(String zipcode) {
    Forecast forecast = ofy().load().type(Forecast.class).id(zipcode).now();
    return Double.parseDouble(forecast.getForecastHigh());
  }
}
