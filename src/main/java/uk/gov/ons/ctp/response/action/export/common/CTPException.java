package uk.gov.ons.ctp.response.action.export.common;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.io.IOException;
import java.text.SimpleDateFormat;
import net.sourceforge.cobertura.CoverageIgnore;

@JsonSerialize(using = CTPException.OurExceptionSerializer.class)
@CoverageIgnore
public class CTPException extends Exception {
  private static final long serialVersionUID = -1569645569528433069L;
  private static final String UNDEFINED_MSG = "Non Specific Error";
  private CTPException.Fault fault;
  private long timestamp;

  public CTPException(CTPException.Fault afault) {
    this(afault, "Non Specific Error", (Object[]) null);
  }

  public CTPException(CTPException.Fault afault, Throwable cause) {
    this(afault, cause, cause != null ? cause.getMessage() : "", (Object[]) null);
  }

  public CTPException(CTPException.Fault afault, String message, Object... args) {
    this(afault, (Throwable) null, message, args);
  }

  public CTPException(CTPException.Fault afault, Throwable cause, String message, Object... args) {
    super(message != null ? String.format(message, args) : "", cause);
    this.timestamp = System.currentTimeMillis();
    this.fault = afault;
  }

  public final CTPException.Fault getFault() {
    return this.fault;
  }

  public final long getTimestamp() {
    return this.timestamp;
  }

  public String toString() {
    return super.toString() + ": " + this.fault.name();
  }

  public static class OurExceptionSerializer extends JsonSerializer<CTPException> {
    public OurExceptionSerializer() {}

    public final void serialize(CTPException value, JsonGenerator jgen, SerializerProvider provider)
        throws IOException {
      SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
      jgen.writeStartObject();
      jgen.writeFieldName("error");
      jgen.writeStartObject();
      jgen.writeStringField("code", value.getFault().name());
      jgen.writeStringField("timestamp", sdf.format(value.getTimestamp()));
      jgen.writeStringField("message", value.getMessage());
      jgen.writeEndObject();
      jgen.writeEndObject();
    }
  }

  public static enum Fault {
    SYSTEM_ERROR,
    RESOURCE_NOT_FOUND,
    RESOURCE_VERSION_CONFLICT,
    VALIDATION_FAILED,
    ACCESS_DENIED,
    BAD_REQUEST;

    private Fault() {}
  }
}
