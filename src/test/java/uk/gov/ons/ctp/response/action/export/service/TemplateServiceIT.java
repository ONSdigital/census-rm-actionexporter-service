package uk.gov.ons.ctp.response.action.export.service;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import com.jcraft.jsch.ChannelSftp;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.assertj.core.api.Assertions;
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
import uk.gov.ons.ctp.response.action.export.domain.PrintFileMainfest;
import uk.gov.ons.ctp.response.action.export.domain.PrintFilesInfo;
import uk.gov.ons.ctp.response.action.export.utility.ActionRequestBuilder;
import uk.gov.ons.ctp.response.action.message.instruction.ActionInstruction;
import uk.gov.ons.ctp.response.action.message.instruction.ActionRequest;
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
  public static final String FULFILLMENT = "fulfillment";
  private static final String DOCUMENTS_SFTP = "Documents/sftp/fulfillment/";
  private static final int SFTP_FILE_RETRY_ATTEMPTS = 24;
  private static final int SFTP_FILE_SLEEP_SECONDS = 5;

  @Autowired private AppConfig appConfig;

  @Autowired private ObjectMapper objectMapper;

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

    removeAllFilesFromSftpServer();
  }

  @Test
  public void testTemplateGeneratesCorrectPrintFileForCensusICL() throws Exception {
    // Given
    ActionRequest actionRequest = ActionRequestBuilder.createICL_EnglandActionRequest(ICL1E);

    ActionInstruction actionInstruction = new ActionInstruction();
    actionInstruction.setActionRequest(actionRequest);

    simpleMessageSender.sendMessage(
        "action-outbound-exchange",
        "Action.Printer.binding",
        ActionRequestBuilder.actionInstructionToXmlString(actionInstruction));

    // When
    assertTrue("Expected files not created on sftp server", waitForSftpServerFileCount(2));

    // Then
    String printFilePath = getLatestPrintFile();
    String expectedPrintFileContents =
        "test-iac|caseRef|Address Line 1|line_2|line_3|postTown|postCode|P_IC_ICL1\n";
    String actualPrintFileContents = sftpFileToString(printFilePath);
    assertEquals(actualPrintFileContents, expectedPrintFileContents);

    String manifestFilePath = getLatestManifestFile();
    PrintFileMainfest expectedManifestFileContents =
        createExpectedManifestContents(actualPrintFileContents, printFilePath);
    PrintFileMainfest actualManifestFileContents = readMainfestFileContentsToJson(manifestFilePath);
    Assertions.assertThat(expectedManifestFileContents)
        .isEqualToIgnoringGivenFields(actualManifestFileContents, "manifestCreated");

    String printFileName = FilenameUtils.removeExtension(FilenameUtils.getName(printFilePath));
    String manifestFileName =
        FilenameUtils.removeExtension(FilenameUtils.getName(manifestFilePath));
    assertEquals(manifestFileName, printFileName);

    int printFileDate = getSftpFileLastModifiedDate(printFilePath);
    int manifestFileDate = getSftpFileLastModifiedDate(manifestFilePath);
    Assertions.assertThat(manifestFileDate).isGreaterThanOrEqualTo(printFileDate);

    assertTrue(deleteFile(printFilePath));
    assertTrue(deleteFile(manifestFilePath));
  }

  @Test
  public void testMostRecentAddressUsedWhenDuplicateSampleUnitRefs() throws Exception {
    // Given
    ActionInstruction firstActionInstruction =
        createActionInstruction(ICL1E, "Old Address", "exercise_1");
    ActionInstruction secondActionInstruction =
        createActionInstruction(ICL1E, "New Address", "exercise_2");

    simpleMessageSender.sendMessage(
        "action-outbound-exchange",
        "Action.Printer.binding",
        ActionRequestBuilder.actionInstructionToXmlString(firstActionInstruction));

    // When
    assertTrue("Expected files not created on sftp server", waitForSftpServerFileCount(2));

    assertTrue(deleteFile(getLatestPrintFile()));
    assertTrue(deleteFile(getLatestManifestFile()));

    simpleMessageSender.sendMessage(
        "action-outbound-exchange",
        "Action.Printer.binding",
        ActionRequestBuilder.actionInstructionToXmlString(secondActionInstruction));

    assertTrue("Expected files not created on sftp server", waitForSftpServerFileCount(2));

    // Then
    String printFileLine = readFileContentsToString(getLatestPrintFile());
    assertEquals(
        "test-iac|caseRef|New Address|line_2|line_3|postTown|postCode|P_IC_ICL1", printFileLine);

    assertTrue(deleteFile(getLatestPrintFile()));
    assertTrue(deleteFile(getLatestManifestFile()));
  }

  private PrintFileMainfest readMainfestFileContentsToJson(String fileName) throws IOException {
    return new ObjectMapper()
        .readValue(readFileContentsToString(fileName), PrintFileMainfest.class);
  }

  private String readFileContentsToString(String fileName) throws IOException {
    return convertInputSteamToString(defaultSftpSessionFactory.getSession().readRaw(fileName));
  }

  public String convertInputSteamToString(InputStream inputStream) throws IOException {
    try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
      return br.lines().collect(Collectors.joining(System.lineSeparator()));
    }
  }

  private String getLatestPrintFile() throws IOException {
    return getLatestSftpFileName(".csv");
  }

  private String getLatestManifestFile() throws IOException {
    return getLatestSftpFileName(".manifest");
  }

  private String getLatestSftpFileName(String fileType) throws IOException {
    Comparator<ChannelSftp.LsEntry> sortByModifiedTimeDescending =
        (f1, f2) -> Integer.compare(f2.getAttrs().getMTime(), f1.getAttrs().getMTime());

    String sftpPath = "Documents/sftp/fulfillment/";
    ChannelSftp.LsEntry[] sftpList = defaultSftpSessionFactory.getSession().list(sftpPath);

    System.out.println("Latest File Length list: " + sftpList.length);

    ChannelSftp.LsEntry latestFile =
        Arrays.stream(sftpList)
            .filter(f -> f.getFilename().endsWith(fileType))
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

  private boolean deleteFile(String fileName) throws IOException {
    return defaultSftpSessionFactory.getSession().remove(fileName);
  }

  private long getSftpFileSize(String printFile) throws IOException {
    return defaultSftpSessionFactory.getSession().list(printFile)[0].getAttrs().getSize();
  }

  private int getSftpFileLastModifiedDate(String printFile) throws IOException {
    return defaultSftpSessionFactory.getSession().list(printFile)[0].getAttrs().getMTime();
  }

  private PrintFileMainfest createExpectedManifestContents(String fileLines, String filePath)
      throws IOException {
    String filename = FilenameUtils.getName(filePath);
    String checksum = DigestUtils.md5Hex(fileLines);
    long printFileSize = getSftpFileSize(filePath);

    PrintFilesInfo printFilesInfo = new PrintFilesInfo(printFileSize, checksum, ".\\", filename);

    List<PrintFilesInfo> files = Arrays.asList(printFilesInfo);

    String manifestCreatedDateTime = DateTimeFormatter.ISO_INSTANT.format(Instant.now());

    return new PrintFileMainfest(
        1,
        files,
        "ONS_RM",
        manifestCreatedDateTime,
        "Initial contact letter households - England",
        "PPD1.1",
        1);
  }

  private String sftpFileToString(String filePath) throws IOException {
    InputStream inputSteam = defaultSftpSessionFactory.getSession().readRaw(filePath);
    // Does this need to be here?
    defaultSftpSessionFactory.getSession();

    return IOUtils.toString(inputSteam);
  }

  private void removeAllFilesFromSftpServer() throws IOException {
    String sftpPath = DOCUMENTS_SFTP;

    long deletedCount =
        Arrays.stream(defaultSftpSessionFactory.getSession().list(sftpPath))
            .filter(f -> f.getFilename().endsWith(".csv") || f.getFilename().endsWith(".manifest"))
            .peek(
                f -> {
                  String filetoDeletePath = sftpPath + f.getFilename();

                  try {
                    defaultSftpSessionFactory.getSession().remove(filetoDeletePath);
                  } catch (IOException e) {
                    System.out.println(
                        "Non Fatal Error, Failed to delete file: " + filetoDeletePath);
                  }
                })
            .count();

    log.info("Deleted: " + deletedCount + " files");
  }

  private boolean waitForSftpServerFileCount(int expectedFileCount)
      throws IOException, InterruptedException {

    int sleepDuration = SFTP_FILE_SLEEP_SECONDS * 1000;

    log.with("Expected File Count", expectedFileCount)
        .with("Maximum attempts", SFTP_FILE_RETRY_ATTEMPTS)
        .debug("Checking for SFTP file(s)");

    // Check every 5 sec up upto 2 mins
    for (int i = 0; i < SFTP_FILE_RETRY_ATTEMPTS; i++) {

      log.with("attempt", i + 1).debug("Retrying...");

      Thread.sleep(sleepDuration);

      long fileCount =
          Arrays.stream(defaultSftpSessionFactory.getSession().list(DOCUMENTS_SFTP))
              .filter(
                  f -> f.getFilename().endsWith(".csv") || f.getFilename().endsWith(".manifest"))
              .count();

      if (fileCount == expectedFileCount) {
        return true;
      }
    }

    return false;
  }
}
