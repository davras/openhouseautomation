package com.openhouseautomation.mapreduce;

import static com.openhouseautomation.OfyService.ofy;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.repackaged.org.joda.time.DateTime;
import com.google.appengine.repackaged.org.joda.time.DateTimeZone;
import com.google.appengine.repackaged.org.joda.time.format.DateTimeFormat;
import com.google.appengine.repackaged.org.joda.time.format.DateTimeFormatter;
import com.google.appengine.tools.mapreduce.Mapper;
import com.openhouseautomation.model.DatastoreConfig;
import com.openhouseautomation.model.Reading;
import com.openhouseautomation.model.Sensor;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author dras
 */
public class ReadingMapper extends Mapper<Entity, String, String> {

  private static final long serialVersionUID = 1L;
  private static final Logger log = Logger.getLogger(ReadingMapper.class.getName());

  @Override
  public void map(Entity entreading) {
    // try {
    // DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    // Key parkey = entreading.getParent();
    // Entity parent = datastore.get(parkey);
    // String type = (String) parent.getProperty("type");
    Reading reading = ofy().load().fromEntity(entreading);
    if (reading == null) {
      log.log(Level.SEVERE, "null reading");
    }
    Sensor sensor = ofy().load().now(reading.getSensor());
    if (sensor == null) {
      log.log(Level.SEVERE, "null sensor");
    }
    String sentitydate = getDateString(reading.getTimestamp());
    String value = reading.getValue(); // value of reading
    emit(sensor.getType() + ":" + sensor.getId() + ":" + sentitydate, value);
    // emits the form: TEMPERATURE:38382834:20140417, 72
    // where TYPE:SENSORID:DATE,READING
    // } catch (EntityNotFoundException e) {
    // }
  }

  public synchronized String getDateString(DateTime d) {
    String timezone = DatastoreConfig.getValueForKey("timezone", "America/Los_Angeles");
    DateTime dtlocal = d.withZone(DateTimeZone.forID(timezone));
    // entityday should be formatted as yyyymmdd, like 20140214 for feb 14, 2014
    DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyyMMdd");
    return dtlocal.toString(fmt);
  }
}
