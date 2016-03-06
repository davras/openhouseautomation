/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.openhouseautomation.iftt;

import com.google.appengine.api.taskqueue.DeferredTask;
import com.openhouseautomation.model.Controller;

/**
 *
 * @author dave
 */
public abstract class DeferredController implements DeferredTask {
  Controller oldcontroller;
  Controller newcontroller;
  public void setOldController(Controller c) {
    this.oldcontroller = c;
  }
  public void setNewController(Controller c) {
    this.newcontroller = c;
  }
  public Controller getOldController() {
    return this.oldcontroller;
  }
  public Controller getNewController() {
    return this.newcontroller;
  }
}
