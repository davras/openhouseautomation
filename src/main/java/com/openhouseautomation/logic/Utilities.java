package com.openhouseautomation.logic;

import com.googlecode.objectify.Key;
import com.openhouseautomation.Convutils;
import static com.openhouseautomation.OfyService.ofy;
import com.openhouseautomation.model.Forecast;
import com.openhouseautomation.model.Reading;
import com.openhouseautomation.model.Sensor;
import java.util.HashMap;
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
    if (com.openhouseautomation.Flags.clearCache) {
      ofy().clear(); // clear the session cache, not the memcache
    }    // get the Sensor
    Sensor sens = ofy().load().type(Sensor.class).id(id).now();
    // get the readings for that sensor
    DateTime dt = Convutils.getNewDateTime().minusSeconds(seconds);
    List<Reading> readings = ofy().load().type(Reading.class)
            .ancestor(sens).filter("timestamp >", dt).list();
    // setup the linear regression
    SimpleRegression sreg = new SimpleRegression(true);
    int loggedreadings = 0;
    double firsttime = 0;
    for (Reading readhistory : readings) {
      double histval = readhistory.getTimestamp().getMillis() / 1000;
      // reference later times to the zero time of the first reading
      if (firsttime == 0) {
        firsttime = histval;
        histval = 0;
      } else {
        histval -= firsttime;
      }
      sreg.addData(histval, Double.parseDouble(readhistory.getValue()));
      if (id == 3885021817L) {
        log.log(Level.INFO, "added reading for {0}: {1} @ {2}", new Object[]{id, readhistory.getValue(), histval});
      }
      loggedreadings++;
    }
    log.log(Level.INFO, "read {0} values\nslope has delta of {1}/hr", new Object[]{loggedreadings, sreg.getSlope()});
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
    Sensor sens = getSensor(name);
    if (sens == null) {
      return 0;
    }
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

  private static final HashMap<String, Key<Sensor>> sensorkeys = new HashMap();

  public static double getDoubleReading(String name) {
    Sensor s = getSensor(name);
    if (null != s) {
      return Double.parseDouble(s.getLastReading());
    }
    return 0;
  }

  public static void fillCache(String name) {
    log.log(Level.INFO, "filling sensor cache");
    // get the keys
    Iterable<Key<Sensor>> allsensors = ofy().load().type(Sensor.class).keys();
    for (Key<Sensor> s : allsensors) {
      Sensor stemp = ofy().load().key(s).now();
      sensorkeys.put(stemp.getName(), s);
    }
    if (!sensorkeys.containsKey(name)) {
      // didn't find that key
      log.log(Level.SEVERE, "Sensor not found: {0}", name);
      sensorkeys.put(name, null);
    }
  }

  public static Sensor getSensor(String sensorname) {
    Key<Sensor> ksensor = sensorkeys.get(sensorname);
    if (null != ksensor) {
      Sensor sensor = ofy().load().key(ksensor).now();
      return sensor;
    } else {
      fillCache(sensorname);
    }
    return null;
  }

  public static double getForecastHigh(String zipcode) {
    Forecast forecast = ofy().load().type(Forecast.class).id(zipcode).now();
    if (null == forecast.getForecastHigh() || "".equals(forecast.getForecastHigh())) {
      return 0.0;
    }
    return Double.parseDouble(forecast.getForecastHigh());
  }
}
