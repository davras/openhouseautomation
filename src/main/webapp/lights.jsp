<%-- 
    Document   : lights
    Created on : Mar 19, 2014, 3:19:00 PM
    Author     : dras
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>JSP Page</title>
  </head>
  <body>
    <form action="lights.jsp">
      <% String favcolor = "#000000";
        if (null != request.getParameter("favcolor")) {
          favcolor = request.getParameter("favcolor");
        }
        if (null != request.getParameter("off")) {
          favcolor = "#000000";
        }
      %>
      <input type="color" value="<%= favcolor%>" name="favcolor"><br>
      <button name="off" value="Off" type="submit">
        Turn off</button><br>
      <input type="submit" name="submit">
    </form>
  </body>
</html>
