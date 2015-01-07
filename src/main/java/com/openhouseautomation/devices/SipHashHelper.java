/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.openhouseautomation.devices;

import au.com.forward.sipHash.SipHash;
import static com.openhouseautomation.OfyService.ofy;
import com.openhouseautomation.model.DatastoreConfig;
import com.openhouseautomation.model.Sensor;

/**
 *
 * @author dras
 */
public class SipHashHelper {

  private String secret;
  public String error="";

  public SipHashHelper() { }
  
  public boolean validateHash(String id, String val, String auth) {
    if (id == null || "".equals(id) || val == null || "".equals(val)) {
      error = "null or blank id or value";
      return false;
    }
    if (auth == null || "".equals(auth)) {
      error = "null or blank auth";
      return false;
    }
    auth = auth.toUpperCase(); // make sure we are comparing upper case
    SipHash sipHash = new SipHash();
    String key = DatastoreConfig.getValueForKey("sensorsecret", "0123456789abcdef"); // don't use the default secret!
    String time = String.valueOf(System.currentTimeMillis() / 1000 / 60); // one minute window for hash
    //long digest = SipHash.digest(sk, new String(sensorid+value+time).getBytes());
    long result = sipHash.hash(key.getBytes(),(val + id).getBytes());
    String sleresult = SipHash.toHex(SipHash.longToBytesLE(result));
    String sberesult = SipHash.toHex(SipHash.longToBytes(result));
    error = "Submitted: " + auth + ", IntBE: " + sberesult + ", IntLE: " + sleresult;
    return sberesult.equals(auth) || sleresult.equals(auth);
  }
  public String getError() {
    return error;
  }
}
