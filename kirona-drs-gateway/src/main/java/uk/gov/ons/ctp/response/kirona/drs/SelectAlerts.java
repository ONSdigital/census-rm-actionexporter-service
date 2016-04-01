
package uk.gov.ons.ctp.response.kirona.drs;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for selectAlerts complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="selectAlerts">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="selectAlerts" type="{http://autogenerated.OTWebServiceApi.xmbrace.com/}xmbSelectAlerts" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "selectAlerts", propOrder = {
    "selectAlerts"
})
public class SelectAlerts {

    protected XmbSelectAlerts selectAlerts;

    /**
     * Gets the value of the selectAlerts property.
     * 
     * @return
     *     possible object is
     *     {@link XmbSelectAlerts }
     *     
     */
    public XmbSelectAlerts getSelectAlerts() {
        return selectAlerts;
    }

    /**
     * Sets the value of the selectAlerts property.
     * 
     * @param value
     *     allowed object is
     *     {@link XmbSelectAlerts }
     *     
     */
    public void setSelectAlerts(XmbSelectAlerts value) {
        this.selectAlerts = value;
    }

}