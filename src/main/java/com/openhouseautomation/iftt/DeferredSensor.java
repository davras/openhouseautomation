/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.openhouseautomation.iftt;

import com.google.appengine.api.taskqueue.DeferredTask;
import com.openhouseautomation.model.Sensor;

/**
 *
 * @author dave
 */
public abstract class DeferredSensor implements DeferredTask {
  Sensor oldsensor;
  Sensor newsensor;
  public void setOldSensor(Sensor c) {
    this.oldsensor = c;
  }
  public void setNewSensor(Sensor c) {
    this.newsensor = c;
  }
  public Sensor getOldSensor() {
    return this.oldsensor;
  }
  public Sensor getNewSensor() {
    return this.newsensor;
  }
}

