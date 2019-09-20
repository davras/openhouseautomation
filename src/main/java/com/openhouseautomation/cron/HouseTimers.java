package com.openhouseautomation.cron;

import com.google.common.base.Strings;
import com.openhouseautomation.Convutils;
import static com.openhouseautomation.OfyService.ofy;
import com.openhouseautomation.logic.HouseFan;
import com.openhouseautomation.model.Controller;
import com.openhouseautomation.notification.NotificationHandler;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.joda.time.DateTime;
import com.openhouseautomation.model.DatastoreConfig;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

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
   * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
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
    notifyTurnOnHouseFan();
    updateLightColor();
    response.setStatus(HttpServletResponse.SC_OK);
  }

  public void updateTime() {
    DateTime now = Convutils.getNewDateTime();
    this.curhour = now.getHourOfDay();
    this.curmin = now.getMinuteOfHour();
  }

  public void updateLightColor() {
    Controller workcolor = ofy().load().type(Controller.class).id(2261732907L).now();
    workcolor.setDesiredState(lightLookup());
    ofy().save().entity(workcolor);
    setcolor(workcolor);
    
    Controller housecolor = ofy().load().type(Controller.class).id(28131427L).now();
    housecolor.setDesiredState(lightLookup());
    
    Controller projector = ofy().load().type(Controller.class).id(4157520376L).now();
    if (projector.getActualState().equals("1")) {
      log.log(Level.INFO, "Movie lights");
      housecolor.setDesiredState("#653d00");
    }
    
    Controller alarmcontroller = ofy().load().type(Controller.class).id(3964578029L).now();
    if (alarmcontroller.getActualState().equals("Away")) {
      log.log(Level.INFO, "Alarm is set");
      housecolor.setDesiredState("#000000");
    }
    ofy().save().entity(housecolor);
    setcolor(housecolor);
    // TODO publish a hook response to update devices
    
  }
  
  public void notifyTurnOnHouseFan() {
    if (curhour < 17 || curmin > 0 ) {
      return; // from 5pm to midnight on the hour
    }
    if (curmin%15 > 0) {
      // only run once every 15m
      return;
    }
    Controller alarm = ofy().load().type(Controller.class).filter("name", "Alarm").first().now();
    if (alarm.getActualState().equalsIgnoreCase("Away")) {
      // don't check house fan if nobody is home
      return;
    }
    HouseFan hf = new HouseFan();
    String hfnotify = hf.notifyInManual();
    if (!Strings.isNullOrEmpty(hfnotify)) {
      NotificationHandler nhnotif = new NotificationHandler();
      // don't need to set recipient if paging
      //nhnotif.setRecipient(DatastoreConfig.getValueForKey("admin", "bob@example.com"));
      nhnotif.setSubject("Recommend House Fan in AUTO");
      nhnotif.setBody(hfnotify);
      nhnotif.page();
    }
  }
  public String lightLookup() {
    // a pretty, pretty mood setting color for the day
    DateTime now = Convutils.getNewDateTime();
    float hourmin = now.getHourOfDay();
    hourmin += now.getMinuteOfHour()/60.0;
    int r=0, g=0, b=0;
    if (hourmin < 6.45) return "#000000";
    if (hourmin < 7) {
      // map blue to bright
      r = g = maptoi(hourmin, 0, 50, 6.45, 7.0);
      b = maptoi(hourmin, 0, 255, 6.45, 7.0);
    } else if (hourmin < 12) {
      // map to yellow
      r = g = maptoi(hourmin, 50, 204, 7.0, 12.0);
      b = maptoi(hourmin, 255, 0, 7.0, 12.0);
    } else if (hourmin < 20) {
      // map to orange
      r=204;
      g=maptoi(hourmin, 204, 153, 12.0, 20.0);
      b=0;
    } else if (hourmin < 22) {
      // map to purple
      r=maptoi(hourmin, 204, 102, 20.0, 22.0);
      g=maptoi(hourmin, 153, 0, 20.0, 22.0);
      b=maptoi(hourmin, 0, 255, 20.0, 22.0);
    } else if (hourmin < 24) {
      // map to low red
      r=maptoi(hourmin, 102, 60, 22.0, 24.0);
      g=0;
      b=maptoi(hourmin, 255, 0,22.0, 24.0);
    }
    log.log(Level.INFO, "Response: @" + hourmin + ":" + r + "/" + g + "/" + b + "=" + rgbtoHex(r, g, b));
    return rgbtoHex(r, g, b);
  }
  
  private String intToHex(int i) {
    if (i < 10) {
      return "0" + Integer.toHexString(i);
    }
    return Integer.toHexString(i);
  }
  private String rgbtoHex(int r, int g, int b) {
    return "#" + intToHex(r)+ intToHex(g) + intToHex(b);
  }
  private static int maptoi(final double unscaledNum, final double minOutput, final double maxOutput, final double minInput, final double maxInput) {
    return (int)((maxOutput - minOutput) * (unscaledNum - minInput) / (maxInput - minInput) + minOutput);
  }

  public void setcolor(Controller cont) {
    //make API call to set the color on all devices
    String access_token = DatastoreConfig.getValueForKey("particleapiaccesstoken", "");
    if (com.google.api.client.util.Strings.isNullOrEmpty(access_token)) {
      return;
    }
    try {
      String url = "https://api.particle.io/v1/devices/events";
      URL obj = new URL(url);
      HttpURLConnection con = (HttpURLConnection) obj.openConnection();
      con.setDoOutput(true);
      con.setInstanceFollowRedirects(false);
      //con.addRequestProperty("Content-Type", "application/json");
      con.addRequestProperty("Authorization", "Bearer " + access_token);
      con.setRequestMethod("POST");
      //con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

      log.log(Level.INFO, "Sending 'POST' request to URL : " + url);
      // Send post request
      DataOutputStream wr = new DataOutputStream(con.getOutputStream());
      wr.writeBytes("name=lightcolor/");
      wr.writeBytes(cont.getLocation());
      wr.writeBytes("&data=");
      wr.writeBytes(cont.getId().toString());
      wr.writeBytes("/");
      wr.writeBytes(cont.getDesiredState());
      wr.writeBytes("&private=true&ttl=60");
      wr.flush();
      wr.close();
      int responseCode = con.getResponseCode();
      log.log(Level.INFO, "Response Code : " + responseCode);

      BufferedReader in = new BufferedReader(
              new InputStreamReader(con.getInputStream()));
      String inputLine;
      StringBuffer response = new StringBuffer();

      while ((inputLine = in.readLine()) != null) {
        response.append(inputLine);
      }
      in.close();
      log.log(Level.INFO, "Response: " + response);
    } catch (Exception e) {
      log.log(Level.SEVERE, "ERR:" + e.getMessage(), e.getCause());
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
