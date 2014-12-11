package com.openhouseautomation;

import com.google.apphosting.api.ApiProxy;
import com.googlecode.objectify.Work;
import static com.openhouseautomation.OfyService.ofy;
import com.openhouseautomation.model.Controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Enumeration;
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
public class ListenServlet extends HttpServlet {

  private static final long serialVersionUID = 1L;
  private static final Logger log = Logger.getLogger(ListenServlet.class.getName());
  long timeout = 5000L; // stop looping when this many ms are left in the request timer

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
    try (PrintWriter out = response.getWriter()) {
      // this connection stays open until timeout or a value is sent back
      // (as the result of a change)
      response.setContentType("text/plain");

      final ArrayList<Controller> cinitial = this.arrangeRequest(request);
      log.log(Level.INFO, "got {0} keys to listen for", cinitial.size());
      boolean foundachange = false;
      while (ApiProxy.getCurrentEnvironment().getRemainingMillis() > timeout && !out.checkError() && !foundachange) {
        // do we have new info to hand back?
        // walk the ArrayList, load each Controller, compare values against original
        // each transaction gets a fresh, empty cache, so loads will load from datastore
        log.log(Level.INFO, "new transaction");
        Controller c = ofy().transact(new Work<Controller>() {
          public Controller run() {
            for (Controller controllercompareinitial : cinitial) {
              Controller controllernew = ofy().cache(false).load().type(Controller.class).id(controllercompareinitial.getId()).now();
              String newval = controllernew.getDesiredState();
              log.log(Level.INFO, "init={0} current={1}", new Object[]{controllercompareinitial.getDesiredState(),
                newval});
              if (!controllercompareinitial.getDesiredState().equals(newval)) {
                // send the new value back & close
                return controllernew;
              }
            }
            return null;
          }
        });
        if (c != null) {
          foundachange=true;
          response.setStatus(HttpServletResponse.SC_OK);
          out.write(c.getId() + "=" + c.getDesiredState() + ";" + c.getLastDesiredStateChange().getTime()/1000);
          out.flush();
          out.close();
          return;
        }
        if (out.checkError()) {
          return;
        }
        // TODO(dras): how to detect if client disconnected?
        try {
          log.log(Level.INFO, "zzz...");
          Thread.sleep(2000);
        } catch (InterruptedException e) {
        }
      }
      // if you get to this point (timeout), the value didn't change
      response.setStatus(HttpServletResponse.SC_NO_CONTENT);
      // returns 204, no content, which tells the client to
      // immediately reconnect
    }
  }

  /**
   * Transforms the HTTP Request into a set of EventBeans
   *
   * @param req The servlet request
   * @return An ArrayList of EventBeans parsed from the HTTP Request
   * @throws IOException
   */
  public ArrayList<Controller> arrangeRequest(HttpServletRequest req) throws IOException {
    log.log(Level.INFO, "Starting arrangeRequest");
    ArrayList<Controller> ebs = new ArrayList<>();
    for (Enumeration<String> paramNames = req.getParameterNames(); paramNames.hasMoreElements();) {
      String controllerid = paramNames.nextElement();
      log.log(Level.INFO, "got an id:{0}", controllerid);
      if ("auth".equals(controllerid)) {
        continue; // the auth isn't a bean
      }
      Controller controller = ofy().load().type(Controller.class).id(Long.parseLong(controllerid)).now();
      ebs.add(controller);
      log.log(Level.INFO, "arrange listener:{0}", controller);
    }
    return ebs;
  }

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
}
