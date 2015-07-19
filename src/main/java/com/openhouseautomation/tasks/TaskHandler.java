/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.openhouseautomation.tasks;

import com.googlecode.objectify.Key;
import static com.openhouseautomation.OfyService.ofy;
import com.openhouseautomation.logic.HouseFan;
import com.openhouseautomation.logic.Utilities;
import com.openhouseautomation.model.Sensor;
import com.openhouseautomation.notification.MailNotification;
import java.io.IOException;
import java.io.PrintWriter;
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
    if ("/newsensorreadinghandler".equals(request.getPathInfo().trim())) {
      log.log(Level.INFO, "new sensor reading handler");
      doNewSensorReading(request, response);
      return;
    }
    PrintWriter out = response.getWriter();
    /* TODO output your page here. You may use following sample code. */
    out.println("<!DOCTYPE html>");
    out.println("<html>");
    out.println("<head>");
    out.println("<title>Servlet TestServlet</title>");
    out.println("</head>");
    out.println("<body>");
    out.println("<h1>Servlet TestServlet</h1>");
    out.println("<br>Request context path:" + request.getContextPath());
    out.println("<br>Request URI: " + request.getRequestURI());
    out.println("<br>Request path info: " + request.getPathInfo());
    out.println("<br>Request path info translated: " + request.getPathTranslated());
    out.println("</body>");
    out.println("</html>");
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

  public void doNewSensorReading(HttpServletRequest request, HttpServletResponse response)
          throws IOException {
    // TODO make a helper class to extract request params
    final String sensorid = request.getParameter("sensor");
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
