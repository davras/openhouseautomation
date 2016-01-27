/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.openhouseautomation.devices;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.RetryOptions;
import static com.google.appengine.api.taskqueue.RetryOptions.Builder.*;
import com.google.appengine.api.taskqueue.TaskOptions;
import org.joda.time.DateTime;
import com.openhouseautomation.model.Sensor;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import static com.openhouseautomation.OfyService.ofy;
import com.openhouseautomation.model.Reading;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Work;

/**
 *
 * @author dras
 */
public class SensorServlet extends HttpServlet {

  private static final long serialVersionUID = 1L;
  private static final Logger log = Logger.getLogger(SensorServlet.class.getName());

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
    try (PrintWriter out = response.getWriter()) {
      String auth = request.getParameter("auth");
      if (!"test".equals(auth)) {
        // with hashkey
        response.sendError(HttpServletResponse.SC_FORBIDDEN, "Unauthorized, please use your auth key");
        return;
        // TODO: move auth to filter servlet
      }
      log.log(Level.INFO, "1. authorization checked");
      String sensorid = request.getParameter("k");
      log.log(Level.INFO, "k={0}", sensorid);
      // load the sensor entity
      Sensor sensor = ofy().load().type(Sensor.class).id(Long.parseLong(sensorid)).now();
      if (sensor != null) {
        out.println(sensor.getId() + "=" + sensor.getLastReading() + ";" + sensor.getLastReadingDate().getMillis() / 1000);
        log.log(Level.INFO, "sent:{0}={1};{2}", new Object[]{sensor.getId(), sensor.getLastReading(), sensor.getLastReadingDate().getMillis() / 1000});
      } else {
        response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Sensor not found");
      }
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
    PrintWriter out = response.getWriter();
    String auth = request.getParameter("auth");
    final String sensorid = request.getParameter("k");
    final String sensorval = request.getParameter("v");
    if (null == sensorid || "".equals(sensorid) || null == sensorval || "".equals(sensorval)) {
      response.sendError(HttpServletResponse.SC_PRECONDITION_FAILED, "Missing value");
      return;
    }
    log.log(Level.INFO, "k={0},v={1}", new Object[]{sensorid, sensorval});
    SipHashHelper shh = new SipHashHelper();
    if (!shh.validateHash(sensorid, sensorval, auth)) {
      log.log(Level.WARNING, "hash validation failed:{0}", shh.getError());
      response.sendError(HttpServletResponse.SC_FORBIDDEN, "Unauthorized, hash failed");
      return;
    } else {
      log.log(Level.INFO, "Hash validated");
    }
    ofy().transact(new Work<Sensor>() {
      @Override
      public Sensor run() {
        Key<Sensor> sk = Key.create(Sensor.class, Long.parseLong(sensorid));
        Sensor sensor = ofy().load().now(sk);
        if (sensor == null) {
          log.log(Level.INFO, "sensor not found:{0}", sensorid);
          return null;
        }
        // set the value
        sensor.setLastReadingDate(new DateTime());
        sensor.setLastReading(sensorval);
        ofy().save().entity(sensor);
        log.log(Level.INFO, "saved sensor:{0}", sensor);
        Reading reading = new Reading();
        reading.setSensor(sk);
        reading.setTimestamp(new DateTime());
        reading.setValue(sensorval);
        ofy().save().entity(reading);
        log.log(Level.INFO, "logged reading:{0}", reading);
        return sensor;
      }
    });
    out.println("OK");

    // TODO: move auth to filter servlet
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

}
