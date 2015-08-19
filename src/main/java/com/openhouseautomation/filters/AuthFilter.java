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
import javax.servlet.ServletContext;
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
  private ServletContext context;

  @Override
  public void init(FilterConfig fConfig) throws ServletException {
    this.context = fConfig.getServletContext();
    log.log(Level.INFO, "AuthenticationFilter initialized");
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
    HttpServletRequest req = (HttpServletRequest) request;
    HttpServletResponse res = (HttpServletResponse) response;
    UserService userService = UserServiceFactory.getUserService();
    String thisURL = req.getRequestURI();
    // if the user is logged in, populate username
    log.log(Level.INFO, "getPathInfo()={0}", req.getPathInfo());
    if (req.getUserPrincipal() == null) {
      if ("/login".equals(req.getPathInfo())) {
        res.getWriter().print("[{\"redirecturl\":\"" + UserServiceFactory.getUserService().createLoginURL(req.getPathInfo()) + "\"}]");
      } else {
        res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "[\"Unauthorized\"]");
      }
    } else {
      // pass the request along the filter chain
      this.context.log("Auth: " + userService.getCurrentUser() + ": isAdmin()=" + userService.isUserAdmin());
      chain.doFilter(request, response);
    }

  }

  @Override
  public void destroy() {
    //close any resources here
  }

}
