package uk.gov.ons.ctp.response.action.export.utility;

import java.io.StringWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.UUID;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import uk.gov.ons.ctp.response.action.message.instruction.ActionAddress;
import uk.gov.ons.ctp.response.action.message.instruction.ActionContact;
import uk.gov.ons.ctp.response.action.message.instruction.ActionEvent;
import uk.gov.ons.ctp.response.action.message.instruction.ActionInstruction;
import uk.gov.ons.ctp.response.action.message.instruction.ActionRequest;
import uk.gov.ons.ctp.response.action.message.instruction.Priority;

public class ActionRequestBuilder {

  public static final String ENGLAND = "E";
  public static final String ORG_NAME = "Castle of Frankenstein";
  public static final String POST_CODE = "postCode";
  public static final String POST_TOWN = "postTown";
  public static final String LOCALITY = "locality";
  public static final String SAMPLE_UNIT_REF = "sampleUR";

  public static ActionRequest createSocialActionRequest(final String actionType) {
    return createSocialActionRequest(actionType, "Prem1", "exRef");
  }

  public static ActionRequest createICL_EnglandActionRequest(final String actionType) {
    return createICL_EnglandActionRequest(actionType, "Prem1", "exRef");
  }

  public static ActionRequest createICL_EnglandActionRequest(
      final String actionType, String addressLine1, String exerciseRef) {
    ActionAddress actionAddress = new ActionAddress();
    actionAddress.setLine1(addressLine1);
    actionAddress.setCountry(ENGLAND);
    actionAddress.setOrganisationName(ORG_NAME);
    actionAddress.setPostcode(POST_CODE);
    actionAddress.setTownName(POST_TOWN);
    actionAddress.setLocality(LOCALITY);

    ActionRequest actionRequest = new ActionRequest();
    actionRequest.setSampleUnitRef(SAMPLE_UNIT_REF);
    actionRequest.setActionId(UUID.randomUUID().toString());
    actionRequest.setActionPlan("actionPlan");
    actionRequest.setActionType(actionType);
    actionRequest.setAddress(actionAddress);
    actionRequest.setQuestionSet("questions");
    actionRequest.setLegalBasis("legalBasis");
    actionRequest.setRegion("region");
    actionRequest.setRespondentStatus("rStatus");
    actionRequest.setEnrolmentStatus("eStatus");
    actionRequest.setCaseGroupStatus("cgStatus");
    actionRequest.setCaseId(UUID.randomUUID().toString());
    actionRequest.setPriority(Priority.HIGHEST);
    actionRequest.setCaseRef("caseRef");
    actionRequest.setIac("test-iac");
    actionRequest.setExerciseRef(exerciseRef);
    actionRequest.setContact(new ActionContact());
    actionRequest.setEvents(new ActionEvent(Collections.singletonList("event1")));
    actionRequest.setReturnByDate(DateTimeFormatter.ofPattern("dd/MM").format(LocalDate.now()));

    return actionRequest;
  }

  public static ActionRequest createSocialActionRequest(
      final String actionType, String addressLine1, String exerciseRef) {
    ActionAddress actionAddress = new ActionAddress();
    actionAddress.setLine1(addressLine1);
    actionAddress.setCountry("E");
    actionAddress.setOrganisationName("Castle of Frankenstein");
    actionAddress.setPostcode("postCode");
    actionAddress.setTownName("postTown");
    actionAddress.setLocality("locality");

    ActionRequest actionRequest = new ActionRequest();
    actionRequest.setSampleUnitRef("sampleUR");
    actionRequest.setActionId(UUID.randomUUID().toString());
    actionRequest.setActionPlan("actionPlan");
    actionRequest.setActionType(actionType);
    actionRequest.setAddress(actionAddress);
    actionRequest.setQuestionSet("questions");
    actionRequest.setLegalBasis("legalBasis");
    actionRequest.setRegion("region");
    actionRequest.setRespondentStatus("rStatus");
    actionRequest.setEnrolmentStatus("eStatus");
    actionRequest.setCaseGroupStatus("cgStatus");
    actionRequest.setCaseId(UUID.randomUUID().toString());
    actionRequest.setPriority(Priority.HIGHEST);
    actionRequest.setCaseRef("caseRef");
    actionRequest.setIac("test-iac");
    actionRequest.setExerciseRef(exerciseRef);
    actionRequest.setContact(new ActionContact());
    actionRequest.setEvents(new ActionEvent(Collections.singletonList("event1")));
    actionRequest.setReturnByDate(DateTimeFormatter.ofPattern("dd/MM").format(LocalDate.now()));

    return actionRequest;
  }

  public static String actionInstructionToXmlString(ActionInstruction actionInstruction)
      throws JAXBException {
    JAXBContext jaxbContext = JAXBContext.newInstance(ActionInstruction.class);
    StringWriter stringWriter = new StringWriter();
    jaxbContext.createMarshaller().marshal(actionInstruction, stringWriter);
    return stringWriter.toString();
  }
}
