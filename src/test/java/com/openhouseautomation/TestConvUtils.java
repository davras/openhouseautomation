/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.openhouseautomation;

import org.junit.After;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author dras
 */
public class TestConvUtils {

  public TestConvUtils() {
  }

  @Before
  public void setUp() {
  }

  @After
  public void tearDown() {
  }

  @Test
  public void testSetup() {
  }

  @Test
  public void testTimeAgoToString() {
    long secs = System.currentTimeMillis() / 1000 - 30; // 30 secs ago
    assertTrue(Convutils.timeAgoToString(secs).equals("30 seconds ago"));
    secs = System.currentTimeMillis() / 1000 - 30 * 60; // 30 mins ago
    assertTrue(Convutils.timeAgoToString(secs).equals("30 minutes ago"));
  }

  @Test
  public void testTitleCase() {
    System.out.println(Convutils.toTitleCase("TEMPERATURE"));
    assertTrue(Convutils.toTitleCase("TEMPERATURE").equals("Temperature"));
    assertTrue(Convutils.toTitleCase("temperature").equals("Temperature"));
    assertTrue(Convutils.toTitleCase("Temperature").equals("Temperature"));
  }
}
