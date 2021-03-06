/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.openhouseautomation.notification;

import com.openhouseautomation.model.DatastoreConfig;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.logging.Level;
import java.util.logging.Logger;
// import javax.servlet.annotation.WebServlet; // 3.1.0 feature, not 2.5

/**
 *
 * @author dras
 */
// @WebServlet(name = "notificationtest", description = "Tests the owners pager", urlPatterns = "/notificationtest") // 3.1.0 feature, not 2.5
public class NotificationTestServlet extends HttpServlet {

  private static final Logger log = Logger.getLogger(NotificationTestServlet.class.getName());

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
    log.log(Level.SEVERE, "TESTING PAGER");
    response.setContentType("text/plain");
    PrintWriter out = response.getWriter();
    String recipient = DatastoreConfig.getValueForKey("pager", "bob@example.com");
    try {
      NotificationHandler nhnotif = new NotificationHandler();
      nhnotif.setRecipient(recipient);
      nhnotif.setSubject("Test notification from " + request.getRemoteHost());
      nhnotif.setBody("This was a test notification");
      nhnotif.page();
      out.println("SENT To: " + recipient);
    } catch (Exception e) {
      log.log(Level.WARNING, "error:" + e.fillInStackTrace());
      out.println("FAILED");
      out.println("To: " + recipient);
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
