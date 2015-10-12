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
    if (userService.getCurrentUser() != null) {
      log.log(Level.INFO,"isAdmin()=" + userService.isUserAdmin());
    }
    log.log(Level.INFO, "getPathInfo()={0}", req.getPathInfo());
    log.log(Level.INFO, "getUserPrincipal()={0}", req.getUserPrincipal());
    log.log(Level.INFO, "source ip={0}", req.getRemoteAddr());
    // if source ip is home
    boolean approved = false;
    if ("50.194.29.173".equals(req.getRemoteAddr())) {
      log.log(Level.WARNING, "approved by source ip");
      approved = true;
    }
    //if ("/status/display/devices".equals(req.getPathInfo())) {
      // needs a user logged in
      if (req.getUserPrincipal() != null) {
        log.log(Level.INFO, "approved by user principle");
        approved=true;
        // TODO put in user auth
      }
    //}
    
    
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
