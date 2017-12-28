package com.openhouseautomation.cron;

import com.google.common.base.Strings;
import com.openhouseautomation.Convutils;
import static com.openhouseautomation.OfyService.ofy;
import com.openhouseautomation.logic.HouseFan;
import com.openhouseautomation.logic.WeightedDecision;
import com.openhouseautomation.model.Controller;
import com.openhouseautomation.model.DatastoreConfig;
import com.openhouseautomation.notification.NotificationHandler;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.joda.time.DateTime;

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
    log.log(Level.INFO, "Time:{0}:{1}", new Object[]{curhour, curmin});
    doChargers();
    doLights();
    doBoatPump();
    turnOffHouseFan();
    notifyTurnOnHouseFan();
    response.setStatus(HttpServletResponse.SC_OK);
  }

  public void updateTime() {
    DateTime now = Convutils.getNewDateTime();
    this.curhour = now.getHourOfDay();
    this.curmin = now.getMinuteOfHour();
  }

  public void doBoatPump() {
    if (curhour == 18 && curmin == 0) {
      // Pump on at 6pm
      log.log(Level.INFO, "Turning boat pump on");
      setController(3960328784L, "1");
    } else if (curmin == 1) {
      // turn off 1 min after every hour
      log.log(Level.INFO, "Turning boat pump off");
      setController(3960328784L, "0");
    }
  }

  public void doChargers() {
    if (curhour == 23 && curmin < 2) {
      // Charger on at 11pm
      log.log(Level.INFO, "Turning chargers on");
      setController(91125605L, "1");
    }
    if (curhour == 2 && curmin < 2) {
      // Charger off at 2am
      log.log(Level.INFO, "Turning chargers off");
      setController(91125605L, "0");
    }
  }
  
    public void doLights() {
    if (curhour == 0 && curmin < 2) {
      // Lights off at midnight
      log.log(Level.INFO, "Turning lights off");
      setController(3640433672L, "0");
    }
    if (curhour == 6 && curmin > 29 && curmin < 32) {
      // Lights on a 6:30am
      log.log(Level.INFO, "Turning chargers off");
      setController(3640433672L, "1");
    }
  }

  public void turnOffHouseFan() {
    if (curhour == 8 && curmin < 2) {
      // House Fan off at 8am
      log.log(Level.INFO, "Turning off house fan");
      if (com.openhouseautomation.Flags.clearCache) ofy().clear(); // clear the session cache, not the memcache
      Controller controller = ofy().load().type(Controller.class).id(4280019022L).now();
      boolean dirty = false;
      if (!"0".equals(controller.getDesiredState())) {
        controller.setDesiredState("0");
        NotificationHandler nhnotif = new NotificationHandler();
        nhnotif.setRecipient(DatastoreConfig.getValueForKey("admin", "bob@example.com"));
        nhnotif.setSubject("Fan Speed");
        nhnotif.setBody("Turned off House Fan");
        nhnotif.page();
        dirty = true;
      }
      if (!Controller.DesiredStatePriority.MANUAL.equals(controller.getDesiredStatePriority())) {
        controller.setDesiredStatePriority(Controller.DesiredStatePriority.MANUAL);
        dirty = true;
      }
      if (dirty) {
        ofy().save().entity(controller).now();
      }
    }
  }

  public void notifyTurnOnHouseFan() {
    if (curhour < 17 || curmin > 0) {
      return; // from 5pm to midnight on the hour
    }
    Controller alarm = ofy().load().type(Controller.class).filter("name", "Alarm").first().now();
    if (alarm.getActualState().equalsIgnoreCase("Away")) {
      // don't check house fan if nobody is home
      return;
    }
    HouseFan hf = new HouseFan();
    String hfnotify = hf.notifyInManual();
    if (!Strings.isNullOrEmpty(hfnotify)) {
      WeightedDecision wd = hf.getWeightedDecision();
      wd.setId(4280019022L);
      ofy().save().entity(wd);
      NotificationHandler nhnotif = new NotificationHandler();
      nhnotif.setRecipient(DatastoreConfig.getValueForKey("admin", "bob@example.com"));
      nhnotif.setSubject("Recommend House Fan in AUTO");
      nhnotif.setBody(hfnotify);
      nhnotif.page();
    }
  }

  public void setController(Long controllerid, String state) {
    if (com.openhouseautomation.Flags.clearCache) ofy().clear(); // clear the session cache, not the memcache
    Controller controller = ofy().load().type(Controller.class).id(controllerid).now();
    if (controller != null && !controller.getDesiredState().equals(state)) {
      controller.setDesiredState(state);
      ofy().save().entity(controller).now();
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
