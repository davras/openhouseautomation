<%-- 
    Document   : testuser
    Created on : Jul 17, 2014, 12:12:32 PM
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
        <h1>Hello World!</h1>
        <%
        String currentuser="";
          if (request.getUserPrincipal() != null) {
            currentuser = request.getUserPrincipal().getName();
          }
        %>
        Hi&nbsp;
        <%= currentuser %>
    </body>
</html>
