/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.openhouseautomation.cron;

import com.google.common.base.Strings;
import com.openhouseautomation.Convutils;
import static com.openhouseautomation.OfyService.ofy;
import com.openhouseautomation.model.Controller;
import com.openhouseautomation.model.DatastoreConfig;
import com.openhouseautomation.model.EventLog;
import com.openhouseautomation.notification.NotificationHandler;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;
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
public class CronController extends HttpServlet {

  Logger log = Logger.getLogger(CronController.class.getName());

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
    final String reqpath = URLDecoder.decode(request.getPathInfo(), "UTF-8");
    log.log(Level.INFO, "request path:" + reqpath);

    if (Strings.isNullOrEmpty(reqpath)) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing controller or path");
      return;
    }

    // extract values from request
    PrintWriter out = response.getWriter();
    StringTokenizer st1 = new StringTokenizer(reqpath, "/");
    String controllerid = (String) st1.nextToken(); // the controller id
    String desiredvalue = (String) st1.nextToken(); // the desired controller value
    log.log(Level.INFO, "c={0},v={1}", new Object[]{controllerid, desiredvalue});

    // load the controller
    if (com.openhouseautomation.Flags.clearCache) {
      ofy().clear(); // clear the session cache, not the memcache
    }
    Controller controller = ofy().load().type(Controller.class).id(Long.parseLong(controllerid)).now();

    // check that everything looks good
    if (controller == null) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing controller or path");
      return;
    }

    if (!controller.getActualState().equals(desiredvalue)) {
      log.log(Level.INFO, "Setting controller {0} to {1}",
              new Object[]{controller.getName(), desiredvalue});
      controller.setDesiredState(desiredvalue);
      ofy().save().entity(controller).now();
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
