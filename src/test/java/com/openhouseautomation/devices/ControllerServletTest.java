/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.openhouseautomation.devices;

import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalMemcacheServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.googlecode.objectify.ObjectifyService;
import com.openhouseautomation.Convutils;
//import com.googlecode.objectify.impl.translate.opt.joda.JodaTimeTranslators;
import static com.openhouseautomation.OfyService.ofy;
import com.openhouseautomation.model.Controller;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import static org.junit.Assert.*;
import org.junit.*;

/**
 *
 * @author dave
 */
public class ControllerServletTest {

  Controller c;
  ControllerServlet cs;

  public ControllerServletTest() {
    //JodaTimeTranslators.add(ObjectifyService.factory());
    ObjectifyService.register(Controller.class);
  }
  private final LocalServiceTestHelper helper = new LocalServiceTestHelper(
          new LocalDatastoreServiceTestConfig(),
          new LocalMemcacheServiceTestConfig());

  @Before
  public void setUp() {
    helper.setUp();
    c = new Controller();
    cs = new ControllerServlet();
    c.setName("Whole House Fan");
    c.setId(new Long(1));
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  //@Test
  public void testJodaDateTime() {
    ofy().save().entity(c).now();
    c.setLastDesiredStateChange(Convutils.getNewDateTime());
    // fails here with com.googlecode.objectify.SaveException: Error saving com.openhouseautomation.model.Controller{
    //   id=1, owner=null, location=null, zone=null, type=null, name=Whole House Fan, desiredstate=0, actualstate=null,
    //   desiredstatepriority=null, lastdesiredstatechange=2015-08-25T19:57:44.126-07:00, lastactualstatechange=null, 
    //   validstates=null}: Class 'class org.joda.time.chrono.ISOChronology' is not a registered @Subclass
    ofy().save().entity(c).now();
  }

  /**
   * Test of doGet method, of class ControllerServlet. If actual state is going
   * to desired state within 3 minutes, stay in AUTO
   */
  //@Test
  public void testHandleDeviceAuto() throws Exception {
    System.out.println("doHandleDeviceAuto()");
    c.setActualState("1");
    c.setDesiredState("0");
    //DateTime dt = new DateTime(DateTimeZone.forID("PST8PDT"));
    LocalDateTime ldt = new LocalDateTime();
    DateTime dt = ldt.toDateTime();
    //DateTime dtm = dt.minusMinutes(1);
    //DateTime dtm2 = dt.withZone(DateTimeZone.forID("PST8PDT"));
    c.setLastDesiredStateChange(dt);
    c.setLastActualStateChange(dt);
    ofy().save().entity(c).now();
    cs.handleDevice(c, "0", null, null);
    //assertEquals(result, "0");
    assertEquals(c.getDesiredStatePriority(), Controller.DesiredStatePriority.AUTO);
  }

  /**
   * Test of doPost method, of class ControllerServlet.
   */
  //@Test
  public void testDoPost() throws Exception {
    System.out.println("doPost");
    HttpServletRequest request = null;
    HttpServletResponse response = null;
    ControllerServlet instance = new ControllerServlet();
    instance.doPost(request, response);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }


}
