package com.openhouseautomation.iftt;

import com.google.appengine.api.taskqueue.DeferredTask;
import com.openhouseautomation.model.Controller;

/**
 *
 * @author dave
 */
public abstract class DeferredController implements DeferredTask {
  Controller controller;
  public void setController(Controller c) {
    this.controller = c;
  }
  public Controller getController() {
    return this.controller;
  }
}
