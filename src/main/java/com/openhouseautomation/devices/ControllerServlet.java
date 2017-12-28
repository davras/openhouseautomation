/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.openhouseautomation.devices;

import com.google.common.base.Strings;
import com.openhouseautomation.Convutils;
import static com.openhouseautomation.OfyService.ofy;
import com.openhouseautomation.model.Controller;
import com.openhouseautomation.model.DatastoreConfig;
import com.openhouseautomation.model.EventLog;
import com.openhouseautomation.notification.NotificationHandler;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author dave
 */
public class ControllerServlet extends HttpServlet {

  private static final long serialVersionUID = 1L;
  private static final Logger log = Logger.getLogger(ControllerServlet.class.getName());

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
    // handles sensor reads
    response.setContentType("text/plain;charset=UTF-8");
    PrintWriter out = response.getWriter();
    String auth = request.getParameter("auth");
    final String controllerid = request.getParameter("k");
    final String reqpath = request.getPathInfo();
    if (!"test".equals(auth)) {
      // with hashkey
      response.sendError(HttpServletResponse.SC_FORBIDDEN, "Unauthorized, please use your auth key");
      return;
      // TODO: move auth to filter servlet
    }
    log.info("1. authorization checked");
    // load the controller entity
    if (com.openhouseautomation.Flags.clearCache) ofy().clear(); // clear the session cache, not the memcache
    Controller controller = ofy().load().type(Controller.class).id(Long.parseLong(controllerid)).now();
    if (controller == null || reqpath == null || "".equals(reqpath)) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing controller or path");
      return;
    }
    log.log(Level.INFO, "id={0},name={1}", new Object[]{controller.getId(), controller.getName()});
    if (reqpath.startsWith("/device")) {
      // get the devices's desired state
      out.println(controller.getId() + "=" + controller.getDesiredState() + ";"
              + controller.getLastDesiredStateChange().getMillis() / 1000);
      log.log(Level.INFO, "sent device desired:{0}={1};{2}", new Object[]{controller.getId().toString(),
        controller.getDesiredState(), Long.toString(controller.getLastDesiredStateChange().getMillis() / 1000)});
    } else if (reqpath.startsWith("/display")) {
      // show the actual state
      out.println(controller.toString());
      log.log(Level.INFO, "sent display:{0}", controller.toString());
    } else if (reqpath.startsWith("/initialize")) {
      initalizeValidStates(controller);
      ofy().save().entity(controller);
      out.println(controller);
    }
  }

  public void initalizeValidStates(Controller controller) {
    List vs = new ArrayList();
    // this should be some sort of ENUM, but it is controller-type specific
    // i.e. fan could have "on/off", or "off/low/high", or "0,1,2,3,4,5"
    if (controller.type == Controller.Type.WHOLEHOUSEFAN) {
      vs.add("0");
      vs.add("1");
      vs.add("2");
      vs.add("3");
      vs.add("4");
      vs.add("5");
    } else if (controller.type == Controller.Type.ALARM) {
      vs.add("DISARM");
      vs.add("HOME");
      vs.add("AWAY");
      vs.add("NOT READY");
    } else if (controller.type == Controller.Type.PROJECTOR) {
      vs.add("1");
      vs.add("0");
    }
    controller.setValidStates(vs);
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

    // lights handle 16 at a time vs one, split out early
    if (reqpath.startsWith("/lights")) {
      doLights(request, response);
      return;
    }
    // this point on, only one controller per device

    // extract values from request
    PrintWriter out = response.getWriter();
    String auth = request.getParameter("auth");
    String controllerid = request.getParameter("k");
    String controllervalue = request.getParameter("v");
    log.log(Level.INFO, "k={0},v={1}, auth={2}", new Object[]{controllerid, controllervalue, auth});

    // load the controller
    if (com.openhouseautomation.Flags.clearCache) ofy().clear(); // clear the session cache, not the memcache
    Controller controller = ofy().load().type(Controller.class).id(Long.parseLong(controllerid)).now();

    // check that everything looks good
    if (controller == null || Strings.isNullOrEmpty(reqpath)) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing controller or path");
      return;
    }

    // check the auth validation
    if (!checkValidation(controller, controllervalue, auth)) {
      log.log(Level.WARNING, "hash validation failed");
      response.sendError(HttpServletResponse.SC_FORBIDDEN, "Unauthorized, hash failed");
      return;
    }

    // notify if the controller is un-expiring
    if (controller.getLastContactDate().plusSeconds(controller.getExpirationtime()).isBeforeNow()) {
      // notify someone
      NotificationHandler nh = new NotificationHandler();
      nh.setSubject("Controller online: " + controller.getName());
      nh.setBody("Controller online: " + controller.getName());
      nh.page();
    }
    controller.setLastContactDate(Convutils.getNewDateTime());
    // handle device requests
    if (reqpath.startsWith("/device")) {
      handleDevice(controller, controllervalue, request, response);
    } else if (reqpath.startsWith("/fan")) {
      log.info("doPost Controller");
    } else {
      response.sendError(HttpServletResponse.SC_NOT_FOUND, "path not found");
    }
  }

  /**
   * A controller calls this path to update the actual state of a device If the
   * actual state changes with no desired change, someone overrode it locally
   * So, will go into manual mode. Window is 3 mins.
   *
   * @return a String containing servlet description
   */
  public void handleDevice(Controller controller, String controllervalue, HttpServletRequest request, HttpServletResponse response) {
    // TODO put this into an objectify transaction
    if (!controller.getActualState().equals(controllervalue)) {
      log.log(Level.INFO, "POST /device, LastActualState:{0} @{1}",
              new Object[]{controller.getActualState(), controller.getLastActualStateChange()});
      controller.setActualState(controllervalue);
      controller.setLastActualStateChange(Convutils.getNewDateTime());
      // if desiredstatelastchange is more than 3 mins old and
      // the desiredstate is not the actual state,
      // and the last desired state change is before the last actual change
      // then someone has locally overridden, must go to Manual
      if (controller.getLastDesiredStateChange().minusMinutes(3).isBeforeNow()
              && !controller.getDesiredState().equals(controller.getActualState())
              && !Controller.DesiredStatePriority.MANUAL.equals(controller.getDesiredStatePriority())) {
        log.log(Level.WARNING, "POST /device, lastdes is > 180 secs old, going into manual\n"
                + "controller.lastdesiredstatechange:{0}\ncurrent time:{3}\ndesired: {1}, actual: {2}",
                new Object[]{controller.getLastDesiredStateChange(),
                  controller.getDesiredState(),
                  controller.getActualState(),
                  Convutils.getNewDateTime()
                });
        log.log(Level.WARNING, "POST /device, cont.lastdesiredstatechange:{0}\n"
                + "current time:{1}\n"
                + "difference:{2}",
                new Object[]{controller.getLastDesiredStateChange().getMillis(),
                  Convutils.getNewDateTime().getMillis(),
                  (Convutils.getNewDateTime().getMillis() - controller.getLastDesiredStateChange().getMillis())
                });
        EventLog etl = new EventLog();
        etl.setIp(request.getRemoteAddr());
        etl.setNewState("MANUAL," + controllervalue);
        etl.setPreviousState(controller.getDesiredStatePriority().toString() + "," + controller.getDesiredState());
        etl.setType("Controller transition to manual");
        etl.setUser(request.getRemoteUser());
        ofy().save().entity(etl);
        controller.setDesiredStatePriority(Controller.DesiredStatePriority.MANUAL);
        // send notification of manual transition
        NotificationHandler nhnotif = new NotificationHandler();
        nhnotif.setBody(controller.getName() + " in MANUAL");
        nhnotif.setRecipient(DatastoreConfig.getValueForKey("admin", "bob@example.com"));
        nhnotif.setSubject("Controller AUTO->MANUAL");
        nhnotif.send();
      }
      if (controller.getValidStates().contains(controllervalue)) {
        controller.setDesiredState(controllervalue);
        log.log(Level.INFO, "POST /device, desired state:{0} @{1}",
                new Object[]{controller.getDesiredState(), controller.getLastDesiredStateChange()});
      }
    }
    ofy().save().entity(controller).now();
    log.log(Level.INFO, "POST /device, saved controller setting:{0}", controller.toString());
  }

  private boolean checkValidation(Controller c, String value, String authhash) {
    log.log(Level.INFO, "k={0},v={1}, auth={2}", new Object[]{c.getName(), value, authhash});
    SipHashHelper shh = new SipHashHelper();
    return shh.validateHash(c.getId().toString(), value, authhash);
  }

  private Controller makeNewLightExpirationController() {
    log.log(Level.WARNING, "Making a new light expiration controller");
    Controller cexpir = new Controller();
    cexpir.setOwner("SYSTEM");
    cexpir.setLocation("SYSTEM");
    cexpir.setZone("SYSTEM");
    cexpir.setType(Controller.Type.LIGHTS);
    cexpir.setDesiredState("0");
    cexpir.setDesiredStatePriority(Controller.DesiredStatePriority.AUTO);
    cexpir.setActualState("0");
    cexpir.setLastDesiredStateChange(Convutils.getNewDateTime());
    cexpir.setLastActualStateChange(Convutils.getNewDateTime());
    cexpir.setId(1234567890L);
    cexpir.setName("Lights");
    cexpir.setLastContactDate(Convutils.getNewDateTime());
    return cexpir;
  }

  /**
   * Returns a short description of the servlet.
   *
   * @return a String containing servlet description
   */
  @Override
  public String getServletInfo() {
    return "Handles sensor reads and updates";
  }

  private void checkLightControllerUnexpired() {
    Controller cexpir = ofy().load().type(Controller.class).id(1234567890L).now();
    if (cexpir == null) {
      cexpir = makeNewLightExpirationController();
    }
    if (cexpir.getLastContactDate().isBefore(Convutils.getNewDateTime().minusMinutes(10))) {
      // notify someone
      NotificationHandler nh = new NotificationHandler();
      nh.setSubject("Light Controller online");
      nh.setBody("Controller online: " + cexpir.getName());
      nh.page();
    }
    cexpir.setLastContactDate(Convutils.getNewDateTime());
    ofy().save().entity(cexpir).now();
  }
  /**
   * Handles an X10 light controller, 16 lights at a time
   *
   * @param request
   * @param response
   * @throws IOException
   */
  public void doLights(HttpServletRequest request, HttpServletResponse response) throws IOException {
    PrintWriter out = response.getWriter();
    final String actualstate = request.getParameter("v");
    if (Strings.isNullOrEmpty(actualstate)) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST, "passed value needs to have 16x[0,1]");
      return;
    }
    // check if unexpired
    checkLightControllerUnexpired();

    // first, set the desired state
    // if the actual setting is not the same as the desired setting,
    // then someone has locally overridden the setting.
    char[] toret = "xxxxxxxxxxxxxxxxx".toCharArray();
    if (com.openhouseautomation.Flags.clearCache) ofy().clear(); // clear the session cache, not the memcache
    List<Controller> lights = ofy().load().type(Controller.class).filter("type", "LIGHTS").list();
    for (Controller c : lights) {
      int lightnum = Integer.parseInt(c.getZone());
      String curstate = actualstate.substring(lightnum, lightnum + 1);
      // safely handle new lights by setting to off
      if (Strings.isNullOrEmpty(c.getDesiredState())) {
        c.setDesiredState("0");
      }

      if (!c.getActualState().equals(curstate)) {
        // a new update for the actual state of a light
        log.log(Level.INFO, "POST /lights, D:" + c.getActualState() + " @" + c.getLastActualStateChange());
        c.setActualState(curstate);
        c.setLastActualStateChange(Convutils.getNewDateTime());
      }
      // only set the values from network request that make sense
      if (c.getDesiredState().equals("1")) {
        toret[lightnum] = '1';
      }
      if (c.getDesiredState().equals("0")) {
        toret[lightnum] = '0';
      }
    }

    ofy().save().entities(lights);
    log.log(Level.INFO, "returning {0}", new String(toret));
    out.print(new String(toret));
    out.flush();
  }
}
