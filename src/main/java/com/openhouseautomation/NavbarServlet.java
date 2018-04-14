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
      if (request.getUserPrincipal() != null) {
        out.println(getLogout(userService.createLogoutURL(request.getRequestURI().replace("navbar.html", ""))));
        return;
      }
      out.println(getLogin(userService.createLoginURL(request.getRequestURI().replace("navbar.html", ""))));
    }
  }

  protected String getLogout(String logoutURL) {
    String logoutpage = "      <div class=\"navbar navbar-inverse navbar-fixed-top\">"
            + "        <a class=\"pull-left\"><img src=\"/images/ic_home_white_24dp.png\"></a>"
            + "        <div class=\"navbar-header\">"
            + "          <button type=\"button\" class=\"navbar-toggle\" data-toggle=\"collapse\" data-target=\".navbar-collapse\">"
            + "            <span class=\"icon-bar\"></span>"
            + "            <span class=\"icon-bar\"></span>"
            + "            <span class=\"icon-bar\"></span>"
            + "          </button>"
            + "        </div>"
            + "        <div class=\"navbar-collapse collapse\">"
            + "          <ul class=\"nav navbar-nav\">"
            + "            <li><a href=\"/status.html\">Status</a></li>"
            + "            <li><a href=\"/control.html\">Control</a></li>"
            + "            <li><a href=\"/scenes.html\">Scenes</a></li>"
            + "            <li><a class=\"pull-left\" href=\"" + logoutURL + "\">Logout</a>"
            + "          </ul>"
            + "        </div><!--/.nav-collapse -->"
            + "      </div>";
    return logoutpage;
  }

  protected String getLogin(String loginURL) {
    // separated for readability
    String loginpage = "      <div class=\"navbar navbar-inverse navbar-fixed-top\">"
            + "        <a class=\"pull-left\" href=\"" + loginURL + "\">Login</a>"
            + "        <div class=\"navbar-header\">"
            + "          <button type=\"button\" class=\"navbar-toggle\" data-toggle=\"collapse\" data-target=\".navbar-collapse\">"
            + "            <span class=\"icon-bar\"></span>"
            + "            <span class=\"icon-bar\"></span>"
            + "            <span class=\"icon-bar\"></span>"
            + "          </button>"
            + "        </div>"
            + "        <div class=\"navbar-collapse collapse\">"
            + "          <ul class=\"nav navbar-nav\">"
            + "            <li><b><a href=\"" + loginURL + "\">Login</b></a>"
            + "          </ul>"
            + "        </div><!--/.nav-collapse -->"
            + "      </div>"
            + "    </div>"
            + "  </nav>";
    return loginpage;
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
