/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.openhouseautomation.devices;

import com.google.api.client.repackaged.com.google.common.base.Strings;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.common.base.Objects;
import com.google.common.io.BaseEncoding;
import java.io.IOException;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.googlecode.objectify.Key;
import com.openhouseautomation.Convutils;
import static com.openhouseautomation.OfyService.ofy;
import com.openhouseautomation.iftt.DeferredSensor;
import com.openhouseautomation.model.Controller;
import com.openhouseautomation.model.DatastoreConfig;
import com.openhouseautomation.model.EventLog;
import com.openhouseautomation.model.Message;
import com.openhouseautomation.model.Reading;
import com.openhouseautomation.model.Sensor;
import com.openhouseautomation.notification.NotificationHandler;
import java.io.BufferedReader;
import java.io.PrintWriter;
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
public class ControllerPushPubSubSubscription extends HttpServlet {

  private static final Logger log = Logger.getLogger(ControllerPushPubSubSubscription.class.getName());
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
    log.log(Level.INFO, "test log line");
    try (PrintWriter out = response.getWriter()) {
      out.println("test");
      response.setStatus(HttpServletResponse.SC_OK);
    } catch (Exception e) {
      log.log(Level.SEVERE, "ERR:" + e.toString());
      response.setStatus(HttpServletResponse.SC_OK);
    }
  }

  protected void processRequest2(HttpServletRequest request, HttpServletResponse response)
          throws ServletException, IOException {
    response.setContentType("text/html;charset=UTF-8");
    log.log(Level.INFO, "test log line");
    try (PrintWriter out = response.getWriter()) {
      /**
       * String pubsubVerificationToken =
       * System.getenv("PUBSUB_VERIFICATION_TOKEN"); // Do not process message
       * if request token does not match pubsubVerificationToken if
       * (request.getParameter("token").compareTo(pubsubVerificationToken) != 0)
       * { response.setStatus(HttpServletResponse.SC_BAD_REQUEST); return; }
       */

      // parse message object from "message" field in the request body json
      // decode message data from base64
      Message message = getMessage(request);
      message.parseData();
      long controllerid = message.getId();
      String controllerval = message.getValue();
      log.log(Level.INFO, "controllerid:" + controllerid + ",data:" + controllerval);

      // load the controller entity
      if (com.openhouseautomation.Flags.clearCache) {
        ofy().clear(); // clear the session cache, not the memcache
      }
      Controller controller = ofy().load().type(Controller.class).id(controllerid).now();
      if (controller == null || Strings.isNullOrEmpty(controllerval)) {
        log.log(Level.WARNING, "Missing controller or path");
        response.sendError(HttpServletResponse.SC_OK);
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

      if (!controller.getActualState().equals(controllerval)) {
        log.log(Level.INFO, "POST /device, LastActualState:{0} @{1}",
                new Object[]{controller.getActualState(), controller.getLastActualStateChange()});
        controller.setActualState(controllerval);
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
          etl.setNewState("MANUAL," + controllerval);
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
        if (controller.getValidStates().contains(controllerval)) {
          controller.setDesiredState(controllerval);
          log.log(Level.INFO, "desired state:{0} @{1}",
                  new Object[]{controller.getDesiredState(), controller.getLastDesiredStateChange().toLocalTime()});
        }
      }
      ofy().save().entity(controller).now();
      log.log(Level.INFO, "POST /device, saved controller setting:{0}", controller.toString());

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

  private Message getMessage(HttpServletRequest request) throws IOException {
    StringBuilder buffer = new StringBuilder();
    BufferedReader reader;
    String requestBody = "";
    reader = request.getReader();
    String line;
    while ((line = reader.readLine()) != null) {
      buffer.append(line);
    }
    requestBody = buffer.toString();
    JsonElement jsonRoot = jsonParser.parse(requestBody);
    String messageStr = jsonRoot.getAsJsonObject().get("message").toString();
    Message message = gson.fromJson(messageStr, Message.class);
    // decode from base64
    String decoded = decode(message.getData());
    message.setData(decoded);
    return message;
  }

  private String decode(String data) {
    return new String(BaseEncoding.base64().decode(data));
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
