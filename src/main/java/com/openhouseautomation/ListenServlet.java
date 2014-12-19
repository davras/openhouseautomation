package com.openhouseautomation;

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
   * Doesn't work: regular Objectify load()
   * Why: Reads from session cache don't reflect other thread's modifications (doesn't normally read Memcache)
   * 
   * Doesn't work: Objectify load with cache(false)
   * Why: Bypasses memcache and reads from Datastore
   * 
   * Doesn't work: Transactions in Objectify
   * Why: Bypasses memcache and reads from Datastore
   * Docs state: Starting a transaction creates a new Objectify instance with a fresh, empty session cache
   * Which implies that memcache is used, but later docs state:
   * (Transactional) Reads and writes bypass the memcache
   * 
   * Works!!!: Use Objectify.clear() between each read to clear the session cache, but still read from Memcache
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
      while (ApiProxy.getCurrentEnvironment().getRemainingMillis() > timeout && !out.checkError() && !foundachange) {
        // do we have new info to hand back?
        // walk the ArrayList, load each Controller, compare values against original
        ofy().clear(); // clear the session cache, not the memcache
        log.log(Level.INFO, "cleared cache");
        for (Controller controllercompareinitial : cinitial) {
          Controller controllernew = ofy().load().type(Controller.class).id(controllercompareinitial.getId()).now();
          String newval = controllernew.getDesiredState();
          if (!controllercompareinitial.getDesiredState().equals(newval)) {
            foundachange = true;
            changedcontroller = controllernew;
          }
        }
        if (foundachange) {
          response.setStatus(HttpServletResponse.SC_OK);
          out.write(changedcontroller.getId() + "=" + changedcontroller.getDesiredState() + ";" + changedcontroller.getLastDesiredStateChange().getTime() / 1000);
          out.flush();
          out.close();
          return;
        }
        if (out.checkError()) {
          return;
        }
        // TODO(dras): how to detect if client disconnected?
        try {
          log.log(Level.INFO, "zzz...");
          Thread.sleep(pollinterval);
        } catch (InterruptedException e) {
        }
      }
      // if you get to this point (timeout), the value didn't change
      response.setStatus(HttpServletResponse.SC_NO_CONTENT);
      // returns 204, no content, which tells the client to
      // immediately reconnect
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
    log.log(Level.FINE, "request path:" + reqpath);

    if (reqpath.startsWith("/lights")) {
      doLightsListen(request, response);
      return;
    }
  }

  private void doLightsListen(HttpServletRequest request, HttpServletResponse response) throws IOException {
    timeout = Long.parseLong(DatastoreConfig.getValueForKey("listentimeoutms", "8000"));
    pollinterval = Long.parseLong(DatastoreConfig.getValueForKey("listenpollintervalms", "2500"));
    PrintWriter out = response.getWriter();
    final String actualstate = request.getParameter("v");
    if (null == actualstate || "".equals(actualstate)) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST, "passed value needs to have 16x[0,1]");
      return;
    }
    // first, set the desired state
    // if the actual setting is not the same as the desired setting,
    // then someone has locally overridden the setting.
    char[] toret = "xxxxxxxxxxxxxxxxx".toCharArray();
    List<Controller> lights = ofy().load().type(Controller.class).filter("type", "LIGHTS").list();
    boolean dirty = false;
    for (Controller c : lights) {
      int lightnum = Integer.parseInt(c.getZone());
      String curstate = actualstate.substring(lightnum, lightnum + 1);
      // handle brand new controllers
      if (c.getDesiredState() == null || c.getDesiredState().equals("")) {
        c.setDesiredState(curstate);
      }
      if (!c.getActualState().equals(curstate)) {
        log.log(Level.INFO, "POST /lights, D:" + c.getActualState() + " @" + c.getLastActualStateChange());
        c.setActualState(curstate);
        // if desiredstatelastchange is more than 60 secs old, this is a local override.
        if (c.getLastDesiredStateChange().getTime() < (System.currentTimeMillis() - 60000)) {
          log.log(Level.INFO, "POST /lights, lastdes is > 60 secs old, going into manual");
          c.setDesiredState(curstate);
          c.setDesiredStatePriority(Controller.DesiredStatePriority.MANUAL);
        }
        dirty = true;
      }
      if (c.getDesiredState().equals("1")) {
        toret[lightnum] = '1';
      }
      if (c.getDesiredState().equals("0")) {
        toret[lightnum] = '0';
      }
    }
    if (dirty) {
      ofy().save().entities(lights);
      log.log(Level.INFO, "returning " + new String(toret));
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
        Controller controllernew = ofy().load().type(Controller.class).id(controllercompareinitial.getId()).now();
        String newval = controllernew.getDesiredState();
        if (!controllercompareinitial.getDesiredState().equals(newval)) {
          foundachange = true;
          int index = -1;
          index = Integer.parseInt(controllernew.getZone());
          String desiredstate = controllernew.getDesiredState();
          toret[index] = newval.charAt(0);
        }
      }
      if (foundachange) {
        response.setStatus(HttpServletResponse.SC_OK);
        out.print(new String(toret));
        out.flush();
        out.close();
        return;
      }
      if (out.checkError()) {
        return;
      }

      // TODO(dras): how to detect if client disconnected?
      //log.log(Level.INFO, "{0} seconds left in request timer", ApiProxy.getCurrentEnvironment().getRemainingMillis());
      try {
        Thread.sleep(pollinterval);
      } catch (InterruptedException e) {
      }
    }
    // if you get to this point (timeout), the value didn't change, 
    // but send back desired anyway (less code for the arduino)
    out.print(new String(toret));
    out.flush();
    out.close();
    return;
  }
}
