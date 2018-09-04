/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.openhouseautomation.iftt;

import com.google.api.client.util.Strings;
import com.openhouseautomation.Convutils;
import static com.openhouseautomation.OfyService.ofy;
import com.openhouseautomation.model.Controller;
import com.openhouseautomation.model.DatastoreConfig;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.joda.time.DateTime;

/**
 *
 * @author dave
 */
public class Rgb extends DeferredController {

  public static final Logger log = Logger.getLogger(Rgb.class.getName());

  public Rgb() {
  }

  @Override
  public void run() {
    //make API call to set the color on all devices
    String access_token = DatastoreConfig.getValueForKey("particleapiaccesstoken", "");
    if (Strings.isNullOrEmpty(access_token)) {
      return;
    }
    try {
      String url = "https://api.particle.io/v1/devices/events";
      URL obj = new URL(url);
      HttpURLConnection con = (HttpURLConnection) obj.openConnection();
      con.setDoOutput(true);
      con.setInstanceFollowRedirects(false);
      //con.addRequestProperty("Content-Type", "application/json");
      con.addRequestProperty("Authorization", "Bearer " + access_token);
      con.setRequestMethod("POST");
      //con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

      log.log(Level.INFO, "Sending 'POST' request to URL : " + url);
      // Send post request
      DataOutputStream wr = new DataOutputStream(con.getOutputStream());
      wr.writeBytes("name=lightcolor&data=");
      wr.writeBytes(getController().getId().toString());
      wr.writeBytes("/");
      wr.writeBytes(getController().getDesiredState());
      wr.writeBytes("&private=true&ttl=60");
      wr.flush();
      wr.close();
      int responseCode = con.getResponseCode();
      log.log(Level.INFO, "Response Code : " + responseCode);

      BufferedReader in = new BufferedReader(
              new InputStreamReader(con.getInputStream()));
      String inputLine;
      StringBuffer response = new StringBuffer();

      while ((inputLine = in.readLine()) != null) {
        response.append(inputLine);
      }
      in.close();
      log.log(Level.INFO, "Response: " + response);
    } catch (Exception e) {
      log.log(Level.SEVERE, "ERR:" + e.getMessage(), e.getCause());
    }
  }
  
  public String lightLookup() {
    // load alarm status
    Controller alarmcontroller = ofy().load().type(Controller.class).id(3964578029L).now();
    if (alarmcontroller.getActualState().equals("Away")) {
      log.log(Level.INFO, "Alarm is set");
      return "#000000";
    }
    // load projector status
    Controller projectorcontroller = ofy().load().type(Controller.class).id(4157520376L).now();
    if (projectorcontroller.getActualState().equals("1")) {
      log.log(Level.INFO, "Movie lights");
      //return "#653d00";
    }
    // otherwise, return proper color
    DateTime now = Convutils.getNewDateTime();
    float hourmin = now.getHourOfDay();
    hourmin += now.getMinuteOfHour()/60.0;
    int r=0, g=0, b=0;
    if (hourmin < 6.45) return "#000000";
    if (hourmin < 7) {
      // map blue to bright
      r = g = maptoi(hourmin, 0, 50, 6.45, 7.0);
      b = maptoi(hourmin, 0, 255, 6.45, 7.0);
    }
    if (hourmin < 12) {
      // map to yellow
      r = g = maptoi(hourmin, 50, 204, 7.0, 12.0);
      b = maptoi(hourmin, 255, 0, 7.0, 12.0);
    }
    if (hourmin < 20) {
      // map to orange
      r=204;
      g=maptoi(hourmin, 204, 153, 12.0, 20.0);
      b=0;
    }
    if (hourmin < 22) {
      // map to purple
      r=maptoi(hourmin, 204, 102, 20.0, 22.0);
      g=maptoi(hourmin, 153, 0, 20.0, 22.0);
      b=maptoi(hourmin, 0, 255, 20.0, 22.0);
    }
    if (hourmin < 24) {
      // map to low red
      r=maptoi(hourmin, 102, 60, 22.0, 24.0);
      g=0;
      b=maptoi(hourmin, 255, 0,22.0, 24.0);
    }
    log.log(Level.INFO, "Response: " + rgbtoHex(r, g, b));
    return rgbtoHex(r, g, b);
  }
  
  private String intToHex(int i) {
    return Integer.toHexString(i);
  }
  private String rgbtoHex(int r, int g, int b) {
    return "#" + intToHex(r)+ intToHex(g) + intToHex(b);
  }
  private static int maptoi(final double unscaledNum, final double minOutput, final double maxOutput, final double minInput, final double maxInput) {
    return (int)((maxOutput - minOutput) * (unscaledNum - minInput) / (maxInput - minInput) + minOutput);
  }
}
