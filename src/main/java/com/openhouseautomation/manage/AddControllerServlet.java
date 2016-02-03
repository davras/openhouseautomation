/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.openhouseautomation.manage;

import org.joda.time.DateTime;
import com.openhouseautomation.model.Controller;
import java.io.IOException;
import java.util.zip.CRC32;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import static com.openhouseautomation.OfyService.ofy;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 *
 * @author dave
 */
public class AddControllerServlet extends HttpServlet {

  private static final long serialVersionUID = 1L;
  private static final Logger log = Logger.getLogger(AddControllerServlet.class.getName());

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
    request.getRequestDispatcher("/WEB-INF/jsp/addcontroller.jsp").forward(request, response);
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
    log.info("doPost Controller");
    Controller cont = new Controller();
    String salt = "abc123";

    // has the form been submitted with a setpoint change?
    boolean formvalid
            = ((request.getParameter("owner") != null) && (!"".equals(request.getParameter("owner"))));

    if (formvalid) {
      cont.setOwner(request.getParameter("owner"));
      cont.setLocation(request.getParameter("location"));
      cont.setZone(request.getParameter("zone"));
      cont.setType(Controller.Type.getTypebyName(request.getParameter("type")));
      cont.setName(request.getParameter("name"));
      cont.setDesiredState("0");
      cont.setDesiredStatePriority(Controller.DesiredStatePriority.AUTO);
      cont.setActualState("0");
      cont.setLastDesiredStateChange(new DateTime());
      cont.setLastActualStateChange(new DateTime());

      // initialize the controller with some default valid states
      List vs = new ArrayList();
      // this should be some sort of ENUM, but it is controller-type specific
      // i.e. fan could have "on/off", or "off/low/high", or "0,1,2,3,4,5"
      if (cont.type == Controller.Type.WHOLEHOUSEFAN) {
        vs.add("0");
        vs.add("1");
        vs.add("2");
        vs.add("3");
        vs.add("4");
        vs.add("5");
      } else if (cont.type == Controller.Type.ALARM) {
        vs.add("DISARM");
        vs.add("HOME");
        vs.add("AWAY");
      } else if (cont.type == Controller.Type.PROJECTOR) {
        vs.add("0");
        vs.add("1");
      }
      cont.setValidStates(vs);

      CRC32 hash = new CRC32();
      hash.update(salt.getBytes());
      hash.update(cont.getOwner().getBytes());
      hash.update(cont.getLocation().getBytes());
      hash.update(cont.getZone().getBytes());
      cont.setId(hash.getValue());

      ofy().clear(); // clear the session cache, not the memcache
      Controller contexists = ofy().load().type(Controller.class).id(cont.getId()).now();

      if (contexists == null) {
        ofy().save().entity(cont).now();
        request.setAttribute("message", "Controller Added successfully, ID is " + cont.getId());
        request.setAttribute("messageLevel", "success");
        request.getRequestDispatcher("/WEB-INF/jsp/addcontroller.jsp").forward(request, response);
      } else {
        log.warning("Controller already exists with id: " + cont.getId());
        request.setAttribute("message", "Controller already exists with id: " + cont.getId());
        request.setAttribute("messageLevel", "danger");
        request.getRequestDispatcher("/WEB-INF/jsp/addcontroller.jsp").forward(request, response);
      }
    }
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
