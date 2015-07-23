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
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author dras
 */
public class TestHouseFan {
  Controller controller;
  public TestHouseFan() {
    ObjectifyService.register(Controller.class);
  }
  private final LocalServiceTestHelper helper = new LocalServiceTestHelper(
          new LocalDatastoreServiceTestConfig(),
          new LocalMemcacheServiceTestConfig());

  @Before
  public void setUp() {
    controller = new Controller();
    controller.setName("Whole House Fan");
  }

  //@Test
  public void testConsiderStatePriority() {
    HouseFan hftester = new HouseFan();
    Controller controller = new Controller();
    controller.setName("Whole House Fan");
    controller.setDesiredStatePriority(Controller.DesiredStatePriority.EMERGENCY);
    ofy().save().entity(controller);
    assertEquals(false, hftester.considerStatePriority());
  }
  
  //@Test
  public void testConsiderControlMode() {
    HouseFan hftester = new HouseFan();
    Controller controller = new Controller();
    controller.setName("Whole House Fan");
    controller.setDesiredStatePriority(Controller.DesiredStatePriority.AUTO);
    ofy().save().entity(controller);
    assertEquals(true, hftester.considerControlMode());
    
    // test the positives as well
    controller.setDesiredStatePriority(Controller.DesiredStatePriority.MANUAL);
    ofy().save().entity(controller);
    assertEquals(false, hftester.considerControlMode());
    controller.setDesiredStatePriority(Controller.DesiredStatePriority.LOCAL);
    ofy().save().entity(controller);
    assertEquals(false, hftester.considerControlMode());
    controller.setDesiredStatePriority(Controller.DesiredStatePriority.EMERGENCY);
    ofy().save().entity(controller);
    assertEquals(false, hftester.considerControlMode());
  }
}
