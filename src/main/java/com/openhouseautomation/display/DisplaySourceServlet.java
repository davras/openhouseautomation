/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.openhouseautomation.display;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.appengine.api.datastore.QueryResultIterator;
import com.googlecode.objectify.cmd.Query;
import static com.openhouseautomation.OfyService.ofy;
import com.openhouseautomation.model.Controller;
import com.openhouseautomation.model.Sensor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author dave
 */
public class DisplaySourceServlet extends HttpServlet {

  private static final long serialVersionUID = 1L;
  private static final Logger log = Logger.getLogger(DisplaySourceServlet.class.getName());

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
    if (request.getPathInfo() == null) {
      return;
    }
    if (request.getPathInfo().startsWith("/display/sensors")) {
      doDisplaySensors(request, response);
      return;
    }
    if (request.getPathInfo().startsWith("/display/devices")) {
      doDisplayControllers(request, response);
      return;
    }
    if (request.getPathInfo().startsWith("/devicetypelist")) {
      doListDeviceTypes(request, response);
      return;
    }
    log.log(Level.WARNING, "unsupported path: " + request.getPathInfo());
    response.getWriter().println("path not supported");
  }

  private void doListDeviceTypes(HttpServletRequest request, HttpServletResponse response) throws IOException {
    PrintWriter out = response.getWriter();
    Query<Controller> q = ofy().load().type(Controller.class).project("type").distinct(true);
    QueryResultIterator<Controller> iterator = q.iterator();
    out.print("[");
    while (iterator.hasNext()) {
      Controller c = iterator.next();
      out.print("{\"name\":\"" + c.getType().toString() + "\"}");
      if (iterator.hasNext()) {
        out.print(",");
      }
    }
    out.print("]");
    return;
  }

  private void doDisplaySensors(HttpServletRequest request, HttpServletResponse response) throws IOException {
    PrintWriter out = response.getWriter();
    // dev on localhost
    if (request.getRemoteAddr().equals("127.0.0.1")) {
      response.getWriter().print(testSensorString);
      return;
    }
    // production
    Query<Sensor> query = ofy().load().type(Sensor.class);
    QueryResultIterator<Sensor> iterator = query.iterator();
    List sensors = new ArrayList();
    while (iterator.hasNext()) {
      Sensor sens = (Sensor) iterator.next();
      sensors.add(sens);
    }
    ObjectMapper om = new ObjectMapper();
    om.writeValue(out, sensors);
  }

  private void doDisplayControllers(HttpServletRequest request, HttpServletResponse response) throws IOException {
    PrintWriter out = response.getWriter();
    // dev on localhost
    if (request.getRemoteAddr().equals("127.0.0.1")) {
      response.getWriter().print(testControllerString);
      return;
    }
    // production
    String type = request.getParameter("type");
    Query<Controller> query = ofy().load().type(Controller.class).filter("type", type);
    QueryResultIterator<Controller> iterator = query.iterator();
    List controllers = new ArrayList();
    while (iterator.hasNext()) {
      Controller cont = (Controller) iterator.next();
      controllers.add(cont);
    }
    ObjectMapper om = new ObjectMapper();
    om.writeValue(out, controllers);
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
    log.log(Level.WARNING, "DisplaySourceServlet got a POST!");
    Enumeration<String> es = request.getParameterNames();
    while (es.hasMoreElements()) {
      String sp = es.nextElement();
      for (String spv : request.getParameterValues(sp)) {
        log.log(Level.WARNING, "doPost params:" + sp + "->" + spv);
      }
    }
    if (request.getPathInfo().startsWith("/controller/update")) {
      doControllerUpdate(request, response);
      return;
    }
  }
  
  public void doControllerUpdate(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String controllerid = request.getParameter("id");
    Controller controller = ofy().load().type(Controller.class).id(Long.parseLong(controllerid)).now();
    controller.setDesiredState(request.getParameter("desiredState"));
    ofy().save().entity(controller);
    response.sendError(HttpServletResponse.SC_NO_CONTENT);
  }

  String testSensorString = " [{\"expired\":false,\"id\":5744863563743232,\"owner\":\"dras\",\"location\":\"home\",\"zone\":\"outsideshadtemp\",\"type\":\"TEMPERATURE\",\"name\":\"Outside Temperature Shaded\",\"unit\":\"F\",\"lastreading\":\"70.25\"}]";
  String testControllerString = "[{\"id\":4280019022,\"owner\":\"dras\",\"location\":\"home\",\"zone\":\"atticwhf\",\"type\":\"WHOLEHOUSEFAN\",\"name\":\"Whole House Fan\",\"desiredStatePriority\":\"MANUAL\",\"validStates\":[\"0\",\"1\",\"2\",\"3\",\"4\",\"5\"],\"lastDesiredStateChange\":1414987381855,\"lastActualStateChange\":1414987381855,\"desiredState\":\"0\",\"actualState\":\"0\"}]";

}
