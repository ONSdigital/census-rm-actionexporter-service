package uk.gov.ons.ctp.response.action.export.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.UUID;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.ons.ctp.response.action.export.config.AppConfig;
import uk.gov.ons.ctp.response.action.export.config.Sftp;
import uk.gov.ons.ctp.response.action.export.domain.ExportFile;
import uk.gov.ons.ctp.response.action.export.domain.ExportFile.SendStatus;
import uk.gov.ons.ctp.response.action.export.domain.ExportJob;
import uk.gov.ons.ctp.response.action.export.message.EventPublisher;
import uk.gov.ons.ctp.response.action.export.message.SftpServicePublisher;
import uk.gov.ons.ctp.response.action.export.repository.ExportFileRepository;

@RunWith(MockitoJUnitRunner.class)
public class NotificationAndManifestFileCreatorTest {

  private static final DateTimeFormatter dateTimeFormatter =
      DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH-mm-ss");

  @Mock private Clock clock;
  @Mock private SftpServicePublisher sftpService;
  @Mock private EventPublisher eventPublisher;
  @Mock private ExportFileRepository exportFileRepository;
  @Mock private AppConfig appConfig;
  @Mock private ManifestBuilder manifestBuilder;
  @InjectMocks private NotificationAndManifestFileCreator notificationAndManifestFileCreator;

  @Test
  public void shouldCreateTheCorrectFilename() {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    ByteArrayOutputStream manifestFileBos = new ByteArrayOutputStream();
    ExportJob exportJob = new ExportJob(UUID.randomUUID());
    String[] responseRequiredList = {"123", "ABC", "FOO", "BAR"};
    Date now = new Date();
    Sftp mockSftp = mock(Sftp.class);
    String directory = "Documents/sftp/";

    when(exportFileRepository.existsByFilename(anyString())).thenReturn(false).thenReturn(false);
    when(manifestBuilder.createManifestData(any(String.class), any())).thenReturn(manifestFileBos);

    // Given
    given(clock.millis()).willReturn(now.getTime());
    given(appConfig.getSftp()).willReturn(mockSftp);
    given(mockSftp.getDirectory()).willReturn(directory);

    LocalDateTime testStartTime = LocalDateTime.now().withNano(0);

    // When
    notificationAndManifestFileCreator.uploadData(
        "DIRSUFFIX", "TESTFILENAMEPREFIX", bos, exportJob, responseRequiredList, 666);

    // Then
    LocalDateTime testEndTest = LocalDateTime.now().withNano(0);

    ArgumentCaptor<ExportFile> exportFileArgumentCaptor = ArgumentCaptor.forClass(ExportFile.class);

    verify(exportFileRepository, times(2)).saveAndFlush(exportFileArgumentCaptor.capture());

    ExportFile csvFileWriteCall = exportFileArgumentCaptor.getAllValues().get(0);
    String csvActualFileName = csvFileWriteCall.getFilename();
    LocalDateTime csvActualDateTime =
        LocalDateTime.parse(csvActualFileName.substring(19, 38), dateTimeFormatter);

    assertThat(csvActualFileName.matches("^TESTFILENAMEPREFIX.*csv$"));
    assertThat(csvActualDateTime).isBetween(testStartTime, testEndTest);
    verify(exportFileRepository).saveAndFlush(csvFileWriteCall);
    assertThat(csvFileWriteCall.getExportJobId()).isEqualTo(exportJob.getId());
    assertThat(csvFileWriteCall.getStatus()).isEqualTo(SendStatus.QUEUED);

    ExportFile manifestFileWriteCall = exportFileArgumentCaptor.getAllValues().get(1);
    String manifestActualFileName = manifestFileWriteCall.getFilename();
    LocalDateTime manifestActualDateTime =
        LocalDateTime.parse(manifestActualFileName.substring(19, 38), dateTimeFormatter);

    assertThat(manifestActualFileName.matches("^TESTFILENAMEPREFIX.*manifest$"));
    assertThat(manifestActualDateTime).isBetween(testStartTime, testEndTest);
    verify(exportFileRepository).saveAndFlush(manifestFileWriteCall);
    assertThat(manifestFileWriteCall.getExportJobId()).isEqualTo(exportJob.getId());
    assertThat(manifestFileWriteCall.getStatus()).isEqualTo(SendStatus.QUEUED);

    verify(sftpService)
        .sendMessage(
            eq("Documents/sftp/DIRSUFFIX/"),
            eq(csvActualFileName),
            eq(responseRequiredList),
            eq("666"),
            eq(bos));

    verify(eventPublisher).publishEvent(eq("Printed file " + csvActualFileName));
  }

  @Test
  public void shouldThrowExceptionForDuplicateFilename() {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    ExportJob exportJob = new ExportJob(UUID.randomUUID());
    String[] responseRequiredList = {"123", "ABC", "FOO", "BAR"};
    Date now = new Date();
    boolean expectedExceptionThrown = false;

    // Given
    given(clock.millis()).willReturn(now.getTime());
    given(exportFileRepository.existsByFilename(any())).willReturn(true);

    // When
    try {
      notificationAndManifestFileCreator.uploadData(
          "DIRSUFFIX", "TESTFILENAMEPREFIX", bos, exportJob, responseRequiredList, 666);
    } catch (RuntimeException ex) {
      expectedExceptionThrown = true;
    }

    // Then
    assertThat(expectedExceptionThrown).isTrue();
    verify(exportFileRepository, never()).saveAndFlush(any());
    verify(sftpService, never()).sendMessage(any(), any(), any(), any(), any());
    verify(eventPublisher, never()).publishEvent(any());
  }
}
