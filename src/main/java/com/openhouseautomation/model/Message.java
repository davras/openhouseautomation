/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.openhouseautomation.model;

import com.google.api.client.util.Strings;
import java.util.StringTokenizer;

/**
 *
 * @author dave
 */
public class Message {
  private String messageId;
  private String publishTime;
  private String data;
  private Long id;
  private String value;

  private Message() {}
  
  public Message(String messageId) {
    this.messageId = messageId;
  }

  public String getMessageId() {
    return messageId;
  }

  public void setMessageId(String messageId) {
    this.messageId = messageId;
  }

  public String getPublishTime() {
    return publishTime;
  }

  public void setPublishTime(String publishTime) {
    this.publishTime = publishTime;
  }

  public String getData() {
    return data;
  }

  public void setData(String data) {
    this.data = data;
  }
  public boolean parseData() {
    String ft, st = null;
    StringTokenizer st1 = new StringTokenizer(data, "/");
    if (st1.countTokens() != 2) {
      return false;
    }
    ft = (String)st1.nextToken();
    st = (String)st1.nextToken();
    if (Strings.isNullOrEmpty(ft) || Strings.isNullOrEmpty(st) ||
            ft.equals("test-event")) {
      return false;
    } else {
      id = Long.parseLong(ft);
      value = st;
    }
    return true;
  }
  public String getValue() {
    return value;
  }
  public Long getId() {
    return id;
  }
}
