/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.openhouseautomation.logic;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalMemcacheServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.googlecode.objectify.ObjectifyService;
import com.openhouseautomation.Convutils;
import static com.openhouseautomation.OfyService.ofy;
import com.openhouseautomation.model.Controller;
import com.openhouseautomation.model.Sensor;
import org.joda.time.DateTime;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

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
    ObjectifyService.register(Controller.class);
  }
  private final LocalServiceTestHelper helper = new LocalServiceTestHelper(
          new LocalDatastoreServiceTestConfig(),
          new LocalMemcacheServiceTestConfig());

  @Before
  public void setUp() {
    helper.setUp();
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

  @Test
  public void testConsiderTemperaturesBadReadings() {
    // should return false for ridiculous readings
    //test outside sensor
    hftester.setup();
    outsidesensor.setLastReading("-201");
    ofy().save().entity(outsidesensor).now();
    insidesensor.setLastReading("71");
    ofy().save().entity(insidesensor).now();
    assertFalse(hftester.considerTemperatures());
  }

  @Test
  public void testConsiderTemperatureGood() {

    hftester.setup();
    outsidesensor.setLastReading("62");
    ofy().save().entity(outsidesensor).now();
    insidesensor.setLastReading("72");
    ofy().save().entity(insidesensor).now();
    assertTrue(hftester.considerTemperatures());
  }

  @Test
  public void testBadInsideTemperature() {
    // test inside sensor
    hftester.setup();
    outsidesensor.setLastReading("63");
    ofy().save().entity(outsidesensor).now();
    insidesensor.setLastReading("-193");
    ofy().save().entity(insidesensor).now();
    assertFalse(hftester.considerTemperatures());
  }

  @Test
  public void testInsideGreaterThanOutside() {
    hftester.setup();
    outsidesensor.setLastReading("64");
    ofy().save().entity(outsidesensor).now();
    insidesensor.setLastReading("84");
    ofy().save().entity(insidesensor).now();
    assertTrue(hftester.considerTemperatures());
  }

  //@Test
  public void testHotterOutside() {
    // test outside > inside
    hftester.setup();
    insidesensor.setLastReading("75");
    ofy().save().entity(insidesensor).now();
    outsidesensor.setLastReading("85");
    ofy().save().entity(outsidesensor).now();
    assertTrue(hftester.considerTemperatures());
    assertTrue(hftester.hotterOutside());
  }

  @Test
  public void testOutsideLessThanInside() {
    // test outside < inside
    hftester.setup();
    insidesensor.setLastReading("85");
    ofy().save().entity(insidesensor).now();
    outsidesensor.setLastReading("75");
    ofy().save().entity(outsidesensor).now();
    assertTrue(hftester.considerTemperatures());
  }

  //@Test
  public void testConsiderSlope() {
    // setup historical temperature readings
    DateTime now = Convutils.getNewDateTime();
    DateTime nowlesshour = now.minusHours(1);
    DateTime nowlesstwohour = now.minusHours(2).plusMinutes(5);

  }
}
