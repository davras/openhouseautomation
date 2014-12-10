package com.openhouseautomation;

import com.openhouseautomation.model.DatastoreConfig;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A set of conversion utilities for convenience
 *
 * @author dras
 */
public class Convutils {

  static long tzoffset = 0L;

  /**
   * Converts seconds to a human-eyeball friendly format
   *
   * @param secs
   * @return A human readable String
   */
  public static String timeAgoToString(long secs) {
    long thistime = System.currentTimeMillis() / 1000;
    long eventtime = secs;
    long timeago = thistime - eventtime;
    if (timeago < 0) {
      return "unknown: " + eventtime;
    }
    if (timeago < 60) { // up to one minute
      return timeago + " seconds ago";
    }
    timeago /= 60; // convert to minutes
    if (timeago < 60) { // up to one hour
      return timeago + " minutes ago";
    }
    timeago /= 60; // convert to hours
    if (timeago < 24) { // up to one day
      return timeago + " hours ago";
    }
    timeago /= 24; // convert to days
    if (timeago < 30) { // up to one month
      return timeago + " days ago";
    }
    timeago /= 30;
    return timeago + " months ago";
  }

  /**
   * Returns the current Date as a String
   *
   * @return String
   */
  public static String currentDate() {
    return new Date().toString();
  }

  /**
   * Turns seconds-since-epoch into Date String
   *
   * @param secs since epoch
   * @return Date as String corresponding to secs parameter since epoch
   */
  public static String timeToString(long secs) {
    if (tzoffset == 0) {
      tzoffset = Long.parseLong(DatastoreConfig.getValueForKey("tzoffset"));
    }
    return new Date((secs + (tzoffset * 60)) * 1000L).toString();
  }

  public static Date convertStringDate(String s) {
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
    Date d;
    try {
      d = dateFormat.parse(s);
    } catch (ParseException pe) {
      return new Date(0L);
    }
    return d;
  }
}
