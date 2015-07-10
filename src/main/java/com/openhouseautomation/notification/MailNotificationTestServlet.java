/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.openhouseautomation.notification;

import com.google.apphosting.api.ApiProxy;
import com.openhouseautomation.model.DatastoreConfig;
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
public class MailNotificationTestServlet extends HttpServlet {

  private static final Logger log = Logger.getLogger(MailNotificationTestServlet.class.getName());

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
    PrintWriter out = response.getWriter();
    String sender = DatastoreConfig.getValueForKey("e-mail sender", "notification@" + ApiProxy.getCurrentEnvironment().getAppId().substring(2) + ".appspotmail.com");
    String recipient = "rbruyere@gmail.com";
    try {
      Properties props = new Properties();
      Session session = Session.getDefaultInstance(props, null);
      String msgBody = "This is a test message sent from " + sender;
      Message msg = new MimeMessage(session);
      // will change s~gautoard to gautoard with substring
      // will not work on Master-Slave apps
      msg.setFrom(new InternetAddress(sender));
      msg.addRecipient(Message.RecipientType.TO,
              new InternetAddress(recipient, "RyanB (Open House Automation)"));
      // TODO pull sender from DS as config item
      msg.setSubject("Test mail from " + sender);
      msg.setText(msgBody);
      Transport.send(msg);
      out.println("SENT");
      out.println("From: " + sender);
      out.println("To: " + recipient);
    } catch (Exception e) {
      log.log(Level.WARNING, "error:" + e.fillInStackTrace());
      out.println("FAILED");
      out.println("From: " + sender);
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
