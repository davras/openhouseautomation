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
    controller.setDesiredStatePriority(Controller.DesiredStatePriority.AUTO);
    ofy().save().entity(controller).now();
    assertTrue(hftester.setup());
    assertTrue(hftester.considerControlMode());
  }

  @Test
  public void testConsiderControlModeManual() {
    // test the positives as well
    controller.setDesiredStatePriority(Controller.DesiredStatePriority.MANUAL);
    ofy().save().entity(controller).now();
    assertTrue(hftester.setup());
    assertFalse(hftester.considerControlMode());
  }

  @Test
  public void testConsiderControlModeLocal() {
    controller.setDesiredStatePriority(Controller.DesiredStatePriority.LOCAL);
    ofy().save().entity(controller).now();
    assertTrue(hftester.setup());
    assertFalse(hftester.considerControlMode());
  }

  @Test
  public void testConsiderControlModeEmergency() {
    controller.setDesiredStatePriority(Controller.DesiredStatePriority.EMERGENCY);
    ofy().save().entity(controller).now();
    assertTrue(hftester.setup());
    assertFalse(hftester.considerControlMode());
  }

  @Test
  public void testConsiderTemperatureGood() {
    outsidesensor.setLastReading("80");
    insidesensor.setLastReading("70");
    ofy().save().entities(insidesensor, outsidesensor).now();
    assertTrue(hftester.considerTemperatures());
    assertTrue(hftester.hotterOutside());
    // should return false for ridiculous readings
    //test outside sensor
    outsidesensor.setLastReading("-201");
    ofy().save().entity(outsidesensor).now();
    hftester.setup();
    assertFalse(hftester.considerTemperatures());
    
    // test inside sensor
    outsidesensor.setLastReading("63");
    insidesensor.setLastReading("-193");
    ofy().save().entities(insidesensor, outsidesensor).now();
    assertFalse(hftester.considerTemperatures());
  }

  //@Test
  public void testConsiderSlope() {
    // setup historical temperature readings
    DateTime now = Convutils.getNewDateTime();
    DateTime nowlesshour = now.minusHours(1);
    DateTime nowlesstwohour = now.minusHours(2).plusMinutes(5);

  }
}
