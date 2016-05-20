/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.openhouseautomation.notification;

import com.google.appengine.api.xmpp.JID;
import com.google.appengine.api.xmpp.Message;
import com.google.appengine.api.xmpp.XMPPService;
import com.google.appengine.api.xmpp.XMPPServiceFactory;
import static com.openhouseautomation.OfyService.ofy;
import com.openhouseautomation.model.Controller;
import com.openhouseautomation.model.Sensor;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author dras
 */
public class XMPPIncomingHandler extends HttpServlet {

  private static final Logger log = Logger.getLogger(XMPPIncomingHandler.class.getName());

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
    XMPPService xmpp = XMPPServiceFactory.getXMPPService();
    Message message = xmpp.parseMessage(request);

    JID fromJid = message.getFromJid();
    String body = message.getBody();
    log.log(Level.FINE, "got an xmpp messsage with body: {0}", body);

    if (body != null && !"".equals(body)) {
      if (body.toLowerCase().contains("status")) {
        if (body.toLowerCase().contains("cont")) {
          // only controllers
          sendStatus(fromJid, getControllerStatus());
        } else if (body.toLowerCase().contains("sens")) {
          // only sensors
          sendStatus(fromJid, getSensorStatus());
        } else {
          // send both controllers and sensors
          sendStatus(fromJid, getControllerStatus());
          sendStatus(fromJid, getSensorStatus());
        }
      }
    }
  }

  protected void sendStatus(JID fromJID, String status) {
    NotificationHandler nhnotif = new NotificationHandler();
    nhnotif.setRecipient(fromJID.getId());
    nhnotif.setSubject("Response for query");
    nhnotif.setBody(status);
    nhnotif.alwaysSend();
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

  protected String getControllerStatus() {
    String toretoffline = "Offline controllers: ";
    String toretonline = "Online controllers: ";
    boolean offline=false;
    List<Controller> controllers = ofy().load().type(Controller.class).list();
    for (Controller c : controllers) {
      if (c.isExpired()) {
        offline=true;
        toretoffline += "\n  " + c.getName();
      } else {
        toretonline += "\n  " + c.getName();
      }
    }
    if (!offline) {
      return "All controllers are online";
    }
    return toretonline + "\n" + toretoffline;
  }
  
    protected String getSensorStatus() {
    String toretoffline = "Offline sensors: ";
    String toretonline = "Online sensors: ";
    boolean offline=false;
    List<Sensor> sensors = ofy().load().type(Sensor.class).list();
    for (Sensor s : sensors) {
      if (s.isExpired()) {
        offline=true;
        toretoffline += "\n  " + s.getName();
      } else {
        toretonline += "\n  " + s.getName();
      }
    }
    if (!offline) {
      return "All sensors are online";
    }
    return toretonline + "\n" + toretoffline;
  }
}
