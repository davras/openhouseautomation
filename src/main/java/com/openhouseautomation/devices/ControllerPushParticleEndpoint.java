/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.openhouseautomation.devices;

import com.google.common.base.Strings;
import java.io.IOException;
import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.openhouseautomation.Convutils;
import static com.openhouseautomation.OfyService.ofy;
import com.openhouseautomation.model.Controller;
import com.openhouseautomation.notification.NotificationHandler;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
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
public class ControllerPushParticleEndpoint extends HttpServlet {

  private static final Logger log = Logger.getLogger(ControllerPushParticleEndpoint.class.getName());
  private final Gson gson = new Gson();
  private final JsonParser jsonParser = new JsonParser();

  /**
   * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
   *
   * @param request servlet request
   * @param response servlet response
   * @throws ServletException if a servlet-specific error occurs
   * @throws IOException if an I/O error occurs
   */
  protected void processRequest(HttpServletRequest request, HttpServletResponse response)
          throws ServletException, IOException {
    response.setContentType("text/plain;charset=UTF-8");
    try (PrintWriter out = response.getWriter()) {
      String message = request.getParameter("data");
      StringTokenizer st1 = new StringTokenizer(message, "/");
      if (st1.countTokens() != 2) {
        log.log(Level.WARNING, "!2 tokens found in: " + message);
        response.sendError(HttpServletResponse.SC_OK);
        return;
      }
      long controllerid = Long.parseLong((String)st1.nextToken());
      String controllerval = (String)st1.nextToken();
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

        if (controller.getValidStates() == null) {
          if (controller.type == Controller.Type.RGB) {
            List vs = new ArrayList();
            vs.add("#000000");
            vs.add("#ffffff");
            controller.setValidStates(vs);
          }
        }
        if (controller.getValidStates().contains(controllerval)) {
          controller.setDesiredState(controllerval);
          log.log(Level.INFO, "desired state:{0} @{1}",
                  new Object[]{controller.getDesiredState(), controller.getLastDesiredStateChange().toLocalTime()});
        }
        if (controller.getType() == Controller.Type.RGB) {
          controller.setDesiredState(controllerval);
        }
      }
      // also triggers the postprocessing onSave()
      ofy().save().entity(controller).now();
      log.log(Level.INFO, "POST /device, saved controller setting:{0}", controller.toString());
      out.println(controller.getDesiredState());
      response.setStatus(HttpServletResponse.SC_OK);
    } catch (Exception e) {
      log.log(Level.SEVERE, "ERR:" + e, e.fillInStackTrace());
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
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
