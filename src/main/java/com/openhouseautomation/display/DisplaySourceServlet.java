/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.openhouseautomation.display;

import com.google.appengine.api.datastore.QueryResultIterator;
import com.googlecode.objectify.cmd.Query;
import static com.openhouseautomation.OfyService.ofy;
import com.openhouseautomation.model.Sensor;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;

@Path("/display")
public class DisplaySourceServlet extends Application {
  private static final long serialVersionUID = 1L;
  private static final Logger log = Logger.getLogger(DisplaySourceServlet.class.getName());
  @GET
  //@Produces(MediaType.APPLICATION_JSON)
  @Produces(MediaType.TEXT_PLAIN)
  public String getSensors() {
    log.log(Level.INFO, "getSensors()");
    return "this will probably never work";
    
    /* 
    Query<Sensor> query = ofy().load().type(Sensor.class);
    QueryResultIterator<Sensor> iterator = query.iterator();
    while (iterator.hasNext()) {
      Sensor sens = (Sensor) iterator.next();
      sensors.add(sens);
    }
    return sensors;
        */
  }
}
