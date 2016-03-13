/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.openhouseautomation.cron;

import static com.openhouseautomation.OfyService.ofy;
import com.openhouseautomation.model.Controller;
import java.io.IOException;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

/**
 *
 * @author dave
 */
public class HouseTimers extends HttpServlet {

  private static final long serialVersionUID = 1L;
  private static final Logger log = Logger.getLogger(HouseTimers.class.getName());

  private int curhour;
  private int curmin;

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
    response.setContentType("text/plain;charset=UTF-8");
    updateTime();
    doChargers();
    response.sendError(HttpServletResponse.SC_OK);
  }

  public void updateTime() {
    DateTime now = new DateTime(DateTimeZone.forID("America/Los_Angeles"));
    this.curhour = now.getHourOfDay();
    this.curmin = now.getMinuteOfHour();
  }

  public void doChargers() {
    if (curhour == 23 && (curmin == 0 || curmin == 1)) {
      // Charger on at 11pm
      setController(91125605L, "1");
    }
    if (curhour == 02 && (curmin == 0 || curmin == 1)) {
      // Charger off at 2am
      setController(91125605L, "0");
    }
    if (curhour == 6 && (curmin == 0 || curmin == 1)) {
      // Charger on at 6am
      setController(91125605L, "1");
    }
    if (curhour == 8 && (curmin == 0 || curmin == 1)) {
      // Charger off at 8am
      setController(91125605L, "0");
    }
  }

  public void setController(Long controllerid, String state) {
    ofy().clear();
    Controller controller = ofy().load().type(Controller.class).id(controllerid).now();
    controller.setDesiredState(state);
    ofy().save().entity(controller).now();
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
