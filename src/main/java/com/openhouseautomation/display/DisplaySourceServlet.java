/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.openhouseautomation.display;

import com.google.appengine.api.datastore.QueryResultIterator;
import com.googlecode.objectify.cmd.Query;
import static com.openhouseautomation.OfyService.ofy;
import com.openhouseautomation.model.Sensor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.logging.Logger;
/**
 *
 * @author dave
 */
public class DisplaySourceServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;
  private static final Logger log = Logger.getLogger(DisplaySourceServletOld.class.getName());
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
    if (request.getPathInfo() == null) return;
    if (request.getPathInfo().startsWith("/display/sensors")) {
      doDisplaySensors(request, response);
      return;
    }
    response.getWriter().println("path not supported");
  }

  private void doDisplaySensors(HttpServletRequest request, HttpServletResponse response) throws IOException {
    PrintWriter out = response.getWriter();
    // dev on localhost
    if (request.getRemoteAddr().equals("127.0.0.1")) {
      response.getWriter().print(testString);
      return;
    }
    // production
    Query<Sensor> query = ofy().load().type(Sensor.class);
    QueryResultIterator<Sensor> iterator = query.iterator();
    out.print("[");
    while (iterator.hasNext()) {
      Sensor sens = (Sensor) iterator.next();
      out.print(sens.toJSONString());
      if (iterator.hasNext()) {
        out.print(",");
      }
    }
    out.print("]");
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
  
  }

  /**
   * Returns a short description of the servlet.
   *
   * @return a String containing servlet description
   */
  @Override
  public String getServletInfo() {
    return "Short description";
  }

  String testString = " [{\"id\":28131427,\"owner\":\"dras\",\"location\":\"home\",\"zone\":\"inside\",\"type\":\"TEMPERATURE\",\"name\":\"Inside Temperature\",\"unit\":\"F\",\"lastReading\":\"76.1\",\"lastReadingDate\":1413579508631,\"secret\":null,\"expirationTime\":3600,\"expired\":false},{\"id\":112578578,\"owner\":\"dras\",\"location\":\"home\",\"zone\":\"outsidewswindspdmph_avg2m\",\"type\":\"WINDSPEED\",\"name\":\"Outside Wind Speed 2 min avg\",\"unit\":\"mph\",\"lastReading\":\"0.91\",\"lastReadingDate\":1413579921659,\"secret\":null,\"expirationTime\":3600,\"expired\":false},{\"id\":1033578736,\"owner\":\"dras\",\"location\":\"home\",\"zone\":\"outsidewsbatt_lvl\",\"type\":\"VOLTAGE\",\"name\":\"Outside Battery Level\",\"unit\":\"V\",\"lastReading\":\"8.30\",\"lastReadingDate\":1413573007906,\"secret\":null,\"expirationTime\":3600,\"expired\":true},{\"id\":1186112788,\"owner\":\"dras\",\"location\":\"home\",\"zone\":\"outsidewsdailyrainin\",\"type\":\"RAIN\",\"name\":\"Outside Rain Daily\",\"unit\":\"inches\",\"lastReading\":\"0.00\",\"lastReadingDate\":1406837586351,\"secret\":null,\"expirationTime\":null,\"expired\":false},{\"id\":1439635287,\"owner\":\"dras\",\"location\":\"home\",\"zone\":\"outsidewswindgustdir_10m\",\"type\":\"WINDDIRECTION\",\"name\":\"Outside Wind Gust direction 10 min\",\"unit\":\"degrees\",\"lastReading\":\"180.00\",\"lastReadingDate\":1413579863455,\"secret\":null,\"expirationTime\":3600,\"expired\":false},{\"id\":1596290393,\"owner\":\"dras\",\"location\":\"home\",\"zone\":\"outsidewswinddir_avg2m\",\"type\":\"WINDDIRECTION\",\"name\":\"Outside Wind Direction 2 min avg\",\"unit\":\"degrees\",\"lastReading\":\"185.60\",\"lastReadingDate\":1413579741515,\"secret\":null,\"expirationTime\":3600,\"expired\":false},{\"id\":1714117207,\"owner\":\"dras\",\"location\":\"home\",\"zone\":\"outsidewsrainin\",\"type\":\"RAIN\",\"name\":\"Outside Rain Current\",\"unit\":\"inchesperhour\",\"lastReading\":\"0.00\",\"lastReadingDate\":1413388936786,\"secret\":null,\"expirationTime\":null,\"expired\":false},{\"id\":3130021022,\"owner\":\"dras\",\"location\":\"home\",\"zone\":\"outsidewstemp\",\"type\":\"TEMPERATURE\",\"name\":\"Outside Temperature\",\"unit\":\"F\",\"lastReading\":\"92.07\",\"lastReadingDate\":1413579440841,\"secret\":null,\"expirationTime\":3600,\"expired\":false},{\"id\":3409600514,\"owner\":\"dras\",\"location\":\"home\",\"zone\":\"outsidewspressure\",\"type\":\"PRESSURE\",\"name\":\"Outside Barometer\",\"unit\":\"inHg\",\"lastReading\":\"29.85\",\"lastReadingDate\":1413579561168,\"secret\":null,\"expirationTime\":3600,\"expired\":false},{\"id\":3470692084,\"owner\":\"dras\",\"location\":\"home\",\"zone\":\"outsidewswindgustmph_10m\",\"type\":\"WINDSPEED\",\"name\":\"Outside Wind Gust speed 10 min\",\"unit\":\"mph\",\"lastReading\":\"4.63\",\"lastReadingDate\":1413579801678,\"secret\":null,\"expirationTime\":3600,\"expired\":false},{\"id\":3829498030,\"owner\":\"dras\",\"location\":\"work\",\"zone\":\"2ndfloor\",\"type\":\"TEMPERATURE\",\"name\":\"Office Temperature\",\"unit\":\"F\",\"lastReading\":\"73.48\",\"lastReadingDate\":1413579701545,\"secret\":null,\"expirationTime\":3600,\"expired\":false},{\"id\":3885021817,\"owner\":\"dras\",\"location\":\"home\",\"zone\":\"outsidewslight_lvl\",\"type\":\"LIGHT\",\"name\":\"Outside Light Level\",\"unit\":\"pct\",\"lastReading\":\"3.15\",\"lastReadingDate\":1413579681168,\"secret\":null,\"expirationTime\":86400,\"expired\":false},{\"id\":3959642986,\"owner\":\"dras\",\"location\":\"work\",\"zone\":\"2ndfloor\",\"type\":\"PRESSURE\",\"name\":\"Office Barometer\",\"unit\":\"F\",\"lastReading\":\"30.01\",\"lastReadingDate\":1413577970474,\"secret\":null,\"expirationTime\":86400,\"expired\":false},{\"id\":3986975262,\"owner\":\"dras\",\"location\":\"home\",\"zone\":\"attic\",\"type\":\"TEMPERATURE\",\"name\":\"Attic Temperature\",\"unit\":\"F\",\"lastReading\":\"94\",\"lastReadingDate\":1413579967604,\"secret\":null,\"expirationTime\":3600,\"expired\":false},{\"id\":4251563943,\"owner\":\"dras\",\"location\":\"home\",\"zone\":\"outsidewshumidity\",\"type\":\"HUMIDITY\",\"name\":\"Outside Humidity\",\"unit\":\"pct\",\"lastReading\":\"22.24\",\"lastReadingDate\":1413579380480,\"secret\":null,\"expirationTime\":3600,\"expired\":false},{\"id\":5744863563743232,\"owner\":\"dras\",\"location\":\"home\",\"zone\":\"outsideshadtemp\",\"type\":\"TEMPERATURE\",\"name\":\"Outside Temperature Shaded\",\"unit\":\"F\",\"lastReading\":\"73.26\",\"lastReadingDate\":1413579065896,\"secret\":null,\"expirationTime\":3600,\"expired\":false}]";
}
