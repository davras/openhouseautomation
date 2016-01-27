/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.openhouseautomation.tasks;

import com.googlecode.objectify.Key;
import static com.openhouseautomation.OfyService.ofy;
import com.openhouseautomation.logic.HouseFan;
import com.openhouseautomation.model.Controller;
import com.openhouseautomation.model.Sensor;
import java.io.IOException;
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
public class TaskHandler extends HttpServlet {

  public static final Logger log = Logger.getLogger(TaskHandler.class.getName());

  /**
   * Handles the HTTP <code>POST</code> method.
   *
   * @param request servlet request
   * @param response servlet response
   * @throws ServletException if a servlet-specific error occurs
   * @throws IOException if an I/O error occurs
   */
  // test with
  // 12dave.gautoard.appspot.com/tasks/newsensorreadinghandler?sensor=28131427 (Inside temp)
  // 12dave.gautoard.appspot.com/tasks/newsensorreadinghandler?sensor=2154791004 (Outside temp)
  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
          throws ServletException, IOException {
    if ("/newsensorvalue".equals(request.getPathInfo().trim())) {
      log.log(Level.INFO, "new sensor reading handler");
      doNewSensorReading(request, response);
      return;
    }
    if ("/newcontrollervalue".equals(request.getPathInfo().trim())) {
      log.log(Level.INFO, "new controller reading handler");
      doNewControllerReading(request, response);
    }
    response.setStatus(HttpServletResponse.SC_OK);
  }

  /**
   * Returns a short description of the servlet.
   *
   * @return a String containing servlet description
   */
  @Override
  public String getServletInfo() {
    return "Short description";
  }

  public void doNewControllerReading(HttpServletRequest request, HttpServletResponse response)
          throws IOException {
    // TODO make a helper class to extract request params
    final String controllerid = request.getParameter("id");
    if (null == controllerid || "".equals(controllerid)) {
      response.sendError(HttpServletResponse.SC_PRECONDITION_FAILED, "Missing value");
      return;
    }
    log.log(Level.INFO, "loading: {0}", controllerid);
    // TODO make helper class to load and save sensors
    Key<Controller> ck = Key.create(Controller.class, Long.parseLong(controllerid));
    Controller controller = ofy().load().now(ck);
    if (controller == null) {
      log.log(Level.INFO, "sensor not found:{0}", controllerid);
      return;
    }
  }

  public void doNewSensorReading(HttpServletRequest request, HttpServletResponse response)
          throws IOException {
    // TODO make a helper class to extract request params
    final String sensorid = request.getParameter("id");
    if (null == sensorid || "".equals(sensorid)) {
      response.sendError(HttpServletResponse.SC_PRECONDITION_FAILED, "Missing value");
      return;
    }
    log.log(Level.INFO, "loading: {0}", sensorid);
    // TODO make helper class to load and save sensors
    Key<Sensor> sk = Key.create(Sensor.class, Long.parseLong(sensorid));
    Sensor sensor = ofy().load().now(sk);
    if (sensor == null) {
      log.log(Level.INFO, "sensor not found:{0}", sensorid);
      return;
    }
    if (sensor.getName().equals("Outside Temperature") || sensor.getName().equals("Inside Temperature")) {
      log.log(Level.INFO, "processing: {0}", sensor.getName());
      new HouseFan().process();
    } else {
      log.log(Level.INFO, "ignoring: {0}", sensor.getName());
    }
  }
}
