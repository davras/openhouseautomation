package com.openhouseautomation.mapreduce;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.tools.mapreduce.Input;
import com.google.appengine.tools.mapreduce.MapReduceJob;
import com.google.appengine.tools.mapreduce.MapReduceSettings;
import com.google.appengine.tools.mapreduce.MapReduceSpecification;
import com.google.appengine.tools.mapreduce.Mapper;
import com.google.appengine.tools.mapreduce.Marshallers;
import com.google.appengine.tools.mapreduce.Output;
import com.google.appengine.tools.mapreduce.Reducer;
import com.google.appengine.tools.mapreduce.inputs.DatastoreInput;
import com.google.appengine.tools.mapreduce.outputs.DatastoreOutput;
import com.openhouseautomation.model.ReadingHistory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author dras
 */
public class ReadingServletMR extends HttpServlet {
  private static final long serialVersionUID = 1L;
  private static final Logger log = Logger.getLogger(ReadingServletMR.class.getName());

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
    redirectToPipelineStatus(request, response, startStatsJob(1, 1));

  }

  private String startStatsJob(int mapShardCount, int reduceShardCount) {
    // (I, K, V, O, R)
    // Input<I>, Input<Entity>, so I=Entity
    Input input = new DatastoreInput("Reading", mapShardCount);

    // Mapper<I,K,V>, Mapper<Entity, String, String>, so I=Entity, K=String, V=String
    Mapper<Entity, String, String> mapper = new ReadingMapper();

    // Reducer<K,V,O>, Reducer<String, String, ReadingHistory>, so K=String, V=String,
    // O=ReadingHistory
    Reducer<String, String, ReadingHistory> reducer = new ReadingReducer();

    // Output<O,R>, Output<ReadingHistory>, so O=ReadingHistory, R=Void
    Output output = new DatastoreOutput();

//    MapReduceSpecification<Entity, String, String, ReadingHistory, Object> mrspecs =
//        new MapReduceSpecification.Builder<>(input, mapper, reducer, output)
//            .setKeyMarshaller(Marshallers.getStringMarshaller())
//            .setValueMarshaller(Marshallers.getStringMarshaller()).setNumReducers(reduceShardCount)
//            .setJobName("history").build();

    MapReduceSpecification<Entity, String, String, ReadingHistory, Object>
        mrspecs = new MapReduceSpecification.Builder<>(input, mapper, reducer, output)
            .setKeyMarshaller(Marshallers.getStringMarshaller())
            .setValueMarshaller(Marshallers.getStringMarshaller())
            .setNumReducers(5)
            .build();
    
    
    MapReduceSettings mrsettings = new MapReduceSettings.Builder().setMillisPerSlice(55000).build();
    return MapReduceJob.start(mrspecs, mrsettings);
  }

  private String getUrlBase(HttpServletRequest req) throws MalformedURLException {
    URL requestUrl = new URL(req.getRequestURL().toString());
    String portString = requestUrl.getPort() == -1 ? "" : ":" + requestUrl.getPort();
    return requestUrl.getProtocol() + "://" + requestUrl.getHost() + portString + "/";
  }

  private String getPipelineStatusUrl(String urlBase, String pipelineId) {
    return urlBase + "_ah/pipeline/status.html?root=" + pipelineId;
  }

  private void redirectToPipelineStatus(HttpServletRequest req, HttpServletResponse resp,
      String pipelineId) throws IOException {
    String destinationUrl = getPipelineStatusUrl(getUrlBase(req), pipelineId);
    log.info("Redirecting to " + destinationUrl);
    resp.sendRedirect(destinationUrl);
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
