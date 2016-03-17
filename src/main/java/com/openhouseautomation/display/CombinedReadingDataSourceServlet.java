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
import com.openhouseautomation.model.ReadingHistory;
import com.openhouseautomation.model.Sensor;
import java.util.ArrayList;

import java.util.List;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author dras
 */
public class CombinedReadingDataSourceServlet extends DataSourceServlet {

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
    String sensorid = request.getParameter("id");
    Sensor sensor = ofy().load().type(Sensor.class).id(Long.parseLong(sensorid)).safe();
    cd.add(new ColumnDescription(Long.toString(sensor.getId()), ValueType.NUMBER, sensor.getName()));
    cd.add(new ColumnDescription("high", ValueType.NUMBER, "High"));
    cd.add(new ColumnDescription("low", ValueType.NUMBER, "Low"));
    data.addColumns(cd);
    int shortchartdays = Integer.parseInt(DatastoreConfig.getValueForKey("shortchartdays", "7"));
    DateTime cutoffdate = Convutils.getNewDateTime().minus(Period.days(shortchartdays));
    // pull old readings first
    List<ReadingHistory> readingshist = ofy().load().type(ReadingHistory.class).ancestor(sensor).filter("timestamp <", cutoffdate).chunkAll().list();
    DateTimeZone dtzonedisp = DateTimeZone.forID("UTC");
    DateTime dt;
    for (ReadingHistory readinghist : readingshist) {
      dt = new DateTime(readinghist.getTimestamp(), dtzonedisp);
      // without a timezone of GMT, you will get:
      // can't create DateTimeValue from GregorianCalendar that is not GMT.
      // and if you want your graph in a TZ other than GMT? Nope.
      GregorianCalendar cal = new GregorianCalendar(dt.toGregorianCalendar());
      try {
        data.addRowFromValues(cal, null, new Double(readinghist.getHigh()), new Double(readinghist.getLow()));
      } catch (TypeMismatchException e) {
        e.printStackTrace();
      }
    }

    //List<Reading> readings = ofy().load().type(Reading.class).ancestor(sensor).filter("timestamp >", cutoffdate).chunkAll().list();
    List<Reading> readings = ofy().load().type(Reading.class).ancestor(sensor).chunkAll().list();
    for (Reading reading : readings) {
      dt = new DateTime(reading.getTimestamp(), dtzonedisp);
      // without a timezone of GMT, you will get:
      // can't create DateTimeValue from GregorianCalendar that is not GMT.
      // and if you want your graph in a TZ other than GMT? Nope.
      GregorianCalendar calz = new GregorianCalendar(dt.toGregorianCalendar());
      try {
        data.addRowFromValues(calz, new Double(reading.getValue()), null, null);
      } catch (TypeMismatchException e) {
        e.printStackTrace();
      }
    }
    return data;
  }
}
