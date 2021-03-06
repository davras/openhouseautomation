package com.openhouseautomation.display;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.appengine.api.datastore.QueryResultIterator;
import com.googlecode.objectify.cmd.Query;
import com.openhouseautomation.Convutils;
import static com.openhouseautomation.OfyService.ofy;
import com.openhouseautomation.cron.HouseTimers;
import com.openhouseautomation.model.Controller;
import com.openhouseautomation.model.ControllerHelper;
import com.openhouseautomation.model.EventLog;
import com.openhouseautomation.model.Forecast;
import com.openhouseautomation.model.NotificationLog;
import com.openhouseautomation.model.Scene;
import com.openhouseautomation.model.SceneController;
import com.openhouseautomation.model.Sensor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author dave
 */
public class DisplaySourceServlet extends HttpServlet {

  private static final long serialVersionUID = 1L;
  private static final Logger log = Logger.getLogger(DisplaySourceServlet.class.getName());

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
    log.log(Level.INFO, request.getMethod() + " " + request.getPathInfo());
    if (request.getPathInfo() == null) {
      return;
    }
    if (request.getPathInfo().startsWith("/display/sensors")) {
      new ObjectMapper().writeValue(response.getWriter(), ofy().load().type(Sensor.class).list());
      return;
    }
    if (request.getPathInfo().startsWith("/display/forecast")) {
      new ObjectMapper().writeValue(response.getWriter(), ofy().load().type(Forecast.class).list());
      return;
    }
    if (request.getPathInfo().startsWith("/display/devices")) {
      doDisplayControllers(request, response);
      return;
    }
    if (request.getPathInfo().startsWith("/display/alerts")) {
      doDisplayAlerts(request, response);
      return;
    }
    if (request.getPathInfo().startsWith("/display/notifications")) {
      new ObjectMapper().writeValue(response.getWriter(), ofy().load().type(NotificationLog.class).order("-lastnotification").list());
      return;
    }
    if (request.getPathInfo().startsWith("/devicetypelist")) {
      doListDeviceTypes(request, response);
      return;
    }
    if (request.getPathInfo().startsWith("/display/scenes")) {
      doDisplayScenes(request, response);
      return;
    }
    log.log(Level.WARNING, "unsupported path: " + request.getPathInfo());
    response.sendError(HttpStatus.SC_NOT_FOUND, "path not supported");
  }

  private void doListDeviceTypes(HttpServletRequest request, HttpServletResponse response) throws IOException {
    PrintWriter out = response.getWriter();
    if (request.getRemoteAddr().equals("127.0.0.1")) {
      response.getWriter().print(testDeviceTypes);
      return;
    }
    // create the array of ControllerHelpers that will have display enabled if the controller is currently configured
    Controller.Type[] types = Controller.Type.values();
    ControllerHelper ch[] = new ControllerHelper[types.length];
    for (int i = 0; i < types.length; i++) {
      ch[i] = new ControllerHelper();
      ch[i].ordinal = types[i].ordinal();
      ch[i].name = types[i].toString();
      ch[i].display = false;
      ch[i].link = types[i].name();
    }
    // doesn't change frequently, no need to clear ofy() session cache
    // if the controller type is configured, flip the display flag on the type so it shows on the web page via Angular
    Query<Controller> q = ofy().load().type(Controller.class).project("type").distinct(true);
    QueryResultIterator<Controller> iterator = q.iterator();
    while (iterator.hasNext()) {
      Controller c = iterator.next();
      int ord = c.getType().ordinal();
      ch[ord].display = true;
    }
    ObjectMapper om = new ObjectMapper();
    om.writeValue(out, ch);
  }

   private void doDisplayScenes(HttpServletRequest request, HttpServletResponse response) throws IOException {
    PrintWriter out = response.getWriter();

    // dev on localhost
    if (request.getRemoteAddr().equals("127.0.0.1")) {
      response.getWriter().print(testControllerString);
      return;
    }

    if (request.getPathInfo().startsWith("/display/scenes/initialize")) {
      // make a new scene based on current controller settings
      Scene initscene = new Scene();
      initscene.setName("Example Scene " + (int) (Math.random() * 100));
      Query<Controller> query = ofy().cache(false).load().type(Controller.class).chunkAll();
      QueryResultIterator<Controller> iterator = query.iterator();
      List<SceneController> controllers = new ArrayList();
      while (iterator.hasNext()) {
        Controller cont = (Controller) iterator.next();
        SceneController savecont = new SceneController();
        savecont.setId(cont.getId());
        savecont.setDesiredState(cont.getDesiredState());
        savecont.setName(cont.getName());
        controllers.add(savecont);
      }
      ObjectMapper om = new ObjectMapper();
      StringWriter swout = new StringWriter();
      om.writeValue(swout, controllers);
      initscene.setConfig(swout.toString());
      ofy().save().entity(initscene).now();
    }

    new ObjectMapper().writeValue(out, ofy().load().type(Scene.class).list());
  }

  private void doDisplayControllers(HttpServletRequest request, HttpServletResponse response) throws IOException {
    PrintWriter out = response.getWriter();
    // dev on localhost
    if (request.getRemoteAddr().equals("127.0.0.1")) {
      response.getWriter().print(testControllerString);
      return;
    }
    // production
    if (com.openhouseautomation.Flags.clearCache) {
      ofy().clear(); // clear the session cache, not the memcache
    }
    String type = request.getParameter("type");
    new ObjectMapper().writeValue(out, ofy().load().type(Controller.class).filter("type", type).list());
  }

  private void doDisplayAlerts(HttpServletRequest request, HttpServletResponse response) throws IOException {
    PrintWriter out = response.getWriter();
    if (com.openhouseautomation.Flags.clearCache) {
      ofy().clear(); // clear the session cache, not the memcache
    }
    try {
      String json = "";
      JSONArray jsareturn = new JSONArray();
      // TODO convert to query for expired controllers
      List<Controller> controllers = ofy().load().type(Controller.class).list();
      for (Controller c : controllers) {
        if (c.isExpired()) {
          JSONObject jso = new JSONObject();
          jso.put("type", "danger")
                  .put("msg", "Controller offline: " + c.getName());
          jsareturn.put(jso);
        }
      }
      // TODO convert to query for expired sensors
      List<Sensor> sensors = ofy().load().type(Sensor.class).list();
      for (Sensor s : sensors) {
        if (s.isExpired()) {
          JSONObject jso = new JSONObject();
          jso.put("type", "danger")
                  .put("msg", "Sensor offline: " + s.getName());
          jsareturn.put(jso);
        }
      }
      jsareturn.write(out);
    } catch (JSONException e) {
      response.getWriter().print("[{ type: 'danger', msg: 'unable to retrieve alerts' }]");
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
    log.log(Level.INFO, this.getClass().getName() + " " + request.getMethod() + " " + request.getPathInfo());
//    Enumeration<String> es = request.getParameterNames();
//    while (es.hasMoreElements()) {
//      String sp = es.nextElement();
//      for (String spv : request.getParameterValues(sp)) {
//        //log.log(Level.WARNING, "doPost params:" + sp + "->" + spv);
//      }
//    }
    if (request.getPathInfo().startsWith("/controller/update")) {
      doControllerUpdate(request, response);
      return;
    }
    if (request.getPathInfo().startsWith("/scenes/set")) {
      doSceneSet(request, response);
      return;
    }
    response.sendError(HttpServletResponse.SC_NOT_FOUND);
  }

  public void doSceneSet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String sceneid = request.getParameter("id");
    log.log(Level.INFO, "going to set scene:" + sceneid);
    if (null == sceneid || "".equals(sceneid)) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }
    Scene scene = ofy().load().type(Scene.class).id(Long.parseLong(sceneid)).now();

    // log the event
    EventLog etl = new EventLog();
    etl.setIp(request.getRemoteAddr());
    etl.setNewState(scene.getName());
    etl.setPreviousState("");
    etl.setType("User change scene");
    etl.setUser(request.getRemoteUser());
    ofy().save().entity(etl);

    // parse and set the controller settings
    String config = scene.getConfig();
    ObjectMapper mapper = new ObjectMapper();
    //TypeReference tr = new TypeReference<List<SceneController>>() {};
    //List<SceneController> scconts = mapper.readValue(config, List.class);
    List<SceneController> scconts = mapper.readValue(config, List.class);
    for (SceneController scdes : scconts) {
      Controller controller = ofy().cache(false).load().type(Controller.class).id(scdes.getId()).now();
      if (controller == null) {
        log.log(Level.WARNING, "Controller " + scdes.getId() + " does not exist");
        continue;
      }
      String state = scdes.getDesiredState();
      if (state == null || "".equals(state)) {
        response.sendError(HttpServletResponse.SC_EXPECTATION_FAILED);
      }
      controller.setDesiredState(state);
      controller.setDesiredStatePriority(Controller.DesiredStatePriority.MANUAL);
      ofy().save().entity(controller);
      log.log(Level.INFO, "updated controller: " + controller.toString());
    }
    response.sendError(HttpServletResponse.SC_OK);
  }

  public void doControllerUpdate(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String controllerid = request.getParameter("id");
    log.log(Level.INFO, "going to update controller:" + controllerid);
    if (null == controllerid || "".equals(controllerid)) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }
    if (controllerid.equals("100")) { // all lights
      if (com.openhouseautomation.Flags.clearCache) {
        ofy().clear(); // clear the session cache, not the memcache
      }
      List<Controller> lights = ofy().load().type(Controller.class).filter("type", "LIGHTS").list();
      for (Controller c : lights) {
        c.setDesiredState(request.getParameter("desiredState"));
        c.setLastDesiredStateChange(Convutils.getNewDateTime());
        c.setLastContactDate(Convutils.getNewDateTime());
      }
      ofy().save().entities(lights);
      log.log(Level.INFO, "updated all controllers");
    } else { // an individual light or other device
      if (com.openhouseautomation.Flags.clearCache) {
        ofy().clear(); // clear the session cache, not the memcache
      }
      Controller controller = ofy().load().type(Controller.class).id(Long.parseLong(controllerid)).now();
      String oldcontroller = controller.toString();
      String state = request.getParameter("desiredState");
      if (state == null) {
        state = request.getParameter("desiredStatePriority");
        if ("AUTO".equals(state)) {
          controller.setDesiredStatePriority(Controller.DesiredStatePriority.AUTO);
        } else if ("MANUAL".equals(state)) {
          controller.setDesiredStatePriority(Controller.DesiredStatePriority.MANUAL);
        }
      } else {
        controller.setDesiredState(state);
        controller.setDesiredStatePriority(Controller.DesiredStatePriority.MANUAL);
      }
      if (controller.getType() == Controller.Type.RGB) {
        // send update to API
        // TODO Pull out API layer, abstract to Controller
        HouseTimers ht = new HouseTimers();
        if (ht.setcolor(controller) == 0) {
          // copy desired to actual as signal of completion
          controller.setActualState(controller.getDesiredState());
        }
        // because RGB controllers only listen on the Particle bus
        // so we fire an event through Particle API
        // and a 200 response is a probably-safe assumption that the controllers online heard it
        // if not, the 1 minute HouseTimers will catch it within 60s.
      }
      controller.setLastDesiredStateChange(Convutils.getNewDateTime());
      ofy().save().entity(controller).now();
      log.log(Level.INFO, "updated controller: " + controller.toString());
      // log the event
      EventLog etl = new EventLog();
      etl.setIp(request.getRemoteAddr());
      etl.setNewState(controller.toString());
      etl.setPreviousState(oldcontroller);
      etl.setType("User change controller");
      etl.setUser(request.getRemoteUser());
      ofy().save().entity(etl).now();
      response.sendError(HttpServletResponse.SC_OK);
    }
  }
  String testDeviceTypes = "[{\"name\":\"Thermostat\",\"link\":\"THERMOSTAT\",\"ordinal\":0,\"display\":false},{\"name\":\"Garage Door\",\"link\":\"GARAGEDOOR\",\"ordinal\":1,\"display\":false},{\"name\":\"Alarm\",\"link\":\"ALARM\",\"ordinal\":2,\"display\":true},{\"name\":\"Lights\",\"link\":\"LIGHTS\",\"ordinal\":3,\"display\":true},{\"name\":\"Sprinkler\",\"link\":\"SPRINKLER\",\"ordinal\":4,\"display\":false},{\"name\":\"Whole House Fan\",\"link\":\"WHOLEHOUSEFAN\",\"ordinal\":5,\"display\":true}]";
  String testSensorString = "[{\"expired\":false,\"id\":5744863563743232,\"owner\":\"dras\",\"location\":\"home\",\"zone\":\"outsideshadtemp\",\"type\":\"TEMPERATURE\",\"name\":\"Outside Temperature Shaded\",\"unit\":\"F\",\"lastreading\":\"70.25\"}]";
  String testControllerString = "[{\"id\":4280019022,\"owner\":\"dras\",\"location\":\"home\",\"zone\":\"atticwhf\",\"type\":\"WHOLEHOUSEFAN\",\"name\":\"Whole House Fan\",\"desiredStatePriority\":\"MANUAL\",\"validStates\":[\"0\",\"1\",\"2\",\"3\",\"4\",\"5\"],\"lastDesiredStateChange\":1414987381855,\"lastActualStateChange\":1414987381855,\"desiredState\":\"0\",\"actualState\":\"0\"}]";
  String testSceneString = "";
  String testNotificationString = "";
}
