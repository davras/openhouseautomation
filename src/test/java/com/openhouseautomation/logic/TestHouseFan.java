package com.openhouseautomation.logic;

import org.joda.time.DateTime;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalMemcacheServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.impl.translate.opt.joda.JodaTimeTranslators;
import com.openhouseautomation.Convutils;
import static com.openhouseautomation.OfyService.ofy;
import com.openhouseautomation.model.Controller;
import com.openhouseautomation.model.Sensor;
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
    JodaTimeTranslators.add(ObjectifyService.factory());
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
    controller.setLastContactDate(Convutils.getNewDateTime());
    ofy().save().entity(controller).now();
    assertTrue(hftester.setup());
  }

  @Test
  public void testConsiderStatePriority() {
    controller.setDesiredStatePriority(Controller.DesiredStatePriority.EMERGENCY);
    controller.setLastContactDate(Convutils.getNewDateTime());
    ofy().save().entity(controller).now();
    assertTrue(hftester.setup());
    boolean result = hftester.considerStatePriority();
    assertFalse(result);
  }

  @Test
  public void testConsiderControlModeAuto() {
    controller.setDesiredStatePriority(Controller.DesiredStatePriority.AUTO);
    controller.setLastContactDate(Convutils.getNewDateTime());
    ofy().save().entity(controller).now();
    assertTrue(hftester.setup());
    assertTrue(hftester.considerControlMode());
  }

  @Test
  public void testConsiderControlModeManual() {
    // test the positives as well
    controller.setDesiredStatePriority(Controller.DesiredStatePriority.MANUAL);
    controller.setLastContactDate(Convutils.getNewDateTime());
    ofy().save().entity(controller).now();
    assertTrue(hftester.setup());
    assertFalse(hftester.considerControlMode());
  }

  @Test
  public void testConsiderControlModeLocal() {
    controller.setDesiredStatePriority(Controller.DesiredStatePriority.LOCAL);
    controller.setLastContactDate(Convutils.getNewDateTime());
    ofy().save().entity(controller).now();
    assertTrue(hftester.setup());
    assertFalse(hftester.considerControlMode());
  }

  @Test
  public void testConsiderControlModeEmergency() {
    controller.setDesiredStatePriority(Controller.DesiredStatePriority.EMERGENCY);
    controller.setLastContactDate(Convutils.getNewDateTime());
    ofy().save().entity(controller).now();
    assertTrue(hftester.setup());
    assertFalse(hftester.considerControlMode());
  }

  @Test
  public void testConsiderTemperatureGood() {
    outsidesensor.setLastReading("80");
    insidesensor.setLastReading("70");
    ofy().save().entities(insidesensor, outsidesensor).now();
    // should return false for ridiculous readings
    //test outside sensor
    outsidesensor.setLastReading("-201");
    ofy().save().entity(outsidesensor).now();
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
