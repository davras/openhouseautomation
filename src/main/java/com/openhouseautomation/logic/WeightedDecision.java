/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.openhouseautomation.logic;

import java.util.Comparator;
import java.util.PriorityQueue;

/**
 *
 * @author dras
 */
public class WeightedDecision {

  // store data
  PriorityQueue<DecisionElement> queue = new PriorityQueue<>();

  static class WDsort implements Comparator<DecisionElement> {

    @Override
    public int compare(DecisionElement one, DecisionElement two) {
      return two.getWeight() - one.getWeight();
    }
  }

  static class DecisionElement {

    public DecisionElement(String name, int weight, Object value) {
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
  }

  public void addElement(String name, int weight, Object value) {
    DecisionElement de = new DecisionElement(name, weight, value);
    queue.offer(de);
  }
  
  public Object getTopValue() {
    return queue.peek().getValue();
  }
  @Override
  public String toString() {
    String toret = "{";
    for (DecisionElement de: queue) {
      toret = "[" + de.name + ", " + de.weight + ", " + de.value + "],";
    }
    //remove last comma
    toret = toret.substring(0,toret.length()-1);
    return toret + "}";
  }
  public String toMessage() {
    String toret = "";
    for (DecisionElement de: queue) {
      toret += de.name + ": " + de.value + " (Weight: " + de.weight + ")\n";
    }
    return toret;
  }
}
