/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.openhouseautomation.logic;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalMemcacheServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.openhouseautomation.Convutils;
import static com.openhouseautomation.OfyService.ofy;
import com.openhouseautomation.model.Controller;
import com.openhouseautomation.model.Sensor;
import org.joda.time.DateTime;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.impl.translate.opt.joda.JodaTimeTranslators;
import com.googlecode.objectify.util.Closeable;

/**
 *
 * @author dras
 */
public class TestHouseFan {

  Controller controller;
  HouseFan hftester;
  Sensor outsidesensor;
  Sensor insidesensor;

  public TestHouseFan() {
    JodaTimeTranslators.add(ObjectifyService.factory());
    ObjectifyService.register(Controller.class);
    ObjectifyService.register(Sensor.class);
  }
  private final LocalServiceTestHelper helper = new LocalServiceTestHelper(
          new LocalDatastoreServiceTestConfig(),
          new LocalMemcacheServiceTestConfig());
  private Closeable closeable;

  @Before
  public void setUp() {
    helper.setUp();
    closeable = ObjectifyService.begin();
    hftester = new HouseFan();
    controller = new Controller();
    controller.setName("Whole House Fan");
    outsidesensor = new Sensor();
    outsidesensor.setName("Outside Temperature");
    insidesensor = new Sensor();
    insidesensor.setName("Inside Temperature");
  }

  @After
  public void tearDown() {
    closeable.close();
    helper.tearDown();
  }

  @Test
  public void testSetup() {
    controller.setDesiredStatePriority(Controller.DesiredStatePriority.EMERGENCY);
    ofy().save().entity(controller).now();
    assertTrue(hftester.setup());
  }

  @Test
  public void testConsiderStatePriority() {
    controller.setDesiredStatePriority(Controller.DesiredStatePriority.EMERGENCY);
    ofy().save().entity(controller).now();
    assertTrue(hftester.setup());
    boolean result = hftester.considerStatePriority();
    assertFalse(result);
  }

  @Test
  public void testConsiderControlModeAuto() {
    // master slave datastore mock is holding state between runs
    // breaking the tests
    hftester.setup();
    controller.setDesiredStatePriority(Controller.DesiredStatePriority.AUTO);
    ofy().save().entity(controller).now();
    assertTrue(hftester.considerControlMode());
  }

  @Test
  public void testConsiderControlModeManual() {
    // test the positives as well
    hftester.setup();
    controller.setDesiredStatePriority(Controller.DesiredStatePriority.MANUAL);
    ofy().save().entity(controller).now();
    assertFalse(hftester.considerControlMode());
  }

  @Test
  public void testConsiderControlModeLocal() {
    hftester.setup();
    controller.setDesiredStatePriority(Controller.DesiredStatePriority.LOCAL);
    ofy().save().entity(controller).now();
    assertFalse(hftester.considerControlMode());
  }

  @Test
  public void testConsiderControlModeEmergency() {
    hftester.setup();
    controller.setDesiredStatePriority(Controller.DesiredStatePriority.EMERGENCY);
    ofy().save().entity(controller).now();
    assertFalse(hftester.considerControlMode());
  }

  // TODO rewrite hf test for extra methods
  @Test
  public void testConsiderFreshnessTrue() {
    outsidesensor.setLastReading("42");
    ofy().save().entity(outsidesensor).now();
    insidesensor.setLastReading("72");
    ofy().save().entity(insidesensor).now();

    // both are fresh readings, should return true
    System.err.println("considerFreshness=" + hftester.considerFreshness());
    assertTrue(hftester.considerFreshness());
  }

  @Test
  public void testConsiderFreshnessFalseInside() {
    outsidesensor.setLastReading("42");
    outsidesensor.setLastReadingDate(new DateTime().minusMinutes(30));
    outsidesensor.setExpirationTime(3600);
    outsidesensor.updateExpired();
    ofy().save().entity(outsidesensor).now();
    insidesensor.setLastReading("72");
    insidesensor.setLastReadingDate(new DateTime().minusMinutes(30));
    insidesensor.setExpirationTime(1);
    insidesensor.updateExpired();
    System.err.println("insidesensor:" + insidesensor);
    ofy().save().entity(insidesensor).now();

    // should return false for inside stale reading
    try {
      Thread.sleep(2000);
    } catch (InterruptedException e) {
    }
    System.err.println("considerFreshness=" + hftester.considerFreshness());
    assertFalse(hftester.considerFreshness());
  }

  @Test
  public void testConsiderFreshnessFalseOutside() {
    outsidesensor.setLastReading("42");
    // new sensors are given 15 minutes before expiring
    outsidesensor.setLastReadingDate(new DateTime().minusMinutes(30));
    outsidesensor.setExpirationTime(1);
    outsidesensor.updateExpired();
    System.err.println("outsidesensor:" + outsidesensor);
    ofy().save().entity(outsidesensor).now();
    insidesensor.setLastReading("72");
    insidesensor.setLastReadingDate(new DateTime().minusMinutes(30));
    insidesensor.setExpirationTime(3600);
    outsidesensor.updateExpired();
    ofy().save().entity(insidesensor).now();

    // should return false for outsidesensor stale reading
    try {
      Thread.sleep(2000);
    } catch (InterruptedException e) {
    }
    assertFalse(hftester.considerFreshness());
  }

  //@Test
  public void testConsiderSlope() {
    // setup historical temperature readings
    DateTime now = Convutils.getNewDateTime();
    DateTime nowlesshour = now.minusHours(1);
    DateTime nowlesstwohour = now.minusHours(2).plusMinutes(5);

  }
}
