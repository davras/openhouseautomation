package com.openhouseautomation.manage;

import com.google.appengine.repackaged.org.joda.time.DateTime;
import static com.openhouseautomation.OfyService.ofy;

import com.openhouseautomation.model.Sensor;

import java.io.IOException;
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
    String salt = "abc123";

    // has the form been submitted with a setpoint change?
    boolean formvalid =
        ((req.getParameter("owner") != null) && (!"".equals(req.getParameter("owner"))));

    if (formvalid) {
      sens.setOwner(req.getParameter("owner"));
      sens.setLocation(req.getParameter("location"));
      sens.setZone(req.getParameter("zone"));
      sens.setType(Sensor.Type.valueOf(req.getParameter("type")));
      sens.setName(req.getParameter("name"));
      sens.setUnit("F");
      sens.setLastReading("99");
      sens.setLastReadingDate(new DateTime());
      sens.setExpirationTime(new Long(3600));

      CRC32 hash = new CRC32();
      hash.update(salt.getBytes());
      hash.update(sens.getOwner().getBytes());
      hash.update(sens.getLocation().getBytes());
      hash.update(sens.getZone().getBytes());
      sens.setId(hash.getValue());

      Sensor sensexists = ofy().load().type(Sensor.class).id(sens.getId()).now();

      if (sensexists == null) {
        ofy().save().entity(sens).now();
        req.setAttribute("message", "Sensor added successfully, ID is " + sens.getId());
        req.setAttribute("messageLevel", "success");
        req.getRequestDispatcher("/WEB-INF/jsp/addsensor.jsp").forward(req, resp);
      } else {
        log.warning("Sensor already exists with id: " + sens.getId());
        req.setAttribute("message", "Sensor already exists with id: " + sens.getId());
        req.setAttribute("messageLevel", "danger");
        req.getRequestDispatcher("/WEB-INF/jsp/addsensor.jsp").forward(req, resp);
      }
    }
  }

}
