package uk.gov.ons.ctp.response.action.export.common;

import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import net.sourceforge.cobertura.CoverageIgnore;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@CoverageIgnore
public class RestExceptionHandler {
  private static final Logger log = LoggerFactory.getLogger(RestExceptionHandler.class);

  public RestExceptionHandler() {}

  @ExceptionHandler({CTPException.class})
  public ResponseEntity<?> handleCTPException(CTPException exception) {
    log.with("fault", exception.getFault())
        .with("exception_message", exception.getMessage())
        .error("Uncaught CTPException", exception);
    HttpStatus status;
    switch (exception.getFault()) {
      case RESOURCE_NOT_FOUND:
        status = HttpStatus.NOT_FOUND;
        break;
      case RESOURCE_VERSION_CONFLICT:
        status = HttpStatus.CONFLICT;
        break;
      case ACCESS_DENIED:
        status = HttpStatus.UNAUTHORIZED;
        break;
      case BAD_REQUEST:
      case VALIDATION_FAILED:
        status = HttpStatus.BAD_REQUEST;
        break;
      case SYSTEM_ERROR:
        status = HttpStatus.INTERNAL_SERVER_ERROR;
        break;
      default:
        status = HttpStatus.I_AM_A_TEAPOT;
    }

    return new ResponseEntity(exception, status);
  }

  @ExceptionHandler({Throwable.class})
  public ResponseEntity<?> handleGeneralException(Throwable t) {
    log.error("Uncaught Throwable", t);
    return new ResponseEntity(
        new CTPException(CTPException.Fault.SYSTEM_ERROR, t, t.getMessage(), new Object[0]),
        HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
