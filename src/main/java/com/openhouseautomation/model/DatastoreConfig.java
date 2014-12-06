package com.openhouseautomation.model;

import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Ignore;
import static com.openhouseautomation.OfyService.ofy;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author dras This class retrieves configuration data from the Datastore
 */
@Entity(name = "Config")
@Cache(expirationSeconds = 600)
public class DatastoreConfig {

  @Ignore
  private static final Logger log = Logger.getLogger(DatastoreConfig.class.getName());
  
  @Id
  String key;
  String value;

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public static String getValueForKey(String key) {
    DatastoreConfig dc = ofy().load().type(DatastoreConfig.class).id(key).now();
    if (dc == null) {
      log.log(Level.SEVERE, "Could not find config value for {0}, please add to the Datastore.", key);
      return null;
    }
    return dc.getValue();
  }
  public static String getValueForKey(String key, String defaultval) {
    DatastoreConfig dc = ofy().load().type(DatastoreConfig.class).id(key).now();
    if (dc == null) {
      log.log(Level.SEVERE, "Could not find config value for {0}, please add to the Datastore.", key);
      return null;
    }
    return dc.getValue();
  }
}
