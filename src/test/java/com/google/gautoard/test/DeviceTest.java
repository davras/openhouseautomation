package com.openhouseautomation.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

public class DeviceTest {
  String url = "http://gthermostat.appspot.com/service";

  // String url = "http://localhost:8888/listen";
  public static void main(String[] argv) {
    try {
      new DeviceTest().go();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void go() throws ClientProtocolException, IOException {
    // add the params
    long timestamp = System.currentTimeMillis();
    HttpClient httpclient = new DefaultHttpClient();
    HttpPost httpPost = new HttpPost(url);
    List<NameValuePair> nvps = new ArrayList<NameValuePair>();
    nvps.add(new BasicNameValuePair("auth", "test"));
    nvps.add(new BasicNameValuePair("listenkey",
        "com.rasdesign.dras.home.thermostat.temperature.setpoint"));

    httpPost.setEntity(new UrlEncodedFormEntity(nvps));
    org.apache.http.HttpResponse response2 = httpclient.execute(httpPost);

    try {
      System.out.println(response2.getStatusLine());
      // HttpEntity entity2 = response2.getEntity();
      // do something useful with the response body
      // and ensure it is fully consumed
      // EntityUtils.consume(entity2);
    } finally {
      httpPost.releaseConnection();
    }
    System.out.println("That took: " + (System.currentTimeMillis() - timestamp) + "ms");
  }
}
