package com.openhouseautomation.cron;

import com.google.common.base.Strings;
import com.openhouseautomation.Convutils;
import static com.openhouseautomation.OfyService.ofy;
import com.openhouseautomation.model.Forecast;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;

/**
 *
 * @author dras
 */
public class WeatherForecast extends HttpServlet {

  private static final long serialVersionUID = 1L;
  private static final Logger log = Logger.getLogger(WeatherForecast.class.getName());
  String surl
          = "https://graphical.weather.gov/xml/SOAP_server/ndfdXMLclient.php?whichClient=NDFDgenMultiZipCode&product=time-series&Unit=e&maxt=maxt&mint=mint&pop12=pop12&Submit=Submit";

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
    try (PrintWriter out = response.getWriter()) {
      // first, get the zip code
      String zipcode = request.getParameter("zipcode");
      if (Strings.isNullOrEmpty(zipcode)) {
        response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing zipcode");
        return;
      }
      long curtime = System.currentTimeMillis();
      // TODO: change the param to read the list of zips from DS/Obj
      URL url = new URL(surl + "&zipCodeList=" + zipcode);
      // the URL returns an XML doc
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      DocumentBuilder db = dbf.newDocumentBuilder();
      Document doc = db.parse(url.openStream());
      XPathFactory xPathfactory = XPathFactory.newInstance();
      XPath xpath = xPathfactory.newXPath();

      XPathExpression exprmin
              = xpath.compile("/dwml/data/parameters/temperature[@type='minimum']/value[1]");
      String minimum = (String) exprmin.evaluate(doc, XPathConstants.STRING);
      // update datastore
      // This is a lazy load so invalid zipcodes do not create invalid entities.
      // Helps prevent user error
      Forecast forecast = ofy().load().type(Forecast.class).id(zipcode).now();
      if (forecast == null) {
        forecast = new Forecast();
        forecast.setZipCode(zipcode);
      }
      forecast.setForecastLow(minimum);
      log.log(Level.INFO, "forecast low=" + minimum);
      XPathExpression exprmax
              = xpath.compile("/dwml/data/parameters/temperature[@type='maximum']/value[1]");
      String maximum = (String) exprmax.evaluate(doc, XPathConstants.STRING);
      forecast.setForecastHigh(maximum);
      log.log(Level.INFO, "forecast high=" + minimum);

      XPathExpression exprpop
              = xpath.compile("/dwml/data/parameters/probability-of-precipitation[@type='12 hour']/value[1]");
      String pop = (String) exprpop.evaluate(doc, XPathConstants.STRING);
      forecast.setForecastPop(pop);
      log.log(Level.INFO, "forecast pop=" + pop);
      forecast.setLastUpdate(Convutils.getNewDateTime());

      log.log(Level.INFO, "forecast cron successful in {0}ms", (System.currentTimeMillis() - curtime));
      
      if (Strings.isNullOrEmpty(minimum) || Strings.isNullOrEmpty(maximum)) {
        return;
      }
      ofy().save().entity(forecast).now();
      out.println(forecast);
    } catch (Exception e) {
        log.log(Level.SEVERE, e.toString(), e.fillInStackTrace());
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
