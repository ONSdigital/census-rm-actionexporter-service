package uk.gov.ons.ctp.response.action.export.service;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

import ma.glasnost.orika.MapperFacade;
import org.junit.Test;
import uk.gov.ons.ctp.response.action.export.message.ActionFeedbackPublisher;
import uk.gov.ons.ctp.response.action.export.repository.ActionRequestRepository;
import uk.gov.ons.ctp.response.action.export.repository.AddressRepository;
import uk.gov.ons.ctp.response.action.message.instruction.ActionInstruction;

public class ActionExportServiceTest {
  ActionFeedbackPublisher actionFeedbackPubl = mock(ActionFeedbackPublisher.class);
  MapperFacade mapperFacade = mock(MapperFacade.class);
  ActionRequestRepository actionRequestRepo = mock(ActionRequestRepository.class);
  AddressRepository addressRepo = mock(AddressRepository.class);

  @Test
  public void testacceptInstructionNullRequestAndNoCancel() {
    // Given
    ActionInstruction actionInstruction = new ActionInstruction();

    ActionExportService actionExportService =
        new ActionExportService(actionFeedbackPubl, mapperFacade, actionRequestRepo, addressRepo);

    // Then
    actionExportService.acceptInstruction(actionInstruction);

    assertTrue(true);
  }
}
