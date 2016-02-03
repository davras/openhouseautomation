/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.openhouseautomation;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author dave
 */
public class LoginServlet extends HttpServlet {

  /**
   * Handles the HTTP <code>GET</code> method.
   *
   * @param request servlet request
   * @param response servlet response
   * @throws ServletException if a servlet-specific error occurs
   * @throws IOException if an I/O error occurs
   */
  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
          throws ServletException, IOException {
    response.setContentType("text/html;charset=UTF-8");
    try (PrintWriter out = response.getWriter()) {
      UserService userService = UserServiceFactory.getUserService();
      response.setContentType("text/html");
      if (request.getUserPrincipal() != null) {
        out.print("<li class=\"navbar-brand\">" + request.getUserPrincipal().getName() + "</li>");
        out.println("<li class=\"navbar-brand\"><a href=\"" + userService.createLogoutURL(request.getRequestURI().replace("login.html", "")) + "\">Logout</a></li>");
      } else {
        out.println("<li class=\"navbar-brand\"><a href=\"" + userService.createLoginURL(request.getRequestURI().replace("login.html", "")) + "\">Login</a></li>");
      }
      out.println("<li class=\"navbar-brand\"><a href=\"https://code.google.com/p/gautoard/wiki/DesignConcepts\">Help</a></li>");
    }
  }

  /**
   * Handles the HTTP <code>POST</code> method.
   *
   * @param request servlet request
   * @param response servlet response
   * @throws ServletException if a servlet-specific error occurs
   * @throws IOException if an I/O error occurs
   */
  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
          throws ServletException, IOException {

  }

  /**
   * Returns a short description of the servlet.
   *
   * @return a String containing servlet description
   */
  @Override
  public String getServletInfo() {
    return "Short description";
  }

}
