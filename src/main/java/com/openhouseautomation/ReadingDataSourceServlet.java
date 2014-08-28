package com.openhouseautomation;

import static com.openhouseautomation.OfyService.ofy;

import com.openhouseautomation.model.Reading;
import com.openhouseautomation.model.Sensor;
import com.google.visualization.datasource.DataSourceServlet;
import com.google.visualization.datasource.base.TypeMismatchException;
import com.google.visualization.datasource.datatable.ColumnDescription;
import com.google.visualization.datasource.datatable.DataTable;
import com.google.visualization.datasource.datatable.value.ValueType;
import com.google.visualization.datasource.query.Query;
import com.ibm.icu.util.GregorianCalendar;
import com.ibm.icu.util.TimeZone;

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
    cd.add(new ColumnDescription("reading", ValueType.NUMBER, "Reading"));
    data.addColumns(cd);

    // Fill the data table.
    try {
      // TODO pass sensor id in as a parameter, then pass back values for that id
      Sensor sensor =
          ofy().load().type(Sensor.class).id(Long.parseLong(request.getParameter("id"))).now();
      Date cutoffdate = new Date(System.currentTimeMillis() - (1000 * 60 * 60 * 24 * 7));
      List<Reading> readings =
          ofy().load().type(Reading.class).ancestor(sensor).filter("timestamp >", cutoffdate)
              .list();
      GregorianCalendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
      // without a timezone of GMT, you will get:
      // can't create DateTimeValue from GregorianCalendar that is not GMT.
      // and if you want your graph in a TZ other than GMT? Nope.
      for (Reading reading : readings) {
        cal.setTime(new Date(reading.getTimestamp().getTime() - (7 * 60 * 60 * 1000))); // convert
                                                                                        // timezones...TODO
                                                                                        // fix this
                                                                                        // kludge
        data.addRowFromValues(cal, new Double(reading.getValue()));
      }
    } catch (final TypeMismatchException e) {
      log.log(Level.SEVERE, e.toString(), e);
    }
    return data;
  }
}
