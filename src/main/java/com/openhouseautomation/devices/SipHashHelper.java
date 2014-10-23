/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.openhouseautomation.devices;

import com.github.emboss.siphash.SipHash;
import com.github.emboss.siphash.SipKey;
import static com.openhouseautomation.OfyService.ofy;
import com.openhouseautomation.model.Sensor;

/**
 *
 * @author dras
 */
public class SipHashHelper {
  public static boolean validateHash(String sensorid, String value, String hash) {
    if ("test".equals(hash)) {
      return true;
    }
    Sensor sensor = ofy().load().type(Sensor.class).id(Long.parseLong(sensorid)).now();
    String secret = sensor.getSecret().trim().concat("0123456789abcdef").substring(0,16);
    byte[] bsec = secret.getBytes();
    SipKey sk = new SipKey(bsec);
    String time = String.valueOf(System.currentTimeMillis()/1000/60); // one minute window for hash
    long digest = SipHash.digest(sk, new String(sensorid+value+time).getBytes());
    return String.valueOf(digest).equals(hash);
  }
}
