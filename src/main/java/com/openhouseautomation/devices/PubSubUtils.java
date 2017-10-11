/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.openhouseautomation.devices;

import com.google.common.io.BaseEncoding;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.openhouseautomation.model.Message;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author dave
 */
public class PubSubUtils {

  private static final Logger log = Logger.getLogger(PubSubUtils.class.getName());
  private static final Gson gson = new Gson();
  private static final JsonParser jsonParser = new JsonParser();

  public static Message getMessage(HttpServletRequest request) throws IOException {
    StringBuilder buffer = new StringBuilder();
    BufferedReader reader;
    String requestBody = "";
    reader = request.getReader();
    String line;
    while ((line = reader.readLine()) != null) {
      buffer.append(line);
    }
    requestBody = buffer.toString();
    JsonElement jsonRoot = jsonParser.parse(requestBody);
    String messageStr = jsonRoot.getAsJsonObject().get("message").toString();
    Message message = gson.fromJson(messageStr, Message.class);
    // decode from base64
    String decoded = new String(BaseEncoding.base64().decode(message.getData()));
    message.setData(decoded);
    return message;
  }
}
