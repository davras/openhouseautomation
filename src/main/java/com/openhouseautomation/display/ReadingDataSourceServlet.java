package com.openhouseautomation.display;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Period;
import com.google.visualization.datasource.DataSourceServlet;
import com.google.visualization.datasource.base.TypeMismatchException;
import com.google.visualization.datasource.datatable.ColumnDescription;
import com.google.visualization.datasource.datatable.DataTable;
import com.google.visualization.datasource.datatable.value.ValueType;
import com.google.visualization.datasource.query.Query;
import com.googlecode.objectify.NotFoundException;
// because import java.util.GregorianCalendar gives a type mismatch (wtf?)
//import java.util.GregorianCalendar; // DO NOT USE
import com.ibm.icu.util.GregorianCalendar;
// converts java.util.GregorianCalendar (as returned by JodaTime.getGregorianCalendar)
// to com.ibm.icu.util.GregorianCalendar (as needed by Google Visualization Library)
import com.openhouseautomation.GregorianCalendarCopy;
import static com.openhouseautomation.OfyService.ofy;
import com.openhouseautomation.model.DatastoreConfig;
import com.openhouseautomation.model.Reading;
import com.openhouseautomation.model.Sensor;
import java.util.ArrayList;

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
    } catch (Exception e) {
      // can't send a response
      log.log(Level.SEVERE, "could not retrieve entity for {0}", request.getParameterValues("id"));
      return null;
    }
    data.addColumns(cd);

    // use the sensors to get the readings
    int shortchartdays = Integer.parseInt(DatastoreConfig.getValueForKey("shortchartdays", "7"));
    DateTime cutoffdate = new DateTime().minus(Period.days(shortchartdays));
    int resolution = 5; // graph resolution in minutes
    int blocks = resolution * 60 * 1000; // blocks of time in graph (300k)
    int positions = shortchartdays * 24 * 60 * 60 * 1000 / blocks;
    double[][] readingsz = new double[sensors.length][positions + 10]; // fudge for ArrayIndexOutOfBoundsException
    for (int i = 0; i < sensors.length; i++) {
      List<Reading> readings = ofy().load().type(Reading.class).ancestor(sensors[i])
              .filter("timestamp >", cutoffdate).chunkAll().list();
      for (Reading reading : readings) {
        int blocknumber = (int) ((reading.getTimestamp().getMillis() - cutoffdate.getMillis()) / blocks);
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
    try {
      for (int i = 0; i < positions; i++) {
        String szonelocal = DatastoreConfig.getValueForKey("timezone", "America/Los_Angeles");
        DateTimeZone dtzonedisp = DateTimeZone.forID("UTC");
        DateTimeZone dtzonelocal = DateTimeZone.forID(szonelocal);
        long tzoffset = dtzonelocal.getOffsetFromLocal(cutoffdate.getMillis() + i * resolution * 60 * 1000);
        long offsettime = cutoffdate.getMillis() + i * resolution * 60 * 1000 + tzoffset;
        DateTime dt = new DateTime(offsettime, dtzonedisp);
        // without a timezone of GMT, you will get:
        // can't create DateTimeValue from GregorianCalendar that is not GMT.
        // and if you want your graph in a TZ other than GMT? Nope.
        GregorianCalendar cal = GregorianCalendarCopy.convert(dt.toGregorianCalendar());
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
          case 4:
            data.addRowFromValues(cal, new Double(readingsz[0][i]),
                    new Double(readingsz[1][i]), new Double(readingsz[2][i]),
                    new Double(readingsz[3][i]));
            break;
          case 5:
          default:
            data.addRowFromValues(cal, new Double(readingsz[0][i]),
                    new Double(readingsz[1][i]), new Double(readingsz[2][i]),
                    new Double(readingsz[3][i]), new Double(readingsz[4][i]));
            break;
        }
      }
    } catch (TypeMismatchException e) {
      log.log(Level.SEVERE, e.toString(), e);
    }
    return data;
  }
}
