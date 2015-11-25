/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.openhouseautomation.filters;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author dras
 */
public class AuthFilter implements Filter {

  private static final Logger log = Logger.getLogger(AuthFilter.class.getName());

  @Override
  public void init(FilterConfig fConfig) throws ServletException {
    log.log(Level.INFO, "AuthenticationFilter initialized");
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
    HttpServletRequest req = (HttpServletRequest) request;
    HttpServletResponse res = (HttpServletResponse) response;
    UserService userService = UserServiceFactory.getUserService();
    String thisURL = req.getRequestURI();
    // if the user is logged in, populate username
    log.log(Level.INFO, "Auth: " + userService.getCurrentUser());
    if (userService.isUserLoggedIn()) {
      log.log(Level.INFO, "isAdmin()=" + userService.isUserAdmin());
    } else {
      log.log(Level.INFO, "not an Admin: ");
    }
    log.log(Level.INFO, "getPathInfo()={0}", req.getPathInfo());
    log.log(Level.INFO, "getUserPrincipal()={0}", req.getUserPrincipal());
    log.log(Level.INFO, "source ip={0}", req.getRemoteAddr());
    
    boolean approved = false;
    if (req.getUserPrincipal() != null) {
      //if ("/status/display/devices".equals(req.getPathInfo())) {
      // needs a user logged in
      log.log(Level.INFO, "approved by user principle");
      approved = userService.isUserAdmin();
      // you have to be an admin to control the house
    }
    if (!approved && "50.194.29.173".equals(req.getRemoteAddr())) {
      // or be in the house (source ip is home)
      log.log(Level.INFO, "approved by source ip");
      approved = true;
    }

    // pass the request along the filter chain
    if (approved) {
      chain.doFilter(request, response);
    } else {
      res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
    }
  }

  @Override
  public void destroy() {
    //close any resources here
  }

}
