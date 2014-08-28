package com.openhouseautomation.mapreduce;

import static com.openhouseautomation.OfyService.ofy;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.tools.mapreduce.Mapper;
import com.openhouseautomation.model.Reading;
import com.openhouseautomation.model.Sensor;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
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

  public synchronized String getDateString(Date d) {
    Date d2 = new Date(d.getTime()-7*60*60*1000);
    // entityday should be formatted as yyyymmdd, like 20140214 for feb 14, 2014
    DateFormat formatter = new SimpleDateFormat("yyyyMMdd");
    return formatter.format(d2);
  }
}
