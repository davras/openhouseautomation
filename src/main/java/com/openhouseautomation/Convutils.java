package com.openhouseautomation;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Instant;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import com.openhouseautomation.model.DatastoreConfig;

/**
 * A set of conversion utilities for convenience
 *
 * @author dras
 */
public class Convutils {

  static String timezone="UTC";

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
    return Instant.now().toString();
  }

  /**
   * Turns seconds-since-epoch into a Joda DateTime String
   *
   * @param secs since epoch
   * @return Date as String corresponding to secs parameter since epoch
   */
  public static String timeToString(long secs) {
    if ("UTC".equals(timezone)) {
      timezone = DatastoreConfig.getValueForKey("timezone", "America/Los_Angeles");
    }
    DateTimeZone zone = DateTimeZone.forID(timezone);
    DateTime dt = new DateTime(secs*1000L,zone);
    return dt.toString();
  }

  public static DateTime convertStringDate(String s) {
    DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyyMMdd"); 
    DateTime d = fmt.parseDateTime(s);
    return d;
  }

  public static String toTitleCase(String givenString) {
    if (null == givenString || "".equals(givenString)) {
      return null;
    }
    String[] arr = givenString.toLowerCase().split(" ");
    StringBuilder sb = new StringBuilder();
    for (String arr1 : arr) {
      sb.append(Character.toUpperCase(arr1.charAt(0))).append(arr1.substring(1)).append(" ");
    }
    return sb.toString().trim();
  }
}
