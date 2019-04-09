package uk.gov.ons.ctp.response.action.export.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import com.jcraft.jsch.ChannelSftp;
import net.logstash.logback.encoder.org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Assert;
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
import uk.gov.ons.tools.rabbit.SimpleMessageBase;
import uk.gov.ons.tools.rabbit.SimpleMessageListener;
import uk.gov.ons.tools.rabbit.SimpleMessageSender;

import javax.persistence.criteria.CriteriaBuilder;
import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.stream.Stream;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;

//import org.apache.tomcat.jni.File;

@RunWith(SpringRunner.class)
@ContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class TemplateServiceIT {
    private static final Logger log = LoggerFactory.getLogger(TemplateServiceIT.class);
    public static final String ICL1E = "ICL1E";
    public static final String P_IC_ICL_1 = "P_IC_ICL1";
    public static final String DOCUMENTS_SFTP = "Documents/sftp/";

    @Autowired
    private AppConfig appConfig;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DefaultSftpSessionFactory defaultSftpSessionFactory;

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

        InputStream inputStream = defaultSftpSessionFactory.getSession().readRaw(notificationFilePath);
        defaultSftpSessionFactory.getSession();

        String fileLines = IOUtils.toString(inputStream);
        assertEquals("test-iac|caseRef|Address Line 1|line_2|line_3|postTown|postCode|P_IC_ICL1",
                fileLines.trim());

        PrintFileMainfest expectedManifest = createExpectedManifest(fileLines, notificationFilePath);
        PrintFileMainfest actualManifest = getActualPrintFileManifest(notificationFilePath);

        long secondsDifference = getSecondsDifferenceBetweenManifests(expectedManifest.getManifestCreated(),
                actualManifest.getManifestCreated());
        assertTrue("Expected createdManifest must be after actualManifest created time",
                secondsDifference > 0);
        assertTrue("Expected date of: " + expectedManifest.getManifestCreated()
                        + " should be within 60 seconds of actual manifest created date: "
                        + actualManifest.getManifestCreated(),
                secondsDifference < 60);

        //This stops us having to ignore the field, which feels a bit cack, can junit handle this
        actualManifest.setManifestCreated(expectedManifest.getManifestCreated());
        Assert.assertTrue(EqualsBuilder.reflectionEquals(expectedManifest, actualManifest));
    }

    private PrintFileMainfest createExpectedManifest(String fileLines, String filePath) throws IOException {
        long bytesLength = fileLines.getBytes().length;
        String filename = FilenameUtils.getName(filePath);
        String checksum = DigestUtils.md5Hex(fileLines);
        PrintFilesInfo printFilesInfo = new PrintFilesInfo(bytesLength, checksum, "./", filename);

        List<PrintFilesInfo> files = new ArrayList<>(Arrays.asList(printFilesInfo));

        String manifestCreatedDateTime = DateTimeFormatter.ISO_INSTANT.format(Instant.now());

        return new PrintFileMainfest(1, files, "ONS_RM",
                manifestCreatedDateTime, "Initial contact letter households - England",
                "PPD1.1", 1);
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

        String fileLine = IOUtils.toString(inputSteam).trim();
        assertEquals(
                "test-iac|caseRef|New Address|line_2|line_3|postTown|postCode|P_IC_ICL1", fileLine);

        assertTrue(defaultSftpSessionFactory.getSession().remove(secondNotificationFilePath));
    }

    @Test
    public void testIncaseWhereManifestFileWriteFailsThatAllIsTransactional() {
        // No idea how to test for this, but should be some transactional test
        // Ideally we want to allow it to get as far as writing the csv file, but fail on the manifest file
        // But then fix it..  Should be nice and easy...
        // We could noble the exportFileRepository, Get it and hack it for this test (ok in a different file)
        // So when we check the filename for duplicate it fails for the first manifest file write.
        // Then allow it to work next time, and all should be good?

        // Anyway this is a holder for now
    }

    private long getSecondsDifferenceBetweenManifests(String expectedCreatedDateTime, String actualCreatedDateTime) {
        long expectedEpoch = Instant.parse(expectedCreatedDateTime).getEpochSecond();
        long actualEpoch = Instant.parse(actualCreatedDateTime).getEpochSecond();

        return expectedEpoch - actualEpoch;
    }

    private PrintFileMainfest getActualPrintFileManifest(String csvFilePath) throws IOException {
        String expectedManifestFileName = createExpectedManifestFileName(csvFilePath);
        String actionManifestFileContents = sftpFileToString(expectedManifestFileName);

        return objectMapper.readValue(actionManifestFileContents, PrintFileMainfest.class);
    }

    private String sftpFileToString(String filePath) throws IOException {
        InputStream inputSteam = defaultSftpSessionFactory.getSession().readRaw(filePath);
        //Does this need to be here?
        defaultSftpSessionFactory.getSession();

        return IOUtils.toString(inputSteam);
    }

    private String createExpectedManifestFileName(String notificationFilePath) {
        //Feels like this should be more elegant
        return notificationFilePath.replace(".csv", ".manifest");
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

    private void removeAllFilesFromSftpServer() throws IOException {
        String sftpPath = DOCUMENTS_SFTP;

        Arrays.stream(defaultSftpSessionFactory.getSession().list(sftpPath))
                .filter(f -> f.getFilename().endsWith(".csv") || f.getFilename().endsWith(".manifest"))
                .peek(f -> {
                    String filetoDeletePath = sftpPath + f.getFilename();

                    try {
                        defaultSftpSessionFactory.getSession().remove(filetoDeletePath);
                    } catch (IOException e) {
                        System.out.println("Non Fatal Error, Failed to delete file: " + filetoDeletePath);
                    }
                }).toArray();
    }

}
