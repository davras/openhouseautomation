/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.openhouseautomation;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.Work;
import static com.openhouseautomation.OfyService.ofy;
import com.openhouseautomation.model.Controller;
import com.openhouseautomation.model.Reading;
import com.openhouseautomation.model.Sensor;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
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
    try (PrintWriter out = response.getWriter()) {
      String auth = request.getParameter("auth");
      final String controllerid = request.getParameter("k");
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
      if (controller != null) {
        out.println(controller.getId() + "=" + controller.getDesiredState() + ";" + controller.getLastStateChange().getTime() / 1000);
        log.log(Level.INFO, "sent:{0}={1};{2}", new Object[]{controller.getId(), controller.getDesiredState(), controller.getLastStateChange().getTime() / 1000});
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
    final String controllerid = request.getParameter("k");
    final String controllervalue = request.getParameter("v");
    // TODO cleanup the anonymous inner class
    log.log(Level.INFO, "k={0},v={1}", new Object[]{controllerid, controllervalue});
    if (SipHashHelper.validateHash(controllerid, controllervalue, auth)) {
      log.log(Level.INFO, "1. checking siphash auth: {0}", auth);
      ofy().transact(new Work<Controller>() {
        @Override
        public Controller run() {
          Key<Controller> ck = Key.create(Controller.class, Long.parseLong(controllerid));
          Controller controller = ofy().load().now(ck);
          // set the value
          controller.setLastStateChange(new Date());
          controller.setActualState(controllervalue);
          ofy().save().entity(controller);
          log.log(Level.INFO, "saved controller setting:{0}", controller);
          return controller;
        }
      });
      out.println("OK");
    } else {
      response.sendError(HttpServletResponse.SC_FORBIDDEN, "Unauthorized, please use your auth key");
      // TODO: move auth to filter servlet
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

}
