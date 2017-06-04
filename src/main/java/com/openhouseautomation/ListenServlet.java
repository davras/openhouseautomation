package com.openhouseautomation;

import com.google.api.client.repackaged.com.google.common.base.Strings;
import com.google.appengine.api.LifecycleManager;
import com.google.apphosting.api.ApiProxy;
import static com.openhouseautomation.OfyService.ofy;
import com.openhouseautomation.model.Controller;
import com.openhouseautomation.model.DatastoreConfig;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.http.HttpStatus;
import org.joda.time.DateTime;

/**
 *
 * @author dras
 */
public class ListenServlet extends HttpServlet {
  // this servlet handles incoming requests to wait for a value change.
  // when the controller changes desiredState, this servlet detects the modification and sends
  // notification back to the client.  Connection is held open until timeout or data change.
  // pseudo Half-BOSH

  /**
   * architecture: Goal: read most recent Controller status from Memcache
   * Verified that an Objectify.save() will update Memcache, now how to read
   * from Memcache for the latest value?.
   *
   * Doesn't work: regular Objectify load() Why: Reads from session cache don't
   * reflect other thread's modifications (doesn't normally read Memcache)
   *
   * Doesn't work: Objectify load with cache(false) Why: Bypasses memcache and
   * reads from Datastore
   *
   * Doesn't work: Transactions in Objectify Why: Bypasses memcache and reads
   * from Datastore Docs state: Starting a transaction creates a new Objectify
   * instance with a fresh, empty session cache Which implies that memcache is
   * used, but later docs state: (Transactional) Reads and writes bypass the
   * memcache
   *
   * Works!!!: Use Objectify.clear() between each read to clear the session
   * cache, but still read from Memcache
   */
  private static final long serialVersionUID = 1L;
  private static final Logger log = Logger.getLogger(ListenServlet.class.getName());
  long timeout = 8000L; // stop looping when this many ms are left in the request timer
  long pollinterval = 1000L; // poll for changes once per second

  /**
   * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
   * methods.
   *
   * @param request servlet request
   * @param response servlet response
   * @throws ServletException if a servlet-specific error occurs
   * @throws IOException if an I/O error occurs
   */
  protected void processRequest(HttpServletRequest request, HttpServletResponse response)
          throws ServletException, IOException {
    try (PrintWriter out = response.getWriter()) {
      response.setContentType("text/plain");

      final ArrayList<Controller> cinitial = this.arrangeRequest(request);
      log.log(Level.INFO, "got {0} keys to listen for", cinitial.size());
      boolean foundachange = false;
      timeout = Long.parseLong(DatastoreConfig.getValueForKey("listentimeoutms", "8000"));
      pollinterval = Long.parseLong(DatastoreConfig.getValueForKey("listenpollintervalms", "2500"));
      Controller changedcontroller = null;
      while (ApiProxy.getCurrentEnvironment().getRemainingMillis() > timeout
              && !out.checkError()
              && !foundachange
              && !LifecycleManager.getInstance().isShuttingDown()) {
        // do we have new info to hand back?
        // walk the ArrayList, load each Controller, compare values against original
        ofy().clear(); // clear the session cache, not the memcache
        //log.log(Level.INFO, "cleared cache");
        for (Controller controllercompareinitial : cinitial) {
          Controller controllernew = ofy().load().type(Controller.class).id(controllercompareinitial.getId()).now();
          // this block should handle memcache flushes
          if (controllernew == null) {
            // prevent errors from causing frequent retry requests
            try {
              Thread.sleep(5000);
            } catch (InterruptedException e) {
            }
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            return;
          }
          String newval = controllernew.getDesiredState();
          if (!controllercompareinitial.getDesiredState().equals(newval)) {
            foundachange = true;
            changedcontroller = controllernew;
          }
        }
        if (foundachange) {
          response.setStatus(HttpServletResponse.SC_OK);
          out.write(changedcontroller.getId() + "=" + changedcontroller.getDesiredState() + ";" + changedcontroller.getLastDesiredStateChange().getMillis() / 1000);
          out.flush();
          out.close();
          return;
        }
        try {
          log.log(Level.INFO, "zzz...");
          Thread.sleep(pollinterval);
        } catch (InterruptedException e) {
        }
      }
      // if you get to this point (timeout), the value didn't change
      response.setStatus(HttpServletResponse.SC_NO_CONTENT);
      // returns 204, no content, which tells the client to immediately reconnect
    }
  }

  /**
   * Transforms the HTTP Request into a list of Controller objects loaded from
   * Datastore
   *
   * @param req The servlet request with controller ids as parameters
   * @return An ArrayList of Controllers parsed from the HTTP Request
   * @throws IOException
   */
  public ArrayList<Controller> arrangeRequest(HttpServletRequest req) throws IOException {
    log.log(Level.INFO, "Starting arrangeRequest");
    ArrayList<Controller> ebs = new ArrayList<>();
    ofy().clear(); // clear the session cache, not the memcache
    for (Enumeration<String> paramNames = req.getParameterNames(); paramNames.hasMoreElements();) {
      String controllerid = paramNames.nextElement();
      log.log(Level.INFO, "got an id:{0}", controllerid);
      if ("auth".equals(controllerid)) {
        continue; // the auth isn't a controller
      }
      Controller controller = ofy().load().type(Controller.class).id(Long.parseLong(controllerid)).now();
      ebs.add(controller);
      log.log(Level.INFO, "arrange listener:{0}", controller);
    }
    return ebs;
  }

  /**
   * Handles the HTTP <code>GET</code> method.
   *
   * @param request servlet request
   * @param response servlet response
   * @throws ServletException if a servlet-specific error occurs
   * @throws IOException if an I/O error occurs
   */
  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
          throws ServletException, IOException {
    processRequest(request, response);
  }

  /**
   * Handles the HTTP <code>POST</code> method.
   *
   * @param request servlet request
   * @param response servlet response
   * @throws ServletException if a servlet-specific error occurs
   * @throws IOException if an I/O error occurs
   */
  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
          throws ServletException, IOException {
// handles sensor updates
    response.setContentType("text/plain;charset=UTF-8");
    final String reqpath = request.getPathInfo();
    log.log(Level.INFO, "request path:" + reqpath);

    if (reqpath.startsWith("/lights")) {
      doLightsListen(request, response);    }
  }

  private boolean validateInputData(List<Controller> lights, String actualstate) {
    // validate the device presents a sane state
    // i.e. xxxxxxxxxxxxxxxxx would be expected (boot-up), as well as
    //      0x00x1xxxxxxxxxxx if there were 4 lights configured
    // but  1101001xx1x0x0x10 means controllercount != devicecount
    // so mark it dirty
    int controllercount=lights.size();
    int devicecount=getDeviceCount(actualstate);
    if (controllercount == devicecount) {
      return true;
    }
    return false;
  }
  
  private int getDeviceCount(String actualstate) {
    int devicecount=0;
    for (int i=0; i < actualstate.length();i++) {
      if (actualstate.charAt(i)!='x') {
        devicecount++;
      }
    }
    return devicecount;
  }
  
  private void doLightsListen(HttpServletRequest request, HttpServletResponse response) throws IOException {
    timeout = Long.parseLong(DatastoreConfig.getValueForKey("listentimeoutms", "8000"));
    pollinterval = Long.parseLong(DatastoreConfig.getValueForKey("listenpollintervalms", "2500"));
    PrintWriter out = response.getWriter();
    final String actualstate = request.getParameter("v");
    if (Strings.isNullOrEmpty(actualstate)) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST, "passed value needs to have 16x[0,1]");
      return;
    }
    // first, set the desired state
    // if the actual setting is not the same as the desired setting,
    // then return desired setting (x10 is one-way for now)
    // TODO listen for x10 signals and report them from microcontroller
    char[] toret = "xxxxxxxxxxxxxxxxx".toCharArray();
    ofy().clear(); // clear the session cache, not the memcache
    List<Controller> lights = ofy().load().type(Controller.class).filter("type", "LIGHTS").list();
    boolean validinputdata = validateInputData(lights,actualstate);
    boolean dirty = false;
    for (Controller c : lights) {
      int lightnum = Integer.parseInt(c.getZone());
      String curstate = actualstate.substring(lightnum, lightnum + 1);
      // handle brand new controllers
      if (Strings.isNullOrEmpty(c.getDesiredState())) {
        c.setDesiredState(curstate);
      }
      if (!curstate.equals(c.getActualState())) {
        // the light controller will send 17 'x' characters when it boots up because it doesn't know the state
        // check for valid input data, as a crashing device can send garbage
        // in that case, send back known good data and ignore the device data
        if (!curstate.equals("x") && validinputdata) {
          log.log(Level.INFO, "updating actual and desired state, D:{0} @{1}", new Object[]{c.getActualState(), c.getLastActualStateChange()});
          c.setActualState(curstate);
          c.setLastActualStateChange(Convutils.getNewDateTime());
          c.setDesiredState(curstate);
        }
        dirty = true;
      }
      if (c.getDesiredState().equals("1")) {
        toret[lightnum] = '1';
      }
      if (c.getDesiredState().equals("0")) {
        toret[lightnum] = '0';
      }
      if (c.getLastContactDate().plusMinutes(20).isBeforeNow()) {
        // update only 3x per hour to save DS writes
        // expiration alert after 1 hour
        log.log(Level.INFO, "updating last contact date");
        c.setLastContactDate(Convutils.getNewDateTime());
        dirty = true;
      }
    }
    if (dirty) {
      ofy().save().entities(lights).now();
      log.log(Level.INFO, "fast returning " + new String(toret));
      out.print(new String(toret));
      out.flush();
      return;
    }
    // now loop, waiting for a value to change
    List<Controller> cinitial = lights;
    boolean foundachange = false;
    while (ApiProxy.getCurrentEnvironment().getRemainingMillis() > timeout && !out.checkError() && !foundachange) {
      // do we have new info to hand back?
      // walk the ArrayList, load each Controller, compare values against original
      ofy().clear(); // clear the session cache
      for (Controller controllercompareinitial : cinitial) {
        Controller controllernew = null;
        try {
          controllernew = ofy().load().type(Controller.class).id(controllercompareinitial.getId()).now();
        } catch (Exception e) {
          // This will catch Memcache flushes and return so the client can
          // reconnect and listen again.
          out.print(new String(toret));
          out.flush();
          out.close();
          return;
        }
        String newval = controllernew.getDesiredState();
        if (!controllercompareinitial.getDesiredState().equals(newval)) {
          foundachange = true;
          int index = -1;
          index = Integer.parseInt(controllernew.getZone());
          toret[index] = newval.charAt(0);
        }
      }
      if (foundachange) {
        response.setStatus(HttpServletResponse.SC_OK);
        log.log(Level.INFO, "delayed returning " + new String(toret));
        out.print(new String(toret));
        out.flush();
        out.close();
        return;
      }
      if (out.checkError()) {
        return;
      }

      try {
        Thread.sleep(pollinterval);
      } catch (InterruptedException e) {
      }
    }
    // if you get to this point (timeout), the value didn't change, 
    // but send back desired anyway (less code for the arduino)
    out.print(new String(toret));
    log.log(Level.INFO, "timeout returning " + new String(toret));
    out.flush();
    out.close();
    return;
  }
}
