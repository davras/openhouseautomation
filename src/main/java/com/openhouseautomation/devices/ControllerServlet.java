/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.openhouseautomation.devices;

import static com.openhouseautomation.OfyService.ofy;
import com.openhouseautomation.model.Controller;
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
    log.log(Level.INFO, "k={0}", controllerid);
    // load the controller entity

    Controller controller = ofy().load().type(Controller.class).id(Long.parseLong(controllerid)).now();
    if (controller == null || reqpath == null || "".equals(reqpath)) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing controller or path");
      return;
    }
    if (reqpath.startsWith("/device")) {
      // get the devices's desired state
      out.println(controller.getId() + "=" + controller.getDesiredState() + ";" + controller.getLastDesiredStateChange().getTime() / 1000);
      log.log(Level.INFO, "sent device desired:{0}={1};{2}", new Object[]{controller.getId(), controller.getDesiredState(), controller.getLastDesiredStateChange().getTime() / 1000});
    } else if (reqpath.startsWith("/display")) {
      // show the actual state
      out.println(controller.toString());
      log.log(Level.INFO, "sent display:{0}", controller.toString());
    } else if (reqpath.startsWith("/initialize")) {
      List vs = new ArrayList();
      vs.add("0");
      vs.add("1");
      vs.add("2");
      vs.add("3");
      vs.add("4");
      vs.add("5");
      controller.setValidStates(vs);
      ofy().save().entity(controller);
      out.println(controller);
    }
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
      doLights(request, response);
      return;
    }
    PrintWriter out = response.getWriter();
    String auth = request.getParameter("auth");
    final String controllerid = request.getParameter("k");
    final String controllervalue = request.getParameter("v");
    Controller controller = ofy().load().type(Controller.class).id(Long.parseLong(controllerid)).now();
    // TODO cleanup the anonymous inner class
    log.log(Level.INFO, "k={0},v={1}", new Object[]{controllerid, controllervalue});
    log.log(Level.INFO, "1. checking siphash auth: {0}", auth);
    if (!SipHashHelper.validateHash(controllerid, controllervalue, auth)) {
      response.sendError(HttpServletResponse.SC_FORBIDDEN, "Unauthorized, please use your auth key");
      return;
    }
    if (controller == null || reqpath == null || "".equals(reqpath)) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing controller or path");
      return;
    }

    if (reqpath.startsWith("/device")) {
      // always sets actual state
      // if the actual setting is not the same as the desired setting,
      // then someone has locally overridden the setting.
      if (controller.getDesiredState() == null || controller.getDesiredState().equals("")) {
        controller.setDesiredState(controllervalue);
      }
      if (!controller.getActualState().equals(controllervalue)) {
        log.log(Level.INFO, "POST /device, D:" + controller.getActualState() + " @" + controller.getLastActualStateChange());
        controller.setActualState(controllervalue);
        // if desiredstatelastchange is more than 60 secs old, this is a local override.
        if (controller.getLastDesiredStateChange().getTime() < (System.currentTimeMillis() - 60000)) {
          log.log(Level.INFO, "POST /device, lastdes is > 60 secs old, going into manual");
          controller.setDesiredState(controllervalue);
          controller.setDesiredStatePriority(Controller.DesiredStatePriority.MANUAL);
          out.println("MANUAL:" + controller.getDesiredState());
        }
      }
      ofy().save().entity(controller);
      log.log(Level.INFO, "POST /device, saved controller setting:{0}", controller.toString());
      out.println("OK");
      return;

    } else if (reqpath.startsWith("/display")) {
      log.log(Level.INFO, "POST /display:" + controllervalue);
      // display is setting a value
      if (controllervalue.equals("AUTO")) {
        controller.setDesiredStatePriority(Controller.DesiredStatePriority.AUTO);
        ofy().save().entity(controller);
        log.log(Level.INFO, "POST /display, saved controller setting:{0}", controller.toString());
        out.println("AUTO");
        out.println("OK");
        return;
      } else {
        // it's a manual setting
        log.log(Level.INFO, "POST /display, manual setting:{0}", controllervalue);
        controller.setDesiredState(controllervalue);
        controller.setDesiredStatePriority(Controller.DesiredStatePriority.MANUAL);
        ofy().save().entity(controller);
        log.log(Level.INFO, "POST /display, saved controller setting:{0}", controller.toString());
        out.println("MANUAL: " + controller.getDesiredState());
        out.println("OK");
      }

    } else if (reqpath.startsWith("/fan")) {
      log.info("doPost Controller");
    } else {
      response.sendError(HttpServletResponse.SC_NOT_FOUND, "path not found");
    }
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

  private void doLights(HttpServletRequest request, HttpServletResponse response) throws IOException {
    PrintWriter out = response.getWriter();
    final String controllervalue = request.getParameter("v");
    // first, set the desired state
    // if the actual setting is not the same as the desired setting,
    // then someone has locally overridden the setting.
    char[] toret = "0000000000000000".toCharArray();
    List<Controller> lights = ofy().load().type(Controller.class).filter("type", "LIGHTS").list();
    for (Controller c : lights) {
      int lightnum = Integer.parseInt(c.getZone());
      String curstate = controllervalue.substring(lightnum + 1, lightnum + 2);
      if (c.getDesiredState() == null || c.getDesiredState().equals("")) {
        c.setDesiredState(curstate);
      }
      if (!c.getActualState().equals(curstate)) {
        log.log(Level.INFO, "POST /lights, D:" + c.getActualState() + " @" + c.getLastActualStateChange());
        c.setActualState(curstate);
        // if desiredstatelastchange is more than 60 secs old, this is a local override.
        if (c.getLastDesiredStateChange().getTime() < (System.currentTimeMillis() - 60000)) {
          log.log(Level.INFO, "POST /device, lastdes is > 60 secs old, going into manual");
          c.setDesiredState(curstate);
          c.setDesiredStatePriority(Controller.DesiredStatePriority.MANUAL);
        }
      }
      if (c.getDesiredState().equals("1")) {
        toret[lightnum] = '1';
    }
    ofy().save().entities(lights);
    out.print(new String(toret));
    }
  }
}
