package com.openhouseautomation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 
 * @author dras
 */
public class DatastoreImport extends HttpServlet {

  private static final long serialVersionUID = 1L;

  /**
   * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
   * 
   * @param request servlet request
   * @param response servlet response
   * @throws ServletException if a servlet-specific error occurs
   * @throws IOException if an I/O error occurs
   */
  protected void processRequest(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    response.setContentType("text/html;charset=UTF-8");
    PrintWriter out = response.getWriter();
    try {
      out.println("<html><head><title>Servlet DatastoreImport</title></head><body>");
      Enumeration enParams = request.getParameterNames();
      out.println("<pre>");
      // while (enParams.hasMoreElements()) {
      // String paramName = (String) enParams.nextElement();
      // out.println("Attribute Name - " + paramName + ", Value - " +
      // request.getParameter(paramName));
      // }
      out.println("</pre>");
      if (null == request.getParameter("databox")) {
        // show the upload page
        out.println("<form action=\"datastoreimport\" method=post>"
            + "<textarea rows=80 cols=80 name=\"databox\" value=\"\" wrap=physical/></textarea>"
            + "<input type=\"submit\" value=\"Submit\">" + "</form>");
        out.println("</body></html");
      } else {
        int lines = 0;
        BufferedReader bread =
            new BufferedReader(new StringReader(request.getParameter("databox")));
        String line = null;
        while ((line = bread.readLine()) != null) {
//          EventBean eb = new EventBean(line);
//          eb.log();
//          lines++;
        }

        // import the uploaded data
        out.println("<!DOCTYPE html>");
        out.println("<html>");
        out.println("<head>");
        out.println("<title>Servlet DatastoreImport</title>");
        out.println("</head>");
        out.println("<body>");
        out.println("<h1>" + lines + " lines loaded</h1>");
        out.println("</body>");
        out.println("</html>");
      }
    } finally {
      out.close();
    }
  }

  // <editor-fold defaultstate="collapsed"
  // desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
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
    processRequest(request, response);
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
    processRequest(request, response);
  }

  /**
   * Returns a short description of the servlet.
   * 
   * @return a String containing servlet description
   */
  @Override
  public String getServletInfo() {
    return "Short description";
  }// </editor-fold>
}
