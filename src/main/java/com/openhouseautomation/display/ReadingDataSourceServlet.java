package com.openhouseautomation.display;

import com.google.visualization.datasource.DataSourceServlet;
import com.google.visualization.datasource.base.TypeMismatchException;
import com.google.visualization.datasource.datatable.ColumnDescription;
import com.google.visualization.datasource.datatable.DataTable;
import com.google.visualization.datasource.datatable.value.ValueType;
import com.google.visualization.datasource.query.Query;
import com.googlecode.objectify.NotFoundException;
import com.ibm.icu.util.GregorianCalendar;
import com.ibm.icu.util.TimeZone;
import static com.openhouseautomation.OfyService.ofy;
import com.openhouseautomation.model.Reading;
import com.openhouseautomation.model.Sensor;

import java.util.ArrayList;
// because import java.util.GregorianCalendar gives a type mismatch (wtf?)
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author dras
 */
public class ReadingDataSourceServlet extends DataSourceServlet {

  private static final long serialVersionUID = 1L;
  private static final Logger log = Logger.getLogger(ReadingDataSourceServlet.class.getName());

  // TODO: only in place for testing, not for production
  @Override
  protected boolean isRestrictedAccessMode() {
    return false;
  }

  @Override
  public DataTable generateDataTable(Query query, HttpServletRequest request) {
    // Create a data table,
    DataTable data = new DataTable();
    ArrayList cd = new ArrayList();
    cd.add(new ColumnDescription("date", ValueType.DATETIME, "Date"));
    String[] sensorids = request.getParameterValues("id");
    Sensor[] sensors = new Sensor[sensorids.length];

    // get the sensors
    try {
      for (int i = 0; i < sensorids.length; i++) {
        sensors[i] = ofy().load().type(Sensor.class).id(Long.parseLong(sensorids[i])).safe();
        cd.add(new ColumnDescription(Long.toString(sensors[i].getId()), ValueType.NUMBER, sensors[i].getName()));
      }
    } catch (NotFoundException | NumberFormatException e) {
      // can't send a response
      log.log(Level.SEVERE, "could not retrieve entity");
      return null;
    }
    data.addColumns(cd);

    // use the sensors to get the readings
    Date cutoffdate = new Date(System.currentTimeMillis() - (1000 * 60 * 60 * 24 * 7));
    int resolution = 2; // graph resolution in minutes
    int blocks = resolution * 60 * 1000; // blocks of time in graph
    int positions = 7 * 24 * 60 * 60 * 1000 / blocks;
    double[][] readingsz = new double[sensors.length][positions];
    for (int i = 0; i < sensors.length; i++) {
      List<Reading> readings = ofy().load().type(Reading.class).ancestor(sensors[i]).filter("timestamp >", cutoffdate).chunkAll().list();
      for (Reading reading : readings) {
        int blocknumber = (int) ((reading.getTimestamp().getTime() - cutoffdate.getTime()) / blocks);
        readingsz[i][blocknumber] = Double.parseDouble(reading.getValue());
      }
    }

    // backfill leading zeros
    for (int j = 0; j < sensors.length; j++) {
      int i = 0;
      while (readingsz[j][i] == 0) {
        i++;
        if (i == positions - 1) {
          break;
        }
      }
      for (int k = 0; k < i; k++) {
        readingsz[j][k] = readingsz[j][i];
      }
    }

    // forward fill other zeros
    for (int j = 0; j < sensors.length; j++) {
      double lastgoodreading = readingsz[j][0];
      for (int i = 0; i < positions; i++) {
        if (readingsz[j][i] != 0) {
          lastgoodreading = readingsz[j][i];
        } else {
          readingsz[j][i] = lastgoodreading;
        }
      }
    }

    // now fill the data table
    GregorianCalendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
    // without a timezone of GMT, you will get:
    // can't create DateTimeValue from GregorianCalendar that is not GMT.
    // and if you want your graph in a TZ other than GMT? Nope.

    try {
      for (int i = 0; i < positions; i++) {
        cal.setTime(new Date(i * resolution * 60 * 1000 + cutoffdate.getTime() - (8 * 60 * 60 * 1000)));
        // TODO fix this for a specific TZ offset in minutes that is pulled from DS config
        switch (sensors.length) {
          case 1:
            data.addRowFromValues(cal, new Double(readingsz[0][i]));
            break;
          case 2:
            data.addRowFromValues(cal, new Double(readingsz[0][i]),
                    new Double(readingsz[1][i]));
            break;
          case 3:
            data.addRowFromValues(cal, new Double(readingsz[0][i]),
                    new Double(readingsz[1][i]), new Double(readingsz[2][i]));
            break;
        }
      }
    } catch (TypeMismatchException e) {
      log.log(Level.WARNING, "exception formatting data for tablerow: {0}", e.fillInStackTrace());
    }
    return data;
  }
}
