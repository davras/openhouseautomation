
<%@page import="static com.openhouseautomation.OfyService.ofy()"%>
<%@page import="com.openhouseautomation.model.Controller"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page isELIgnored="false" %>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">

    <title>Open House Automation</title>

    <!-- Bootstrap core CSS -->
    <link href="/css/bootstrap.min.css" rel="stylesheet">

    <!-- gAutoArd core CSS -->
    <link href="/css/main.css" rel="stylesheet">

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
          <a class="navbar-brand" href="/">Open House Automation</a>
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
            <li>Overview</li>
            <li class="divider"></li>
            <li><a href="/status.jsp">Current</a></li>
            <li><a href="/charts/weekly.html">Weekly</a></li>
            <li><a href="/charts/archived.html">Archived</a></li>
            <li class="divider"></li>
            <li class="nav-header">Control</li>
            <li><a href="/controller/lights">Lights</a></li>
            <li><a href="/controller/fan">Fan</a></li>
            <li><a href="/controller/thermostat">Thermostat</a></li>
            <li class="divider"></li>
            <li class="nav-header">Scenes</li>
            <li><a href="/scenes/wakeup">Wake Up</a></li>
            <li><a href="/scenes/leave">Leave</a></li>
            <li><a href="/scenes/gethome">Get Home</a></li>
            <li><a href="/scenes/watchamovie">Watch a Movie</a></li>
            <li><a href="/scenes/gotobed">Go to Bed</a></li>
            <li class="divider"></li>
            <li class="nav-header">Manage</li>
            <li><a href="/addcontroller">Add Controller</a></li>
            <li class="active"><a href="/addsensor">Add Sensor</a></li>
            <li><a href="/addlcddisplay.jsp">Add LCD Display</a></li>
          </ul>
        </div>
        <div class="col-sm-9 col-sm-offset-3 col-md-10 col-md-offset-2 main">
          <c:choose>
            <c:when test="${message != null}">
              <div class="alert alert-${messageLevel} alert-dismissible" role="alert">
                <button type="button" class="close" data-dismiss="alert"><span aria-hidden="true">&times;</span><span class="sr-only">Close</span></button>
                ${message}
              </div>
            </c:when>
          </c:choose>
          <h1>Fan</h1>
          <form action='/controller/fan' method="post" class="form-horizontal" role="form">
            <div class="form-group">
              <ul class="nav nav-pills">
                <% Controller controller = ofy().load().type(Controller.class).id(new Long(4280019022)).now();
                String[] states = { "OFF", "1", "2", "3", "4", "5" };
                if (controller.getDesiredStatePriority() == Controller.DesiredStatePriority.AUTO) {
                  // if in AUTO, show the manual buttons, highlighting the current state
                  for (int i=0; i < states.length; i++) {
                    out.print("<li");
                    if (controller.getActualState().equals(states[i])) {
                      out.print("class=\\\"active\\\"");
                    }
                    out.print("><a href=\\\"/controller/fan&");
                  }
                  out.print("<li");
                  %>
                <li class="active"><a href="#">Home</a></li>
                <li><a href="#">Profile</a></li>
                <li><a href="#">Messages</a></li>
              </ul>
              <label for="owner" class="col-sm-2 col-md-1 control-label">Owner</label>
              <div class="col-sm-4 col-md-5">
                <input type="text" class="form-control" id="owner" name="owner" placeholder="Your username">
              </div>
            </div>

            <div class="form-group">
              <label for="location" class="col-sm-2 col-md-1 control-label">Location</label>
              <div class="col-sm-4 col-md-5">
                <input type="text" class="form-control" id="location" name="location" placeholder="home, work, etc..">
              </div>
            </div>

            <div class="form-group">
              <label for="zone" class="col-sm-2 col-md-1 control-label">Zone</label>
              <div class="col-sm-4 col-md-5">
                <input type="text" class="form-control" id="zone" name="zone" placeholder="Your downstairs, outside, garage, etc...">
              </div>
            </div>

            <div class="form-group">
              <label for="type" class="col-sm-2 col-md-1 control-label">Type</label>
              <div class="sol-sm-4 col-md-5">
                <select name="type">
                  <% for (Type t : Sensor.Type.values()) {%>
                  <option value="<%=t%>"><%=t%></option>
                  <% }%>
                </select>
              </div>
            </div>


            <div class="form-group">
              <label for="name" class="col-sm-2 col-md-1 control-label">Name</label>
              <div class="col-sm-4 col-md-5">
                <input type="text" class="form-control" id="name" name="name" placeholder="What to display on a webpage, like 'Outside Temperature'">
              </div>
            </div>

            <div class="form-group">
              <div class="col-sm-offset-2 col-sm-7 col-md-offset-1 col-md-9">
                <button type="submit" class="btn btn-default">Create Sensor</button>
              </div>
            </div>
          </form>
        </div>
      </div>
    </div>

    <!-- jQuery (necessary for Bootstrap's JavaScript plugins) -->
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.0/jquery.min.js"></script>
    <!-- Include all compiled plugins (below), or include individual files as needed -->
    <script src="/js/bootstrap.min.js"></script>
  </body>
</html>
