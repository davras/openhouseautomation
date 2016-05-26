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
  Sensor sensor;
  public void setSensor(Sensor c) {
    this.sensor = c;
  }
  public Sensor getSensor() {
    return this.sensor;
  }
}

