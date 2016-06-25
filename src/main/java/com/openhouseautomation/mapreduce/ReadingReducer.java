package com.openhouseautomation.mapreduce;

import com.google.appengine.tools.mapreduce.Reducer;
import com.google.appengine.tools.mapreduce.ReducerInput;
import static com.openhouseautomation.OfyService.ofy;
import com.openhouseautomation.model.ReadingHistory;
import com.openhouseautomation.model.Sensor;
import com.googlecode.objectify.Key;
import com.openhouseautomation.Convutils;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author dras
 */
public class ReadingReducer extends Reducer<String, String, ReadingHistory> {

  private static final long serialVersionUID = 1L;
  private static final Logger LOG = Logger.getLogger(ReadingReducer.class.getName());

  @Override
  public void reduce(String key, ReducerInput<String> values) {
    LOG.log(Level.INFO, "reducing: {0}", key);
    if (key.startsWith("TEMPERATURE") || key.startsWith("HUMIDITY")) {
      reduceHighLow(key, values);
    } else if (key.startsWith("LIGHT")) {
      reduceTotal(key, values);
    } else if (key.startsWith("WINDSPEED")) {
      reduceHigh(key, values);
    }
    // TODO reduce rain volume
  }

  public void reduceHighLow(String key, ReducerInput<String> values) {
    String high = "-9999";
    String low = "9999";
    Float val = 0f;
    while (values.hasNext()) {
      String value = values.next();
      val = Float.parseFloat(value);
      if (val > Float.parseFloat(high)) {
        high = value;
      }
      if (val < Float.parseFloat(low)) {
        low = value;
      }
    }
    StringTokenizer st1 = new StringTokenizer(key, ":");
    String sensortype = st1.nextToken();
    String sensorid = st1.nextToken();
    String readingdate = st1.nextToken();
    ReadingHistory rhist = new ReadingHistory();
    rhist.setSensor(Key.create(Sensor.class, Long.parseLong(sensorid)));
    rhist.setHigh(high);
    rhist.setLow(low);
    rhist.setTimestamp(Convutils.convertStringDate(readingdate));
    rhist.setId(sensorid + "." + readingdate);
    ofy().save().entity(rhist); // async
  }

  public void reduceAvgNonZero(String key, ReducerInput<String> values) {
    Float totalval = 0f;
    Float avgval = 0f;
    int readings = 0;
    while (values.hasNext()) {
      String value = values.next();
      float fval = Float.parseFloat(value);
      if (fval > 0) {
        totalval += fval;
        readings++;
      }
    }
    StringTokenizer st1 = new StringTokenizer(key, ":");
    String sensortype = st1.nextToken();
    String sensorid = st1.nextToken();
    String readingdate = st1.nextToken();
    ReadingHistory rhist = new ReadingHistory();
    rhist.setSensor(Key.create(Sensor.class, Long.parseLong(sensorid)));
    rhist.setAverage(Float.toString(totalval / readings));
    rhist.setTimestamp(Convutils.convertStringDate(readingdate));
    rhist.setId(sensorid + "." + readingdate);
    ofy().save().entity(rhist); // async
  }

  public void reduceTotal(String key, ReducerInput<String> values) {
    Float totalval = 0f;
    while (values.hasNext()) {
      String value = values.next();
      float fval = Float.parseFloat(value);
      totalval += fval;
    }
    StringTokenizer st1 = new StringTokenizer(key, ":");
    String sensortype = st1.nextToken();
    String sensorid = st1.nextToken();
    String readingdate = st1.nextToken();
    ReadingHistory rhist = new ReadingHistory();
    rhist.setSensor(Key.create(Sensor.class, Long.parseLong(sensorid)));
    rhist.setTotal(Float.toString(totalval));
    rhist.setTimestamp(Convutils.convertStringDate(readingdate));
    rhist.setId(sensorid + "." + readingdate);
    ofy().save().entity(rhist); // async
  }

  public void reduceHigh(String key, ReducerInput<String> values) {
    String high = "-9999";
    while (values.hasNext()) {
      String value = values.next();
      if (Float.parseFloat(value) > Float.parseFloat(high)) {
        high = value;
      }
    }
    StringTokenizer st1 = new StringTokenizer(key, ":");
    String sensortype = st1.nextToken();
    String sensorid = st1.nextToken();
    String readingdate = st1.nextToken();
    ReadingHistory rhist = new ReadingHistory();
    rhist.setSensor(Key.create(Sensor.class, Long.parseLong(sensorid)));
    rhist.setHigh(high);
    rhist.setTimestamp(Convutils.convertStringDate(readingdate));
    rhist.setId(sensorid + "." + readingdate);
    ofy().save().entity(rhist); // async
  }

}
