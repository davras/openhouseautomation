/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.openhouseautomation;

/**
 *
 * @author dras
 */
public class GregorianCalendarCopy {
    public static com.ibm.icu.util.GregorianCalendar convert(java.util.GregorianCalendar fromcal) {
      com.ibm.icu.util.GregorianCalendar tocal = new com.ibm.icu.util.GregorianCalendar();
      tocal.setTimeZone(com.ibm.icu.util.TimeZone.getTimeZone("GMT"));
      tocal.setTimeInMillis(fromcal.getTimeInMillis());
      return tocal;
    }
}
