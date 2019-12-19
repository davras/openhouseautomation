package com.openhouseautomation.manage;

import com.google.appengine.api.users.UserServiceFactory;
import com.openhouseautomation.Convutils;
import static com.openhouseautomation.OfyService.ofy;

import com.openhouseautomation.model.Sensor;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.CRC32;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class AddSensorServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;
  private static final Logger log = Logger.getLogger(AddSensorServlet.class.getName());

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
      IOException {
    req.getRequestDispatcher("/WEB-INF/jsp/addsensor.jsp").forward(req, resp);
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
      IOException {
    log.info("doPost Sensor");
    Sensor sens = new Sensor();
    String salt = UserServiceFactory.getUserService().getCurrentUser().getUserId();

    // has the form been submitted with a setpoint change?
    boolean formvalid =
        ((req.getParameter("owner") != null) && (!"".equals(req.getParameter("owner"))));

    if (formvalid) {
      sens.setOwner(req.getParameter("owner"));
      sens.setLocation(req.getParameter("location"));
      sens.setZone(req.getParameter("zone"));
      sens.setType(Sensor.Type.valueOf(req.getParameter("type")));
      sens.setName(req.getParameter("name"));
      sens.setUnit(sens.getDefaultUnits());
      sens.setLastReading("99");
      sens.setLastReadingDate(Convutils.getNewDateTime());
      sens.setExpirationTime(3600);

      CRC32 hash = new CRC32();
      hash.update(salt.getBytes());
      hash.update(sens.getOwner().getBytes());
      hash.update(sens.getLocation().getBytes());
      hash.update(sens.getZone().getBytes());
      hash.update(sens.getType().hashCode());
      sens.setId(hash.getValue());

      Sensor sensexists = ofy().load().type(Sensor.class).id(sens.getId()).now();

      if (sensexists == null) {
        ofy().save().entity(sens).now();
        log.log(Level.INFO, "Sensor added with id: {0}", sens.getId());
        req.setAttribute("message", "Sensor added successfully, ID is " + sens.getId());
        req.setAttribute("messageLevel", "success");
      } else {
        log.log(Level.WARNING, "Sensor already exists with id: {0}", sens.getId());
        req.setAttribute("message", "Sensor already exists with id: " + sens.getId());
        req.setAttribute("messageLevel", "danger");
      }
      req.getRequestDispatcher("/WEB-INF/jsp/addsensor.jsp").forward(req, resp);
    }
  }

}
