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

  private static final String ENGLAND = "E";
  private static final String ORG_NAME = "Castle of Frankenstein";
  private static final String POST_CODE = "postCode";
  private static final String POST_TOWN = "postTown";
  private static final String LOCALITY = "locality";
  private static final String SAMPLE_UNIT_REF = "SampleUnitRef21Long21";
  public static final String LINE_2 = "line_2";
  public static final String LINE_3 = "line_3";
  public static final String LINE_1 = "Address Line 1";
  public static final String EXERCISE_REF = "exRef";

  public static ActionRequest createICL_EnglandActionRequest(final String actionType) {
    return createICL_EnglandActionRequest(actionType, LINE_1, EXERCISE_REF);
  }

  public static ActionRequest createICL_EnglandActionRequest(
      final String actionType, String addressLine1, String exerciseRef) {
    ActionAddress actionAddress = new ActionAddress();
    actionAddress.setLine1(addressLine1);
    actionAddress.setLine2(LINE_2);
    actionAddress.setLine3(LINE_3);
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

  public static String actionInstructionToXmlString(ActionInstruction actionInstruction)
      throws JAXBException {
    JAXBContext jaxbContext = JAXBContext.newInstance(ActionInstruction.class);
    StringWriter stringWriter = new StringWriter();
    jaxbContext.createMarshaller().marshal(actionInstruction, stringWriter);
    return stringWriter.toString();
  }
}
