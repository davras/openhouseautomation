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
public class NavbarServlet extends HttpServlet {

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
      out.println(
              "<nav class=\"navbar navbar-default navbar-inverse\">\n"
              + "  <div class=\"container-fluid\">\n"
              + "<!--ver 30d-->"
              + "    <!-- Brand and toggle get grouped for better mobile display -->\n"
              + "    <div class=\"navbar-header\">\n"
              + "      <button type=\"button\" class=\"navbar-toggle collapsed\" data-toggle=\"collapse\" data-target=\"#oha-navbar-collapse-1\" aria-expanded=\"false\">\n"
              + "        <span class=\"sr-only\">Toggle navigation</span>\n"
              + "        <span class=\"icon-bar\"></span>\n"
              + "        <span class=\"icon-bar\"></span>\n"
              + "        <span class=\"icon-bar\"></span>\n"
              + "      </button>\n"
      );
      if (request.getUserPrincipal() != null) {
        //out.println("<a class=\"navbar-brand\" href=\"/\">" + request.getUserPrincipal().getName() + "</a>");
        out.println("<a href=\"/\"><img src=\"/images/ic_home_white_24dp.png\" width=\"36\" height=\"36\"/></a>");
      } else {
        out.print("<a class=\"navbar-brand\" href=\"" + userService.createLoginURL(request.getRequestURI().replace("navbar.html", "")) + "\">Login</a>");
      }
      out.println(
              "    </div>\n"
              + "    <!-- Collect the nav links, forms, and other content for toggling -->\n"
              + "    <div class=\"collapse navbar-collapse\" id=\"oha-navbar-collapse-1\">\n"
              + "      <ul class=\"nav navbar-nav\">\n"
      );
      if (request.getUserPrincipal() != null) {
        out.println("<nav class=\"navbar\"><a class=\"navbar-brand\" href=\"" + userService.createLogoutURL(request.getRequestURI().replace("navbar.html", "")) + "\">Logout</a>");
      }
      out.println(
              "      </nav>\n"
              + "    </div><!-- /.navbar-collapse -->\n"
              + "  </div><!-- /.container-fluid -->\n"
              + "</nav>");
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
