/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.openhouseautomation.logic;

import static com.openhouseautomation.OfyService.ofy;
import com.openhouseautomation.model.Reading;
import com.openhouseautomation.model.Sensor;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.logging.Level;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.joda.time.DateTime;

/**
 *
 * @author dave
 */
public class TestTemperatureSlope extends HttpServlet {

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
    response.setContentType("text/html;charset=UTF-8");
    try (PrintWriter out = response.getWriter()) {
      out.println("<!DOCTYPE html>");
      out.println("<html>");
      out.println("<head>");
      out.println("<title>Servlet TestTemperatureSlope</title>");
      out.println("</head>");
      out.println("<body>");
      out.println("<h1>Servlet TestTemperatureSlope at " + request.getContextPath() + "</h1>");
      out.println("time,reading<br>");
      Sensor sens = ofy().load().type(Sensor.class).filter("name", "Outside Temperature").first().now();
      // get the Sensor
      // get the readings for that sensor
      DateTime dt = new DateTime().minusSeconds(60 * 60 * 2);
      List<Reading> readings = ofy().load().type(Reading.class)
              .ancestor(sens).filter("timestamp >", dt).list();
      // setup the linear regression
      SimpleRegression sreg = new SimpleRegression(true); // do not compute the intercept, just the slope
      int loggedreadings = 0;
      long firsttime = 0;
      StringBuffer sb = new StringBuffer();
      for (Reading readhistory : readings) {
        // get the slope over hours, because:
        // -1F change in 3600 seconds = -0.00028
        // -1F change in 1 hour = -1
        // TODO but this means your lookback time is in seconds, and the returned slope is in minutes
        //long histval = readhistory.getTimestamp().getMillis() / 1000 / 60;
        long histval = readhistory.getTimestamp().getMillis()/1000/60;
        // reference later times to the zero time of the first reading
        if (firsttime == 0) {
          firsttime = histval;
          histval = 0;
        } else {
          histval -= firsttime;
        }
        sreg.addData(histval, Double.parseDouble(readhistory.getValue()));
        loggedreadings++;
        sb.append("#" + loggedreadings + ": start reading time: " + firsttime + ", this reading: " + 
                readhistory.getValue() + "F, @" + histval + ", slope is: " + sreg.getSlope() + "F/hr, slope confidence interval: " + 
                sreg. getSlopeConfidenceInterval()+ "<br>\n");
        out.println(histval + "," + readhistory.getValue() + "<br>");
      }
      out.println("<hr>");
      out.println(sb);
      out.println("</body>");
      out.println("</html>");
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
