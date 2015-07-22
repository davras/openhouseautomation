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

  public TestHouseFan() {
    ObjectifyService.register(Controller.class);
  }
  private final LocalServiceTestHelper helper = new LocalServiceTestHelper(
          new LocalDatastoreServiceTestConfig(),
          new LocalMemcacheServiceTestConfig());

  @Before
  public void setUp() {
    helper.setUp();
  }

  @Test
  public void doTest() {
    HouseFan hftester = new HouseFan();
    Controller controller = new Controller();
    controller.setName("Whole House Fan");
    controller.setDesiredStatePriority(Controller.DesiredStatePriority.EMERGENCY);
    ofy().save().entity(controller);
    assertEquals(false, hftester.considerStatePriority());
    assertEquals(2,2);
    System.out.println("this was a test");
    
  }
}
