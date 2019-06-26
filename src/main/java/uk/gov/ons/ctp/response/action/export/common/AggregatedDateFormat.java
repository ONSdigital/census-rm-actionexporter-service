package uk.gov.ons.ctp.response.action.export.common;

import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.util.Arrays;
import java.util.Date;

public class AggregatedDateFormat extends DateFormat {
  private static final Logger log = LoggerFactory.getLogger(AggregatedDateFormat.class);
  private DateFormat[] inputFormats;
  private DateFormat outputFormat;

  public AggregatedDateFormat() {}

  public void init(DateFormat outputFormat, DateFormat[] inputFormats) {
    this.inputFormats = inputFormats;
    this.outputFormat = outputFormat;
  }

  public StringBuffer format(Date date, StringBuffer toAppendTo, FieldPosition fieldPosition) {
    log.with("date", date).trace("Formatting date to string");
    return this.outputFormat.format(date, toAppendTo, fieldPosition);
  }

  public Object clone() {
    AggregatedDateFormat result = new AggregatedDateFormat();
    DateFormat[] inputFormatsClone = new DateFormat[this.inputFormats.length];

    for (int i = 0; i < this.inputFormats.length; ++i) {
      inputFormatsClone[i] = (DateFormat) this.inputFormats[i].clone();
    }

    result.init((DateFormat) this.outputFormat.clone(), inputFormatsClone);
    return result;
  }

  public Date parse(String source, ParsePosition pos) {
    log.with("string", source).trace("Parsing string to date");
    return (Date)
        Arrays.stream(this.inputFormats)
            .map(
                (d) -> {
                  return d.parse(source, pos);
                })
            .findFirst()
            .orElse(null);
  }
}
