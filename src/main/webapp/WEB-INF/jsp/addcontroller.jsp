<%@page import="com.openhouseautomation.model.Controller"%>
<%@page import="java.util.zip.CRC32"%>
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
            <li class="nav-header">Sensors</li>
            <li><a href="/addsensor">Add Sensor</a></li>
            <li class="divider"></li>
            <li class="nav-header">Controllers</li>
            <li class=""active"><a href="/addcontroller">Add Controller</a></li>
            <li class="divider"></li>
            <li class="nav-header">Readings</li>
            <li><a href="/status.jsp">Current</a></li>
            <li><a href="/charts/weekly.html">Weekly</a></li>
            <li><a href="/charts/archived.html">Archived</a></li>
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
          <h1>Add Controller</h1>
          <p class="lead">Please provide the following information:</p>
          <form action='/addcontroller' method="post" class="form-horizontal" role="form">
            <div class="form-group">
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
                  <% for (Controller.Type t : Controller.Type.values()) { %>
                  <option value="<%=t%>"><%=t%></option>
                  <% } %>
                </select>
              </div>
            </div>
            <div class="form-group">
              <label for="name" class="col-sm-2 col-md-1 control-label">Name</label>
              <div class="col-sm-4 col-md-5">
                <input type="text" class="form-control" id="name" name="name" placeholder="What to display on a webpage, like 'House Fan'">
              </div>
            </div>
            
            <div class="form-group">
              <div class="col-sm-offset-2 col-sm-7 col-md-offset-1 col-md-9">
                <button type="submit" class="btn btn-default">Create Controller</button>
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
