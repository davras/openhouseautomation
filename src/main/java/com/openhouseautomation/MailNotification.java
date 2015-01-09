/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.openhouseautomation;

import com.google.apphosting.api.ApiProxy;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 *
 * @author dras
 */
public class MailNotification extends HttpServlet {

  private static final Logger log = Logger.getLogger(MailNotification.class.getName());

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
    response.setContentType("text/plain");
    try (PrintWriter out = response.getWriter()) {
      Properties props = new Properties();
      Session session = Session.getDefaultInstance(props, null);
      String msgBody = "This is a test message.";
      Message msg = new MimeMessage(session);
      msg.setFrom(new InternetAddress("notification@" + ApiProxy.getCurrentEnvironment().getAppId() + ".appspotmail.com", "OpenHouseAutomation Notification"));
      //msg.setFrom(new InternetAddress("davras@gmail.com", "Open House Automation admin"));
      msg.addRecipient(Message.RecipientType.TO,
          new InternetAddress("davras@gmail.com", "David Ras (Open House Automation)"));
      // TODO pull sender from DS as config item
      msg.setSubject("TestSubject");
      msg.setText(msgBody);
      Transport.send(msg);
      out.println("SENT");
    } catch (Exception e) {
      log.log(Level.WARNING, "error:" + e.fillInStackTrace());
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
