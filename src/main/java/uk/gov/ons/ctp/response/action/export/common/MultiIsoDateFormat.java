package uk.gov.ons.ctp.response.action.export.common;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class MultiIsoDateFormat extends AggregatedDateFormat {
  private static final String ISO_FORMAT_1 = "yyyy-MM-dd'T'HH:mm:ss.SSSX";
  private static final String ISO_FORMAT_2 = "yyyy-MM-dd'T'HH:mm:ss.SSSXX";
  private static final String ISO_FORMAT_3 = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";

  public MultiIsoDateFormat() {
    DateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
    DateFormat inputFormat1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX");
    DateFormat inputFormat2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXX");
    DateFormat inputFormat3 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
    DateFormat[] inputFormats = new DateFormat[] {inputFormat1, inputFormat2, inputFormat3};
    this.init(outputFormat, inputFormats);
  }
}
