package com.openhouseautomation.model;

import com.google.api.client.util.Strings;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Ignore;
import static com.openhouseautomation.OfyService.ofy;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author dras This class retrieves configuration data from the Datastore
 */
@Entity(name = "Config")
@Cache(expirationSeconds = 7200)
public class DatastoreConfig {

  @Ignore
  private static final Logger log = Logger.getLogger(DatastoreConfig.class.getName());

  @Ignore
  private static LoadingCache<String, String> cachez = CacheBuilder.newBuilder()
       .maximumSize(1000)
       .expireAfterWrite(10, TimeUnit.MINUTES)
       .build(
           new CacheLoader<String, String>() {
             @Override
             public String load(String key) {
               DatastoreConfig dc = ofy().load().type(DatastoreConfig.class).id(key).now();
               return dc.getValue();
             }
           });
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

  public static String getValueForKey(String key, String defaultstr) {
    // check internal cache first, which will cause a load
    String retval = cachez.getUnchecked(key);
    // if empty, set the default and return default
    if (Strings.isNullOrEmpty(retval)) {
      DatastoreConfig dc = ofy().load().type(DatastoreConfig.class).id(key).now();
      if (dc == null && null != defaultstr && !"".equals(defaultstr)) {
        log.log(Level.WARNING, "Could not find config value for {0}, adding placeholder with {1}.  Modify the value in the Datastore", new Object[]{key, defaultstr});
        dc = new DatastoreConfig();
        dc.setKey(key);
        dc.setValue(defaultstr);
        ofy().save().entity(dc).now();
        return defaultstr;
      }
    }
    // otherwise, return cache lookup value
    return retval;
  }
}
