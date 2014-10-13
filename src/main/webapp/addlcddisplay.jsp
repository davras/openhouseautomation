
<%@page import="com.openhouseautomation.model.LCDDisplay"%>
<%@page import="static com.openhouseautomation.OfyService.ofy"%>
<%-- 
    Document   : addlcddisplay
    Created on : Aug 31, 2014, 8:31:37 PM
    Author     : dave
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
    LCDDisplay lcdd = new LCDDisplay();
    String errorz = "";
    long snaptime = System.currentTimeMillis(); // page timing
    // has the form been submitted with a setpoint change?
    boolean formvalid = ((request.getParameter("displayname") != null) && (!"".equals(request.getParameter("displayname"))));

    if (formvalid) {
        lcdd.setDisplayName(request.getParameter("displayname"));
        lcdd.setDisplayString(request.getParameter("displaystring"));
        ofy().save().entity(lcdd).now();
    }
    
%>
<!DOCTYPE html>
<html lang="en">
    <head>
        <meta charset="utf-8">
        <meta http-equiv="X-UA-Compatible" content="IE=edge">
        <meta name="viewport" content="width=device-width, initial-scale=1">

        <title>gAutoArd : Create LCD Display</title>

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
                        <li class="active">Overview</li>
                        <li class="divider"></li>
                        <li class="nav-header">Sensors</li>
                        <li><a href="/addsensor">Add Sensor</a></li>
                        <li class="divider"></li>
                        <li class="nav-header">Controllers</li>
                        <li><a href="/addcontroller">Add Controller</a></li>
                        <li class="divider"></li>
                        <li class="nav-header">Readings</li>
                        <li><a href="/status.jsp">Current</a></li>
                        <li><a href="/charts/weekly.html">Weekly</a></li>
                        <li><a href="/charts/archived.html">Archived</a></li>
                    </ul>
                </div>
                <div class="col-sm-9 col-sm-offset-3 col-md-10 col-md-offset-2 main">
                    <h1>Add LCD Display</h1>
                    <% if (formvalid) {
                            if (errorz.length() > 0) {
                                out.println(errorz);
                  } else {%>
                    <h3>Saved</h3>
                    <% }
          } else {%>
                    <h3>Please provide the following information:</h3>
                    <form action='addlcddisplay.jsp' id="usrform">
                        <table>
                            <tr><td>Display Name: </td><td><input type='number' name='displayname' value=""/></td><td><i>The name to use in the LCDDisplay servlet k= param to generate the return display data</i></td></tr>
                            <tr><td>Display String: </td><td><textarea rows="3" cols="20" name="displaystring" form="usrform"></textarea></td><td><i>The encoded string to display on the LCD</i></td></tr>
                        </table>
                        <p><input type='submit' value='Create LCD Display'/>
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
