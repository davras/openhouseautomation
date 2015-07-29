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
import static com.openhouseautomation.OfyService.ofy;
import com.openhouseautomation.model.Controller;
import com.openhouseautomation.model.Sensor;
import org.joda.time.DateTime;
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
  public void testConsiderControlMode() {
    controller.setDesiredStatePriority(Controller.DesiredStatePriority.AUTO);
    ofy().save().entity(controller).now();
    hftester.setup();
    assertEquals(true, hftester.considerControlMode());

    // test the positives as well
    controller.setDesiredStatePriority(Controller.DesiredStatePriority.MANUAL);
    ofy().save().entity(controller).now();
    hftester.setup();
    assertEquals(false, hftester.considerControlMode());
    controller.setDesiredStatePriority(Controller.DesiredStatePriority.LOCAL);
    ofy().save().entity(controller).now();
    hftester.setup();
    assertEquals(false, hftester.considerControlMode());
    controller.setDesiredStatePriority(Controller.DesiredStatePriority.EMERGENCY);
    ofy().save().entity(controller).now();
    hftester.setup();
    assertEquals(false, hftester.considerControlMode());
  }

  @Test
  public void testConsiderTemperatures() {
    // should return false for ridiculous readings
    //test outside sensor
    outsidesensor.setLastReading("-200");
    ofy().save().entity(outsidesensor).now();
    insidesensor.setLastReading("75");
    ofy().save().entity(insidesensor).now();
    hftester.setup();
    assertFalse(hftester.considerTemperatures());
    outsidesensor.setLastReading("65");
    ofy().save().entity(outsidesensor).now();
    hftester.setup();
    assertTrue(hftester.considerTemperatures());
    
    // test inside sensor
    insidesensor.setLastReading("-200");
    ofy().save().entity(insidesensor).now();
    hftester.setup();
    assertFalse(hftester.considerTemperatures());
    insidesensor.setLastReading("85");
    ofy().save().entity(insidesensor).now();
    hftester.setup();
    assertTrue(hftester.considerTemperatures());
    
    // test outside > inside
    insidesensor.setLastReading("75");
    ofy().save().entity(insidesensor).now();
    hftester.setup();
    outsidesensor.setLastReading("85");
    ofy().save().entity(outsidesensor).now();
    hftester.setup();
    assertFalse(hftester.considerTemperatures());
    
    // test outside < inside
    insidesensor.setLastReading("85");
    ofy().save().entity(insidesensor).now();
    hftester.setup();
    outsidesensor.setLastReading("75");
    ofy().save().entity(outsidesensor).now();
    hftester.setup();
    assertTrue(hftester.considerTemperatures());

  }

  @Test
  public void testConsiderSlope() {
    // setup historical temperature readings
    DateTime now = new DateTime();
    DateTime nowlesshour = now.minusHours(1);
    DateTime nowlesstwohour = now.minusHours(2).plusMinutes(5);
    
  }
}
