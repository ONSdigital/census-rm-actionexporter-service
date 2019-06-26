package uk.gov.ons.ctp.response.action.export.actioninstuction;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

/**
 * Java class for Outcome.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <p>
 *
 * <pre>
 * &lt;simpleType name="Outcome"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="REQUEST_FAILED"/&gt;
 *     &lt;enumeration value="REQUEST_ACCEPTED"/&gt;
 *     &lt;enumeration value="REQUEST_COMPLETED"/&gt;
 *     &lt;enumeration value="REQUEST_DECLINED"/&gt;
 *     &lt;enumeration value="REQUEST_COMPLETED_DEACTIVATE"/&gt;
 *     &lt;enumeration value="REQUEST_COMPLETED_DISABLE"/&gt;
 *     &lt;enumeration value="CANCELLATION_FAILED"/&gt;
 *     &lt;enumeration value="CANCELLATION_ACCEPTED"/&gt;
 *     &lt;enumeration value="CANCELLATION_COMPLETED"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 */
@XmlType(name = "Outcome")
@XmlEnum
public enum Outcome {
  REQUEST_FAILED,
  REQUEST_ACCEPTED,
  REQUEST_COMPLETED,
  REQUEST_DECLINED,
  REQUEST_COMPLETED_DEACTIVATE,
  REQUEST_COMPLETED_DISABLE,
  CANCELLATION_FAILED,
  CANCELLATION_ACCEPTED,
  CANCELLATION_COMPLETED;

  public String value() {
    return name();
  }

  public static Outcome fromValue(String v) {
    return valueOf(v);
  }
}
