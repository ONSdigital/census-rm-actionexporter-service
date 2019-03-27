package uk.gov.ons.ctp.response.action.export.service;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;

import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import com.jcraft.jsch.ChannelSftp;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.BlockingQueue;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.integration.sftp.session.DefaultSftpSessionFactory;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.ons.ctp.common.message.rabbit.Rabbitmq;
import uk.gov.ons.ctp.response.action.export.config.AppConfig;
import uk.gov.ons.ctp.response.action.export.utility.ActionRequestBuilder;
import uk.gov.ons.ctp.response.action.message.instruction.ActionInstruction;
import uk.gov.ons.ctp.response.action.message.instruction.ActionRequest;
import uk.gov.ons.tools.rabbit.SimpleMessageBase;
import uk.gov.ons.tools.rabbit.SimpleMessageListener;
import uk.gov.ons.tools.rabbit.SimpleMessageSender;

@RunWith(SpringRunner.class)
@ContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class TemplateServiceIT {
  private static final Logger log = LoggerFactory.getLogger(TemplateServiceIT.class);
  public static final String ICL1E = "ICL1E";
  public static final String P_IC_ICL_1 = "P_IC_ICL1";

  @Autowired private AppConfig appConfig;

  @Autowired private DefaultSftpSessionFactory defaultSftpSessionFactory;

  private SimpleMessageSender simpleMessageSender;
  private SimpleMessageListener simpleMessageListener;

  @Before
  public void setUp() throws IOException {
    Rabbitmq rabbitConfig = this.appConfig.getRabbitmq();
    simpleMessageSender =
        new SimpleMessageSender(
            rabbitConfig.getHost(),
            rabbitConfig.getPort(),
            rabbitConfig.getUsername(),
            rabbitConfig.getPassword());

    simpleMessageListener =
        new SimpleMessageListener(
            rabbitConfig.getHost(),
            rabbitConfig.getPort(),
            rabbitConfig.getUsername(),
            rabbitConfig.getPassword());
  }

  @Test
  public void testTemplateGeneratesCorrectPrintFileForCensusICL() throws Exception {
    // Given
    ActionRequest actionRequest = ActionRequestBuilder.createICL_EnglandActionRequest(ICL1E);

    ActionInstruction actionInstruction = new ActionInstruction();
    actionInstruction.setActionRequest(actionRequest);
    BlockingQueue<String> queue =
        simpleMessageListener.listen(
            SimpleMessageBase.ExchangeType.Fanout, "event-message-outbound-exchange");

    simpleMessageSender.sendMessage(
        "action-outbound-exchange",
        "Action.Printer.binding",
        ActionRequestBuilder.actionInstructionToXmlString(actionInstruction));

    // When
    String message = queue.take();

    // Then
    assertThat(message, containsString(P_IC_ICL_1));
    String notificationFilePath = getLatestSftpFileName();

    InputStream inputSteam = defaultSftpSessionFactory.getSession().readRaw(notificationFilePath);
    defaultSftpSessionFactory.getSession();

    String fileLine = convertInputSteamToString(inputSteam);
    assertEquals(
        "test-iac|caseRef|Address Line 1|line_2|line_3|postTown|postCode|P_IC_ICL1", fileLine);

    assertTrue(defaultSftpSessionFactory.getSession().remove(notificationFilePath));
  }

  @Test
  public void testMostRecentAddressUsedWhenDuplicateSampleUnitRefs() throws Exception {
    // Given
    ActionInstruction firstActionInstruction =
        createActionInstruction(ICL1E, "Old Address", "exercise_1");
    ActionInstruction secondActionInstruction =
        createActionInstruction(ICL1E, "New Address", "exercise_2");

    BlockingQueue<String> queue =
        simpleMessageListener.listen(
            SimpleMessageBase.ExchangeType.Fanout, "event-message-outbound-exchange");

    simpleMessageSender.sendMessage(
        "action-outbound-exchange",
        "Action.Printer.binding",
        ActionRequestBuilder.actionInstructionToXmlString(firstActionInstruction));

    // When
    String firstActionExportConfirmation = queue.take();

    assertThat(firstActionExportConfirmation, containsString(P_IC_ICL_1));
    String firstNotificationFilePath = getLatestSftpFileName();
    assertTrue(defaultSftpSessionFactory.getSession().remove(firstNotificationFilePath));
    defaultSftpSessionFactory.getSession().close();

    simpleMessageSender.sendMessage(
        "action-outbound-exchange",
        "Action.Printer.binding",
        ActionRequestBuilder.actionInstructionToXmlString(secondActionInstruction));

    String secondActionExportConfirmation = queue.take();

    // Then
    assertThat(secondActionExportConfirmation, containsString(P_IC_ICL_1));
    String secondNotificationFilePath = getLatestSftpFileName();
    InputStream inputSteam =
        defaultSftpSessionFactory.getSession().readRaw(secondNotificationFilePath);

    String fileLine = convertInputSteamToString(inputSteam);
    assertEquals(
        "test-iac|caseRef|New Address|line_2|line_3|postTown|postCode|P_IC_ICL1", fileLine);

    assertTrue(defaultSftpSessionFactory.getSession().remove(secondNotificationFilePath));
  }

  public String convertInputSteamToString(InputStream inputStream) throws IOException {
    try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
      return br.lines().collect(Collectors.joining(System.lineSeparator()));
    }
  }

  private String getLatestSftpFileName() throws IOException {
    Comparator<ChannelSftp.LsEntry> sortByModifiedTimeDescending =
        (f1, f2) -> Integer.compare(f2.getAttrs().getMTime(), f1.getAttrs().getMTime());

    String sftpPath = "Documents/sftp/";
    ChannelSftp.LsEntry[] sftpList = defaultSftpSessionFactory.getSession().list(sftpPath);

    System.out.println("Latest File Length list: " + sftpList.length);

    ChannelSftp.LsEntry latestFile =
        Arrays.stream(sftpList)
            .filter(f -> f.getFilename().endsWith(".csv"))
            .min(sortByModifiedTimeDescending)
            .orElseThrow(() -> new RuntimeException("No file on SFTP"));
    log.with("latest_file", latestFile.getFilename()).info("Found latest file");

    System.out.println("Got latest file" + latestFile.getFilename());

    return sftpPath + latestFile.getFilename();
  }

  private ActionInstruction createActionInstruction(
      String actionType, String addressLine1, String exerciseRef) {
    ActionRequest actionRequest =
        ActionRequestBuilder.createICL_EnglandActionRequest(actionType, addressLine1, exerciseRef);
    ActionInstruction actionInstruction = new ActionInstruction();
    actionInstruction.setActionRequest(actionRequest);

    return actionInstruction;
  }
}
