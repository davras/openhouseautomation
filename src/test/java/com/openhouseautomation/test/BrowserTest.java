package com.openhouseautomation.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;



public class BrowserTest {
  String url = "http://gthermostat.appspot.com/service";

  // String url = "http://localhost:8888/service";

  public static void main(String[] argv) {
    try {
      new BrowserTest().go();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void go() throws ClientProtocolException, IOException {
    long snaptime = System.currentTimeMillis();
    browsersettemperature();
    System.out.println("browser set setpoint took " + (System.currentTimeMillis() - snaptime)
        + " ms");
    try {
      Thread.sleep(2000);
    } catch (InterruptedException e) {
    }
    snaptime = System.currentTimeMillis();
    browsergettemperature();
    System.out.println("browser get setpoint took " + (System.currentTimeMillis() - snaptime)
        + " ms");
  }

  public void browsersettemperature() throws ClientProtocolException, IOException {
    // add the params
    DefaultHttpClient httpclient = new DefaultHttpClient();
    HttpPost httpPost = new HttpPost(url);
    List<BasicNameValuePair> nvps = new ArrayList<BasicNameValuePair>();
    nvps.add(new BasicNameValuePair("auth", "test"));
    nvps.add(new BasicNameValuePair("com.rasdesign.dras.home.thermostat.setpoint.set", new Double(
        Math.round((Math.random() * 30) + 60)).toString()));

    httpPost.setEntity(new UrlEncodedFormEntity(nvps));
    HttpResponse response2 = httpclient.execute(httpPost);

    try {
      System.out.println("set response=" + response2.getStatusLine());
      HttpEntity entity2 = response2.getEntity();
      // do something useful with the response body
      // and ensure it is fully consumed
      EntityUtils.consume(entity2);
    } finally {
      httpPost.releaseConnection();
    }
  }

  public void browsergettemperature() throws ClientProtocolException, IOException {
    DefaultHttpClient httpclient = new DefaultHttpClient();
    HttpPost httpPost = new HttpPost(url);
    List<NameValuePair> nvps = new ArrayList<NameValuePair>();
    nvps.add(new BasicNameValuePair("auth", "test"));
    nvps.add(new BasicNameValuePair("com.rasdesign.dras.home.thermostat.setpoint.get", ""));

    httpPost.setEntity(new UrlEncodedFormEntity(nvps));
    HttpResponse response2 = httpclient.execute(httpPost);

    try {
      System.out.println("get response=" + response2.getStatusLine());
      HttpEntity entity2 = response2.getEntity();
      System.out.println("body=" + EntityUtils.toString(entity2));
      // do something useful with the response body
      // and ensure it is fully consumed
      EntityUtils.consume(entity2);
    } finally {
      httpPost.releaseConnection();
    }
  }
}
