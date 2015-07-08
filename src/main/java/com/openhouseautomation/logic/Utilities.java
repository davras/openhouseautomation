/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.openhouseautomation.logic;

import static com.openhouseautomation.OfyService.ofy;
import com.openhouseautomation.model.Reading;
import com.openhouseautomation.model.Sensor;
import java.util.List;
import org.apache.commons.math3.stat.regression.SimpleRegression;

/**
 *
 * @author dras
 */
public class Utilities {
  public static double getSlope(Long id, int seconds) {
    // get the Sensor
    Sensor sens = ofy().load().type(Sensor.class).id(id).now();
    // get the readings for that sensor
    List<Reading> readings = ofy().load().type(Reading.class)
            .ancestor(sens).filter("timestamp >", System.currentTimeMillis()/1000-seconds).list();
    // setup the linear regression
    SimpleRegression sreg = new SimpleRegression(false); // do not compute the intercept, just the slope
    for (Reading readhistory: readings) {
      sreg.addData(readhistory.getTimestamp().getMillis()/1000 , Double.parseDouble(readhistory.getValue()));
    }
    return sreg.getSlope();
  }
  // convenience method to use "Inside Temperature"
  public static double getSlope(String name, int seconds) {
    Sensor sens = ofy().load().type(Sensor.class).filter("name", name).first().now();
    return getSlope(sens.getId(), seconds);
  }
  // convenience method to use long for id
  public static double getSlope(long id, int seconds) {
    return getSlope(new Long(id), seconds);
  }
  
  public static double getReading(String name) {
    Sensor sens = ofy().load().type(Sensor.class).filter("name", name).first().now();
    return Double.parseDouble(sens.getLastReading());
  }
}
