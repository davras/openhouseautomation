/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.openhouseautomation.iftt;

import com.google.api.client.util.Strings;
import com.openhouseautomation.model.DatastoreConfig;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author dave
 */
public class Rgb extends DeferredController {

  // TODO add automatic discovery
  static String[] DEV_IDS = {
    "3c0035000747353138383138",
    "380022000447343138333038"
  };
  public static final Logger log = Logger.getLogger(Rgb.class.getName());

  public Rgb() {
  }

  @Override
  public void run() {
    for (String dev_id : DEV_IDS) {
      tickleDevice(dev_id);
    }
  }

  private void tickleDevice(String device) {
    //make API call to set the color on all devices
    String access_token = DatastoreConfig.getValueForKey("particleapiaccesstoken", "");
    if (Strings.isNullOrEmpty(access_token)) {
      return;
    }
    try {
      String url = "https://api.particle.io/v1/devices/" + device + "/updateOHA";
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
      con.setDoOutput(true);
      DataOutputStream wr = new DataOutputStream(con.getOutputStream());
      wr.writeBytes("ping");
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
}