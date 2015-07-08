/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.openhouseautomation.logic;

import com.openhouseautomation.notification.MailNotification;

/**
 *
 * @author dras
 */
public class NewReadingProcessor {
  public void process() {
    compareTemperaturesforCooling();
  }
  public void compareTemperaturesforCooling() {
    // if outside temperature is less than inside temperature
    if (Utilities.getReading("Outside Temperature")-1 < Utilities.getReading("Inside Temperature")) {
      // and if slope is negative (outside temperature going down for the last hour)
      if (Utilities.getSlope("Outside Temperature", 60*60) < 0) {
        MailNotification mnotif = new MailNotification();
        mnotif.setBody(
                "Outside Temperature is lower than Inside Temperature\n\n" +
                "Outside Temperature: " + Utilities.getReading("Outside Temperature") + "\n" +
                "Inside Temperature: " + Utilities.getReading("Inside Temperature") + "\n" +
                "Outside Temperature Slope: " + Utilities.getSlope("Outside Temperature", 60*60));
        mnotif.setRecipient("davras@gmail.com");
        mnotif.setSubject("A/C off, House Fan on");
        mnotif.sendNotification();
      }
    }
  }
}
