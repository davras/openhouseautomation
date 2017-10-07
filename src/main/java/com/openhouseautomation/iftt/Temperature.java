/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.openhouseautomation.iftt;

import static com.openhouseautomation.OfyService.ofy;
import com.openhouseautomation.logic.HouseFan;
import com.openhouseautomation.model.Controller;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.HttpStatus;

/**
 *
 * @author dras
 */
public class Temperature extends DeferredSensor {

  public static final Logger log = Logger.getLogger(Temperature.class.getName());

  @Override
  public void run() {
    Float fold = null, fnew = null;
    try {
      fold = Float.parseFloat(super.sensor.getPreviousReading());
      fnew = Float.parseFloat(super.sensor.getLastReading());
    } catch (NumberFormatException e) {
    }
    if (Objects.equals(fold, fnew)) {
      return;
    }

    if (sensor.getName().equals("Outside Temperature")
            || sensor.getName().equals("Inside Temperature")) {
      HouseFan hf = new HouseFan();
      hf.process();
      // this part sucks
      // have to reload the controller, put in the decision, save it again
      // TODO find a better way
      // problem is that the hf.process() can bail out early, which saves DS reads
      // but causes one extra write
      Controller controller = ofy().load().type(Controller.class).filter("name", "Whole House Fan").first().now();
      if (controller == null) {
        log.log(Level.SEVERE, "Controller not found: Whole House Fan");
        return;
      }
      controller.setDecision(hf.getWeightedDecision().toJSONString());
      ofy().save().entity(controller).now();
      // end suckage
      log.log(Level.INFO, "Temperature.WHF.Decision:" + hf.getWeightedDecision().toMessage());
    }
  }
}
