/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.openhouseautomation.cron;

import static com.openhouseautomation.OfyService.ofy;
import com.openhouseautomation.model.Controller;
import com.openhouseautomation.model.NotificationLog;
import com.openhouseautomation.model.Sensor;
import com.openhouseautomation.notification.NotificationHandler;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author dave
 */
public class ExpirationCheck extends HttpServlet {

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

    // do all the sensors
    ofy().clear(); // clear the session cache, not the memcache
    List<Sensor> sensors = ofy().load().type(Sensor.class).list();
    for (Sensor s : sensors) {
      if (s.isExpired()) {
        // it's expired, notify someone
        NotificationHandler nh = new NotificationHandler();
        nh.setSubject("Sensor OFFLINE: " + s.getName());
        nh.setBody("Sensor OFFLINE: " + s.getName());
        nh.send();
      }
    }

    // do all the controllers
    List<Controller> controllers = ofy().load().type(Controller.class).list();
    for (Controller c : controllers) {
      if (c.isExpired()) {
        // it's expired, notify someone
        NotificationHandler nh = new NotificationHandler();
        nh.setSubject("Controller OFFLINE: " + c.getName());
        nh.setBody("Controller OFFLINE: " + c.getName());
        nh.send();
      }
    }
    
    // remove old notifications
    List<NotificationLog> notifications = ofy().load().type(NotificationLog.class).list();
    ArrayList<NotificationLog> toclear = new ArrayList();
    for (NotificationLog nl : notifications) {
      if (nl.getLastnotification().plusDays(1).isBeforeNow()) { // 24 hours default
        toclear.add(nl);
      }
    }
    ofy().delete().entities(toclear).now();
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
