package com.openhouseautomation;

import com.openhouseautomation.model.Controller;
import com.openhouseautomation.model.Forecast;
import com.openhouseautomation.model.Location;
import com.openhouseautomation.model.Reading;
import com.openhouseautomation.model.ReadingHistory;
import com.openhouseautomation.model.Sensor;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyFactory;
import com.googlecode.objectify.ObjectifyService;

/**
 * Custom version of the ObjectifyService to register application model class.
 *
 * @author Jose Montes de Oca
 */
public class OfyService {
  static {
    factory().register(Controller.class);
    factory().register(Sensor.class);
    factory().register(Reading.class);
    factory().register(ReadingHistory.class);
    factory().register(Forecast.class);
    factory().register(Location.class);
  }

  public static Objectify ofy() {
    return ObjectifyService.ofy();
  }

  public static ObjectifyFactory factory() {
    return ObjectifyService.factory();
  }
}
