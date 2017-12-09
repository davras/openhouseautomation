/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.openhouseautomation.devices;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.common.base.Objects;
import java.io.IOException;
import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.googlecode.objectify.Key;
import com.openhouseautomation.Convutils;
import static com.openhouseautomation.OfyService.ofy;
import com.openhouseautomation.iftt.DeferredSensor;
import com.openhouseautomation.model.Message;
import com.openhouseautomation.model.Reading;
import com.openhouseautomation.model.Sensor;
import com.openhouseautomation.notification.NotificationHandler;
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
public class SensorPushPubSubSubscription extends HttpServlet {

  private static final Logger log = Logger.getLogger(SensorPushPubSubSubscription.class.getName());
  private final Gson gson = new Gson();
  private final JsonParser jsonParser = new JsonParser();

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
    response.setContentType("text/html;charset=UTF-8");
    try {
      /**
      String pubsubVerificationToken = System.getenv("PUBSUB_VERIFICATION_TOKEN");
      // Do not process message if request token does not match pubsubVerificationToken
      if (request.getParameter("token").compareTo(pubsubVerificationToken) != 0) {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        return;
      }
      */
      
      // parse message object from "message" field in the request body json
      // decode message data from base64
      Message message = PubSubUtils.getMessage(request);
      if (!message.parseData()) {
        log.log(Level.WARNING, "bad data:" + message.getData());
        response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        return;
      }
      // save sensor reading
      long sensorid = message.getId();
      String sensorval = message.getValue();
      log.log(Level.INFO, "sensorid:" + sensorid + ",data:" + sensorval);
      Key<Sensor> sk = Key.create(Sensor.class, sensorid);
      Sensor sensor = ofy().load().now(sk);
      if (sensor == null) {
        log.log(Level.INFO, "sensor not found:{0}", sensorid);
        response.sendError(HttpServletResponse.SC_OK, "Sensor not found");
        return;
      }
      // if the sensor was expired and is now updating
      if (sensor.getLastReadingDate().plusSeconds(sensor.getExpirationTime()).isBeforeNow()
              && sensor.getExpirationTime() > 0) {
        // notify someone
        NotificationHandler nh = new NotificationHandler();
        nh.setSubject("Sensor online");
        nh.setBody("Sensor online: " + sensor.getName());
        nh.page();
      }

      if (sensor.getType() == Sensor.Type.TEMPERATURE
              && Float.parseFloat(sensorval) < -195.0) {
        log.log(Level.SEVERE, "Bad temperature reading: " + sensorval);
        response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        return;
      }
      // set the value
      sensor.setLastReadingDate(Convutils.getNewDateTime());
      sensor.setLastReading(sensorval);
      doPostProcessing(sensor);
      ofy().save().entity(sensor); // async
      log.log(Level.INFO, "saved sensor:{0}", sensor);
      Reading reading = new Reading();
      reading.setSensor(sk);
      reading.setTimestamp(Convutils.getNewDateTime());
      reading.setValue(sensorval);
      ofy().save().entity(reading); // async
      log.log(Level.INFO, "logged reading:{0}", reading);
      // 200, 201, 204, 102 status codes are interpreted as success by the Pub/Sub system
      response.setStatus(HttpServletResponse.SC_NO_CONTENT);
    } catch (Exception e) {
      log.log(Level.SEVERE, "ERR:" + e.toString());
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }

  public void doPostProcessing(Sensor sensor) {
    if (!sensor.needsPostprocessing()) {
      return;
    }
    if (Objects.equal(sensor.getPreviousReading(), sensor.getLastReading())) {
      return;
    }
    // Add the task to the default queue.
    Queue queue = QueueFactory.getDefaultQueue();
    DeferredSensor dfc = null;
    String classtoget = "com.openhouseautomation.iftt." + Convutils.toTitleCase(sensor.getType().name());
    log.log(Level.INFO, "creating class: {0}", classtoget);
    try {
      dfc = (DeferredSensor) Class.forName(classtoget).newInstance();
      // i.e.: com.openhouseautomation.iftt.Alarm class
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
      // obviously, don't enqueue the task
      log.log(Level.SEVERE, "I could not create the class needed: {0}"
              + "\n" + "Please make sure the class exists and is accessible before enabling postprocessing on controller id: {1}",
              new Object[]{classtoget, sensor.getId()}
      );
    }
    if (dfc != null) {
      // grab the sensor and add the task
      dfc.setSensor(sensor);
      queue.addAsync(TaskOptions.Builder.withPayload(dfc)); // async
    }
  }


  // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
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
    processRequest(request, response);
  }

  /**
   * Returns a short description of the servlet.
   *
   * @return a String containing servlet description
   */
  @Override
  public String getServletInfo() {
    return "Short description";
  }// </editor-fold>

}
