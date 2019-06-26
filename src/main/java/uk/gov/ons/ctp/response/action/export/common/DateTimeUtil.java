package uk.gov.ons.ctp.response.action.export.common;

import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import net.sourceforge.cobertura.CoverageIgnore;

@CoverageIgnore
public class DateTimeUtil {
  public static final String DATE_FORMAT_IN_JSON = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
  private static final Logger log = LoggerFactory.getLogger(DateTimeUtil.class);

  public DateTimeUtil() {}

  public static Timestamp nowUTC() {
    return new Timestamp(System.currentTimeMillis());
  }

  public static XMLGregorianCalendar giveMeCalendarForNow() throws DatatypeConfigurationException {
    GregorianCalendar gregorianCalendar = new GregorianCalendar();
    gregorianCalendar.setTime(new Date());
    XMLGregorianCalendar result = null;
    DatatypeFactory factory = DatatypeFactory.newInstance();
    result =
        factory.newXMLGregorianCalendar(
            gregorianCalendar.get(1),
            gregorianCalendar.get(2) + 1,
            gregorianCalendar.get(5),
            gregorianCalendar.get(11),
            gregorianCalendar.get(12),
            gregorianCalendar.get(13),
            gregorianCalendar.get(14),
            0);
    return result;
  }

  public static XMLGregorianCalendar stringToXMLGregorianCalendar(String string, String format)
      throws DatatypeConfigurationException {
    XMLGregorianCalendar result = null;
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);

    try {
      Date date = simpleDateFormat.parse(string);
      GregorianCalendar gregorianCalendar = (GregorianCalendar) GregorianCalendar.getInstance();
      gregorianCalendar.setTime(date);
      result = DatatypeFactory.newInstance().newXMLGregorianCalendar(gregorianCalendar);
    } catch (ParseException var7) {
      log.error("Failed to parse date", var7);
      result = giveMeCalendarForNow();
    }

    return result;
  }
}
