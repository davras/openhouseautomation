package com.openhouseautomation.cron;


import static com.openhouseautomation.OfyService.ofy;

import com.openhouseautomation.model.Reading;
import com.googlecode.objectify.Key;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * For temperature readings older than 7 days, save only the high and low for the day.
 *
 * @author dras
 */
public class DatastoreLogTruncator extends HttpServlet {

  private static final long serialVersionUID = 1L;
  private static final Logger log = Logger.getLogger(DatastoreLogTruncator.class.getName());

  /**
   * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
   *
   * @param request servlet request
   * @param response servlet response
   * @throws ServletException if a servlet-specific error occurs
   * @throws IOException if an I/O error occurs
   */
  protected void processRequest(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    response.setContentType("text/html;charset=UTF-8");
    PrintWriter out = response.getWriter();
    long minage = 6;
    try {
      minage = Long.parseLong(getServletConfig().getInitParameter("age"));
    } catch (Exception e) {
      // TODO(dras): If no action is required, add justification in a comment.
    }
    try {
      Date cutoffdate = new Date(System.currentTimeMillis() - (minage * 86400000));

      log.log(Level.INFO, "aggregating keys older than {0}", cutoffdate);

      Iterable<Key<Reading>> oldreadings =
          ofy().load().type(Reading.class).filter("timestamp <", cutoffdate).keys();
      ofy().delete().keys(oldreadings);
    } catch (Exception e) {
      e.printStackTrace();
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
          "Cron failed: " + e.getMessage());

    } finally {
      out.close();
    }
  }


  // <editor-fold defaultstate="collapsed"
  // desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
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
  }// </editor-fold>
}
