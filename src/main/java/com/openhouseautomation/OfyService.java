package com.openhouseautomation;

import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyFactory;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.impl.translate.opt.joda.JodaTimeTranslators;
import com.openhouseautomation.model.Controller;
import com.openhouseautomation.model.DatastoreConfig;
import com.openhouseautomation.model.EventLog;
import com.openhouseautomation.model.Forecast;
import com.openhouseautomation.model.LCDDisplay;
import com.openhouseautomation.model.Location;
import com.openhouseautomation.model.NotificationLog;
import com.openhouseautomation.model.Reading;
import com.openhouseautomation.model.ReadingHistory;
import com.openhouseautomation.model.Scene;
import com.openhouseautomation.model.Sensor;

/**
 * Custom version of the ObjectifyService to register application model class.
 *
 * @author Jose Montes de Oca
 */
public class OfyService {
  static {
    JodaTimeTranslators.add(ObjectifyService.factory());
    factory().register(Controller.class);
    factory().register(Sensor.class);
    factory().register(Reading.class);
    factory().register(ReadingHistory.class);
    factory().register(Forecast.class);
    factory().register(Location.class);
    factory().register(LCDDisplay.class);
    factory().register(DatastoreConfig.class);
    factory().register(Scene.class);
    factory().register(EventLog.class);
    factory().register(NotificationLog.class);
  }

  public static Objectify ofy() {
    return ObjectifyService.ofy();
  }

  public static ObjectifyFactory factory() {
    return ObjectifyService.factory();
  }
}
