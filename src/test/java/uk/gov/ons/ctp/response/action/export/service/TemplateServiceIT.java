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
import java.nio.charset.Charset;
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
import org.springframework.integration.sftp.session.SftpSession;
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

  @Autowired private AppConfig appConfig;

  @Autowired private DefaultSftpSessionFactory defaultSftpSessionFactory;

  private SimpleMessageSender simpleMessageSender;
  private SimpleMessageListener simpleMessageListener;

  @Before
  public void setUp() throws IOException {
    cleanSftpServer();
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
    final String CensusICL = "CENSUS_ICL";

    // Given
    ActionRequest actionRequest = ActionRequestBuilder.createICL_EnglandActionRequest(CensusICL);

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
    assertThat(message, containsString(CensusICL));
    System.out.println("Msg contains: " + CensusICL);
    String notificationFilePath = getLatestSftpFileName();

    InputStream inputSteam = defaultSftpSessionFactory.getSession().readRaw(notificationFilePath);
    defaultSftpSessionFactory.getSession();

    String fileLine = convertInputSteamToString(inputSteam);
    assertEquals("test-iac|caseRef|Prem1|line_2||postTown|postCode|null|PACK_CODE", fileLine);

    assertTrue(defaultSftpSessionFactory.getSession().remove(notificationFilePath));
  }

  public String convertInputSteamToString(InputStream inputStream) throws IOException {

    Charset charset = Charset.defaultCharset();

    try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, charset))) {
      return br.lines().collect(Collectors.joining(System.lineSeparator()));
    }
  }

  //  DO NOT DELETE, MAYBE REQUIRED
  //  @Test
  //  public void testMostRecentAddressUsedWhenDuplicateSampleUnitRefs() throws Exception {
  //    // Given
  //    final String CensusICL = "CENSUS_ICL";
  //    ActionInstruction firstActionInstruction =
  //        createActionInstruction(CensusICL, "Old Address", "exercise_1");
  //    ActionInstruction secondActionInstruction =
  //        createActionInstruction(CensusICL, "New Address", "exercise_2");
  //
  //    BlockingQueue<String> queue =
  //        simpleMessageListener.listen(
  //            SimpleMessageBase.ExchangeType.Fanout, "event-message-outbound-exchange");
  //
  //    simpleMessageSender.sendMessage(
  //        "action-outbound-exchange",
  //        "Action.Printer.binding",
  //        ActionRequestBuilder.actionInstructionToXmlString(firstActionInstruction));
  //
  //    // When
  //    String firstActionExportConfirmation = queue.take();
  //
  //    assertThat(firstActionExportConfirmation, containsString(CensusICL));
  //    String firstNotificationFilePath = getLatestSftpFileName();
  //    assertTrue(defaultSftpSessionFactory.getSession().remove(firstNotificationFilePath));
  //    defaultSftpSessionFactory.getSession().close();
  //
  //    simpleMessageSender.sendMessage(
  //        "action-outbound-exchange",
  //        "Action.Printer.binding",
  //        ActionRequestBuilder.actionInstructionToXmlString(secondActionInstruction));
  //
  //    String secondActionExportConfirmation = queue.take();
  //
  //    // Then
  //    assertThat(secondActionExportConfirmation, containsString(CensusICL));
  //    String secondNotificationFilePath = getLatestSftpFileName();
  //    InputStream inputSteam =
  //        defaultSftpSessionFactory.getSession().readRaw(secondNotificationFilePath);
  //
  //    try (Reader reader = new InputStreamReader(inputSteam);
  //        CSVParser parser = new CSVParser(reader, CSVFormat.newFormat('|'))) {
  //      Iterator<String> firstRowColumns = parser.iterator().next().iterator();
  //      assertEquals("New Address", firstRowColumns.next());
  //    } finally {
  //      // Delete the file created in this test
  //      assertTrue(defaultSftpSessionFactory.getSession().remove(secondNotificationFilePath));
  //    }
  //  }

  private void cleanSftpServer() throws IOException {
    //  don't know if this actually does anything

    String sftpPath = "Documents/sftp/";
    SftpSession session = defaultSftpSessionFactory.getSession();

    Comparator<ChannelSftp.LsEntry> sortByModifiedTimeDescending =
        (f1, f2) -> Integer.compare(f2.getAttrs().getMTime(), f1.getAttrs().getMTime());

    try {
      while (true) {
        ChannelSftp.LsEntry[] files = session.list(sftpPath);

        ChannelSftp.LsEntry latestFile =
            Arrays.stream(files)
                .filter(f -> f.getFilename().endsWith(".csv"))
                .min(sortByModifiedTimeDescending)
                .orElseThrow(() -> new RuntimeException("No file on SFTP"));

        String file_path = sftpPath + latestFile.getFilename();
        System.out.println("file path to remove: " + file_path);
        session.remove(file_path);
      }
    } catch (Exception ex) {
      System.out.println(ex.getMessage());
    }

    ChannelSftp.LsEntry[] files = session.list(sftpPath);

    Object[] blah = Arrays.stream(files).filter(f -> f.getFilename().endsWith(".csv")).toArray();

    System.out.println("Latest File Length list: " + blah.length);
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
