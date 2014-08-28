package com.openhouseautomation;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Returns the time in seconds since epoch (Jan 1, 1970)
 * @author dras
 */
public class TimeServlet extends HttpServlet {

  private static final long serialVersionUID = 1L;

  /**
   * Gets the current time
   * @param req The HTTP request
   * @param resp The HTTP response
   * @throws IOException
   */
  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    resp.getWriter().println("gettime=" + System.currentTimeMillis() / 1000);
  }
}
