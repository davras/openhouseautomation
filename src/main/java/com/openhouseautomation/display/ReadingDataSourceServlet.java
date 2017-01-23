package com.openhouseautomation.display;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Period;
import com.google.visualization.datasource.DataSourceServlet;
import com.google.visualization.datasource.datatable.ColumnDescription;
import com.google.visualization.datasource.datatable.DataTable;
import com.google.visualization.datasource.datatable.TableCell;
import com.google.visualization.datasource.datatable.TableRow;
import com.google.visualization.datasource.datatable.value.DateTimeValue;
import com.google.visualization.datasource.datatable.value.Value;
import com.google.visualization.datasource.datatable.value.ValueType;
import com.google.visualization.datasource.query.Query;
import com.openhouseautomation.Convutils;
// because import java.util.GregorianCalendar gives a type mismatch (wtf?)
//import java.util.GregorianCalendar; // DO NOT USE
//import com.ibm.icu.util.GregorianCalendar;
// converts java.util.GregorianCalendar (as returned by JodaTime.getGregorianCalendar)
// to com.ibm.icu.util.GregorianCalendar (as needed by Google Visualization Library)
import com.openhouseautomation.GregorianCalendar;
import static com.openhouseautomation.OfyService.ofy;
import com.openhouseautomation.model.DatastoreConfig;
import com.openhouseautomation.model.Reading;
import com.openhouseautomation.model.Sensor;

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

  int resolution = 10; // graph resolution in minutes
  int shortchartdays = Integer.parseInt(DatastoreConfig.getValueForKey("shortchartdays", "5"));
  int blocks = 60 / resolution * 24 * shortchartdays; // blocks of time in graph (300k)
  
  // TODO: only in place for testing, not for production
  @Override
  protected boolean isRestrictedAccessMode() {
    return false;
  }

  @Override
  public DataTable generateDataTable(Query query, HttpServletRequest request) {
    // Create a data table,
    DataTable data = new DataTable();
    data.addColumn(new ColumnDescription("date", ValueType.DATETIME, "Date"));
    String[] type = request.getParameterValues("type");
    long starttime = System.currentTimeMillis();
    List<Sensor> sensors;
    try {
      sensors = ofy().load().type(Sensor.class).filter("type", type[0]).list();
    } catch (Exception e) {
      log.log(Level.SEVERE, "could not retrieve sensor type: {0}", request.getParameterValues("type"));
      return null;
    }
    log.log(Level.INFO, "retrieve sensor classes took {0}ms", System.currentTimeMillis() - starttime);
    // add column descriptions
    for (Sensor s : sensors) {
      data.addColumn(new ColumnDescription(Long.toString(s.getId()), ValueType.NUMBER, s.getName()));
    }

    // use the sensors to get the readings
    shortchartdays = Integer.parseInt(DatastoreConfig.getValueForKey("shortchartdays", "5"));
    DateTime cutoffdate = Convutils.getNewDateTime().minus(Period.days(shortchartdays));
    log.log(Level.INFO, "filling {0} blocks", blocks);
    Double[][] readingsz = new Double[sensors.size()][blocks + 10]; // fudge for ArrayIndexOutOfBoundsException
    starttime = System.currentTimeMillis();
    // fill in the readings
    for (int i = 0; i < sensors.size(); i++) {
      Sensor s = (Sensor) sensors.get(i);
      // zero if needed
      if (s.getType() == Sensor.Type.RAIN) {
        for (int clrz=0; clrz < blocks; clrz++) {
          readingsz[i][clrz] = 0.0;
        }
      }
      long starttime2 = System.currentTimeMillis();
      Iterable<Reading> readings = ofy().load().type(Reading.class).ancestor(s).chunkAll().iterable();
      log.log(Level.INFO, "retrieve reading iterator took {0}ms", (System.currentTimeMillis() - starttime2));
      starttime2 = System.currentTimeMillis();
      int readingcount = 0;
      for (Reading reading : readings) {
        readingcount++;
        int blocknumber = (int) ((reading.getTimestamp().getMillis() - cutoffdate.getMillis()) / 1000 / 60 / resolution);
        if (blocknumber >= 0 && blocknumber < blocks) {
          try {
            readingsz[i][blocknumber] = Double.parseDouble(reading.getValue());
          } catch (java.lang.NumberFormatException e) {
            log.log(Level.WARNING, "could not parse: {0}", reading);
          }
        }
      }
      log.log(Level.INFO, "parse and store {0} readings took {1}ms", new Object[]{readingcount, System.currentTimeMillis() - starttime2});
    }
    log.log(Level.INFO, "time to get all readings: {0}ms", (System.currentTimeMillis() - starttime));

    starttime = System.currentTimeMillis();
    // now fill the data table
    try {
      for (int i = 0; i < blocks; i++) {
        String szonelocal = DatastoreConfig.getValueForKey("timezone", "America/Los_Angeles");
        DateTimeZone dtzonedisp = DateTimeZone.forID("UTC");
        DateTimeZone dtzonelocal = DateTimeZone.forID(szonelocal);
        long tzoffset = dtzonelocal.getOffsetFromLocal(cutoffdate.getMillis() + i * resolution * 60 * 1000);
        long offsettime = cutoffdate.getMillis() + i * resolution * 60 * 1000 + tzoffset;
        DateTime dt = new DateTime(offsettime, dtzonedisp);
        // without a timezone of GMT, you will get:
        // can't create DateTimeValue from GregorianCalendar that is not GMT.
        // and if you want your graph in a TZ other than GMT? Nope.
        GregorianCalendar cal = new GregorianCalendar(dt.toGregorianCalendar());

        // brittle below this line, stupid visualization chart servlet
        TableRow tr = new TableRow();
        tr.addCell(new TableCell(new DateTimeValue(cal)));
        for (int j = 0; j < sensors.size(); j++) {
          if (null == readingsz[j][i]) {
            // put in a blank
            tr.addCell(new TableCell(Value.getNullValueFromValueType(data.getColumnDescription(j + 1).getType())));
          } else {
            // put in the actual value
            tr.addCell(new TableCell(readingsz[j][i]));
          }
        }
        data.addRow(tr);
      }
    } catch (com.google.visualization.datasource.base.TypeMismatchException e) {
      log.log(Level.SEVERE, e.toString());
    }
    log.log(Level.INFO, "time to fill the data table: {0}ms", System.currentTimeMillis() - starttime);
    return data;
  }
}
