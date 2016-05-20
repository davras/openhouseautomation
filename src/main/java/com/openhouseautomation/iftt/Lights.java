/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.openhouseautomation.iftt;

import com.openhouseautomation.model.DatastoreConfig;
import com.openhouseautomation.notification.NotificationHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author dras
 */
public class Lights extends DeferredController {

  private static final Logger log = Logger.getLogger(Lights.class.getName());

  @Override
  public void run() {
    log.log(Level.INFO, "checking:" + super.controller.getId()
     + "/" + super.controller.getName()
            + "=" + super.controller.getActualState()
            + ", was: " + super.controller.getPreviousState()
            + "\n vs. desired: " + super.controller.getDesiredState() 
            + "\n last actual change:" + super.controller.lastactualstatechange
            + "\n last desired change:" + super.controller.lastdesiredstatechange
            + "\n last controller contact:" + super.controller.lastcontactdate);
    if (super.controller.getActualState().equals(super.controller.getPreviousState())) {
      // no change, ignore
      return;
    }
    if (super.controller.getId() == 91125605L) {
      NotificationHandler nhnotif = new NotificationHandler();
      nhnotif.setRecipient(DatastoreConfig.getValueForKey("admin"));
      nhnotif.setSubject("Charger status");
      nhnotif.setBody("Charger status change: " + super.controller.getPreviousState()+ " -> " + super.controller.getActualState());
      nhnotif.alwaysSend();
    }
  }
}
