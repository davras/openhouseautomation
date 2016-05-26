package com.openhouseautomation.display;

import static com.openhouseautomation.OfyService.ofy;

import com.openhouseautomation.model.Forecast;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author dras
 */
public class ForecastServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;

  /**
   * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
   *
   * @param request servlet request
   * @param response servlet response
   * @throws ServletException if a servlet-specific error occurs
   * @throws IOException if an I/O error occurs
   */
  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    response.setContentType("text/plain;charset=UTF-8");
    // <ver>.gautoard.appspot.com/forecast?k=95376&v=high
    // <ver>.gautoard.appspot.com/forecast?k=95376&v=low
    // <ver>.gautoard.appspot.com/forecast?k=95376&v=pop
    // <ver>.gautoard.appspot.com/forecast?k=95376&v=all
    // wget -q -O- "test.gautoard.appspot.com/forecast?k=95376&v=forecasthigh"

    try (PrintWriter out = response.getWriter()) {
      String zipcode = request.getParameter("k");
      if (zipcode == null || "".equals(zipcode)) {
        response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing zipcode (k=?)");
        return;
      }
      Forecast forecast = ofy().load().type(Forecast.class).id(zipcode).now();
      if (forecast == null) {
        response.sendError(HttpServletResponse.SC_NOT_FOUND,
            "I don't have information for that zip code");
        return;
      }
      String param = request.getParameter("v");
      if (null == param || "".equals(param)) {
        response.sendError(HttpServletResponse.SC_NOT_FOUND, "Use a param: {high,low,pop,all}");
        return;
      }
      if ("forecasthigh".equals(param)) {
        out.println(forecast.getForecastHigh());
      }
      if ("forecastlow".equals(param)) {
        out.println(forecast.getForecastLow());
      }
      if ("forecastpop".equals(param)) {
        out.println(forecast.getForecastPop());
      }
      if ("forecastall".equals(param)) {
        out.println("high=" + forecast.getForecastHigh());
        out.println("low=" + forecast.getForecastLow());
        out.println("pop=" + forecast.getForecastPop());
        out.println("last update=" + forecast.getLastUpdate().getMillis() / 1000);
        out.println("last update(human)=" + forecast.getLastUpdate());
      }
    }
  }
}
