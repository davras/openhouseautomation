/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.openhouseautomation;

import static com.openhouseautomation.OfyService.ofy;
import com.openhouseautomation.model.Forecast;
import com.openhouseautomation.model.Sensor;
import com.openhouseautomation.model.LCDDisplay;
import java.io.IOException;
import java.io.PrintWriter;
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
public class LCDDisplayServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(LCDDisplayServlet.class.getName());

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-sepcific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String displayk = request.getParameter("k");
        if (displayk == null || "".equals(displayk)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No k= provided, please use a display name");
        }
        LCDDisplay lcdd = ofy().load().type(LCDDisplay.class).id(displayk).now();
        response.setContentType("text/plain");
//      I:{28131427} O:{3130021022}\n
//      {3409600514}inHg {4251563943}%RH\n
//      FC:{FCH95376}>{FCL95376} {FCP95376}%POP
        try (PrintWriter out = response.getWriter()) {
            out.println(replaceTokens(lcdd.getDisplayString()));
        }
    }

    public String replaceTokens(String s) {
        while (s.contains("{")) {
            int bgnidx = s.indexOf("{");
            int endidx = s.indexOf("}");
            String item = s.substring(bgnidx + 1, endidx);
            log.log(Level.FINE, "item={0}", item);
            if (item.startsWith("FC")) {
                String tkresp = getForecast(item);
                s = s.substring(0, bgnidx) + tkresp + s.substring(endidx + 1);
            } else {
                long sensid = Long.parseLong(item);
                String sensrd = getSensorReading(sensid);
                s = s.substring(0, bgnidx) + sensrd + s.substring(endidx + 1);
            }
        }
        return s;
    }

    public String getSensorReading(long l) {
        return ofy().load().type(Sensor.class).id(l).now().getLastReading();
    }

    public String getForecast(String token) {
        String zipcode = token.substring(3);
        Forecast forecast = ofy().load().type(Forecast.class).id(zipcode).now();
        if (token.startsWith("FCH")) {
            return forecast.getForecastHigh();
        }
        if (token.startsWith("FCL")) {
            return forecast.getForecastLow();
        }
        if (token.startsWith("FCP")) {
            return forecast.getForecastPop();
        }
        return "X";
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
