<%-- 
    Document   : addsensor
    Created on : Jun 23, 2014, 1:23:39 PM
    Author     : dras
--%>
<%@page import="java.util.zip.CRC32"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@page import="static com.openhouseautomation.OfyService.ofy"%>
<%@page import="java.util.Date"%>
<%@page import="com.openhouseautomation.model.Sensor"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
  Sensor sens = new Sensor();
  String salt = "abc123";
  String errorz = "";
  long snaptime = System.currentTimeMillis(); // page timing
  // has the form been submitted with a setpoint change?
  boolean formvalid = ((request.getParameter("owner") != null) && (!"".equals(request.getParameter("owner"))));

  if (formvalid) {
    sens.setOwner(request.getParameter("owner"));
    sens.setLocation(request.getParameter("location"));
    sens.setZone(request.getParameter("zone"));
    sens.setType(Sensor.Type.TEMPERATURE);
    sens.setName(request.getParameter("name"));
    sens.setUnit("F");
    sens.setLastReading("99");
    sens.setLastReadingDate(new Date());
    CRC32 hash = new CRC32();
    hash.update(salt.getBytes());
    hash.update(sens.getOwner().getBytes());
    hash.update(sens.getLocation().getBytes());
    hash.update(sens.getZone().getBytes());
    sens.setId(hash.getValue());
    Sensor sensexists = ofy().load().type(Sensor.class).id(sens.getId()).now();
    if (sensexists == null) {
      ofy().save().entity(sens).now();
    } else {
      errorz = "Sensor already exists with id: " + sens.getId();
    }
  }
%>
<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">

    <title>gAutoArd</title>

    <!-- Bootstrap core CSS -->
    <link href="/css/bootstrap.min.css" rel="stylesheet">

    <!-- gAutoArd core CSS -->
    <link href="/css/main.css" rel="stylesheet">

    <link rel="stylesheet" href="/css/jquery-ui.css">
    <!-- HTML5 shim and Respond.js IE8 support of HTML5 elements and media queries -->
    <!--[if lt IE 9]>
      <script src="https://oss.maxcdn.com/libs/html5shiv/3.7.0/html5shiv.js"></script>
      <script src="https://oss.maxcdn.com/libs/respond.js/1.4.2/respond.min.js"></script>
    <![endif]-->
    <style>
      #slider { margin: 10px; }
    </style>
  </head>
  <body>
    <div class="navbar navbar-inverse navbar-fixed-top" role="navigation">
      <div class="container-fluid">
        <div class="navbar-header">
          <button type="button" class="navbar-toggle" data-toggle="collapse" data-target=".navbar-collapse">
            <span class="sr-only">Toggle navigation</span>
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
          </button>
          <a class="navbar-brand" href="/">gAutoArd</a>
        </div>
        <div class="navbar-collapse collapse">
          <ul class="nav navbar-nav navbar-right">
            <li><a href="https://code.google.com/p/gautoard/wiki/DesignConcepts">Help</a></li>
          </ul>
        </div>
      </div>
    </div>

    <div class="container-fluid">
      <div class="row">
        <div class="col-sm-3 col-md-2 sidebar">
          <ul class="nav nav-sidebar">
            <li><a href="/">Overview</a></li>
            <li class="divider"></li>
            <li class="nav-header">Devices</li>
            <li class="active"><a href="/thermostat.jsp">Thermostat</a></li>
            <li><a href="/lights.jsp">Lights</a></li>
            <li><a href="/alarm.jsp">Alarm</a></li>
            <li><a href="/charts/temperature.jsp">Weekly Temperature Chart</a></li>
            <li><a href="/charts/temperature_archived.jsp">Archive Temperature Chart</a></li>
          </ul>
        </div>
        <div class="col-sm-9 col-sm-offset-3 col-md-10 col-md-offset-2 main">
          <h1>Add Sensor</h1>
          <% if (formvalid) {
              if (errorz.length() > 0) {
                out.println(errorz);
              } else {%>
          <h3>Your new sensor: (use the ID <%= sens.getId()%> in your Arduino or Spark)</h3>
          <% }
          } else {%>
          <h3>Please provide the following information:</h3>
          <form action='addsensor.jsp'>
            <table>
              <tr><td>Owner: </td><td><input type='text' name='owner' value="<%= request.getUserPrincipal().getName()%>"/></td><td><i>Your username</i></td></tr>
              <tr><td>Location: </td><td><input type='text' name='location' /></td><td><i>home, work, etc.</i></td></tr>
              <tr><td>Zone: </td><td><input type='text' name='zone' /></td><td><i>downstairs, outside, garage, etc.</i></td></tr>
              <tr><td>Name: </td><td><input type='text' name='name' /></td><td><i>What to display on a webpage, like "Outside Temperature"</i></td></tr>
            </table>
            <p><input type='submit' value='Create Sensor'/>
          </form>
          <% }%>
          <p>Page Generation: <%= (System.currentTimeMillis() - snaptime)%> ms</p>
        </div>
      </div>
    </div>

    <!-- jQuery (necessary for Bootstrap's JavaScript plugins) -->
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.0/jquery.min.js"></script>
    <!-- Include all compiled plugins (below), or include individual files as needed -->
    <script src="/js/bootstrap.min.js"></script>
    <script src="/js/jquery-ui.js"></script>

  </body>
</html>
