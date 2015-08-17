/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.openhouseautomation.filters;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 *
 * @author dras
 */
public class AuthFilter implements Filter {

  private ServletContext context;

  @Override
  public void init(FilterConfig fConfig) throws ServletException {
    this.context = fConfig.getServletContext();
    this.context.log("AuthenticationFilter initialized");
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

    HttpServletRequest req = (HttpServletRequest) request;
    HttpServletResponse res = (HttpServletResponse) response;
    UserService userService = UserServiceFactory.getUserService();
    String thisURL = req.getRequestURI();
    res.setContentType("text/html");
    if (req.getUserPrincipal() == null) {
      res.getWriter().println("<p>Please <a href=\""
              + userService.createLoginURL(thisURL)
              + "\">sign in</a>.</p>");
    } else {
      // pass the request along the filter chain
      chain.doFilter(request, response);
    }

  }

  @Override
  public void destroy() {
    //close any resources here
  }

}
