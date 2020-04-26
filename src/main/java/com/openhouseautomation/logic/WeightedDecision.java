/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.openhouseautomation.logic;

import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import java.util.Comparator;
import java.util.PriorityQueue;

/**
 *
 * @author dras
 */
@Entity
@Cache
public class WeightedDecision {

  @Id
  public Long id; //id of the controller that used this WD.
  
  // store data
  PriorityQueue<DecisionElement> queue = new PriorityQueue<>(10, new WDsort());
  
  private class WDsort implements Comparator<DecisionElement> {

    @Override
    public int compare(DecisionElement one, DecisionElement two) {
      return one.getWeight() - two.getWeight();
    }
  }

    // why was this static?
  private class DecisionElement {
    
    private DecisionElement(String name, int weight, Object value) {
      this.name = name;
      this.weight = weight;
      this.value = value;
    }
    public String name;
    public int weight;
    public Object value;

    public int getWeight() {
      return this.weight;
    }
    public Object getValue() {
      return this.value;
    }
    public String getName() {
      return this.name;
    }
  }

  public void addElement(String name, int weight, Object value) {
    DecisionElement de = new DecisionElement(name, weight, value);
    queue.offer(de);
  }

  public Object getTopValue() {
    if (queue.isEmpty()) {
      return 0;
    }
    return queue.peek().getValue();
  }
  public String getTopName() {
    if (queue.isEmpty()) {
      return "OFFLINE";
    }
    return queue.peek().getName();
  }

  public String toJSONString() {
    String toret = "[";
    for (DecisionElement de: queue) {
      toret += "{\"name\": \"" + de.name + "\","
              + "\"weight\": \"" + de.weight + "\","
              + "\"value\": \"" + de.value + "\"},";
    }
    //remove last comma
    return toret.substring(0,toret.length()-1) + "]";
  }
  public String toMessage() {
    String toret = "";
    for (DecisionElement de: queue) {
      toret += de.name + ": " + de.value + " (Weight: " + de.weight + ")\n";
    }
    return toret;
  }
  public void setId(Long id) {
    this.id = id;
  }
}
