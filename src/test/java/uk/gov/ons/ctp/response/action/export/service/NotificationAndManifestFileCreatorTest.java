package uk.gov.ons.ctp.response.action.export.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.time.Clock;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.ons.ctp.response.action.export.domain.ExportFile;
import uk.gov.ons.ctp.response.action.export.domain.ExportFile.SendStatus;
import uk.gov.ons.ctp.response.action.export.domain.ExportJob;
import uk.gov.ons.ctp.response.action.export.message.EventPublisher;
import uk.gov.ons.ctp.response.action.export.message.SftpServicePublisher;
import uk.gov.ons.ctp.response.action.export.repository.ActionRequestRepository;
import uk.gov.ons.ctp.response.action.export.repository.ExportFileRepository;

@RunWith(MockitoJUnitRunner.class)
public class NotificationAndManifestFileCreatorTest {

  private static final SimpleDateFormat FILENAME_DATE_FORMAT =
      new SimpleDateFormat("ddMMyyyy_HHmmss");

  @Mock private Clock clock;
  @Mock private ActionRequestRepository actionRequestRepository;
  @Mock private SftpServicePublisher sftpService;
  @Mock private EventPublisher eventPublisher;
  @Mock private ExportFileRepository exportFileRepository;
  @Mock private ManifestBuilder manifestBuilder;

  private NotificationAndManifestFileCreator notificationAndManifestFileCreator;

  @Test
  public void shouldCreateTheCorrectFilename() {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    ByteArrayOutputStream manifestFileBos = new ByteArrayOutputStream();
    ExportJob exportJob = new ExportJob(UUID.randomUUID());
    String[] responseRequiredList = {"123", "ABC", "FOO", "BAR"};
    Date now = new Date();

    // Given
    given(clock.millis()).willReturn(now.getTime());

    String expectedCsvFilename =
            String.format("TESTFILENAMEPREFIX_%s.csv", FILENAME_DATE_FORMAT.format(now));

    String expectedManifestFileName = expectedCsvFilename.replace(".csv", ".manifest");

    //Don't like this, but don't know the exact filename either as time dependent...
    when(manifestBuilder.getManifestFileName(expectedCsvFilename)).thenReturn(expectedManifestFileName);
    when(manifestBuilder.createManifestData(any(String.class), any())).thenReturn(manifestFileBos);

    notificationAndManifestFileCreator = new NotificationAndManifestFileCreator( sftpService,
            eventPublisher,
            exportFileRepository,
            clock,
            manifestBuilder);

    // When
    notificationAndManifestFileCreator.uploadData(
        "TESTFILENAMEPREFIX", bos, exportJob, responseRequiredList, 666);

    // Then

    ArgumentCaptor<ExportFile> exportFileArgumentCaptor = ArgumentCaptor.forClass(ExportFile.class);

    verify(exportFileRepository, times(2)).saveAndFlush(exportFileArgumentCaptor.capture());

    ExportFile csvFileWriteCall = exportFileArgumentCaptor.getAllValues().get(0);
    verify(exportFileRepository).saveAndFlush(csvFileWriteCall);
    assertThat(csvFileWriteCall.getFilename()).isEqualTo(expectedCsvFilename);
    assertThat(csvFileWriteCall.getExportJobId()).isEqualTo(exportJob.getId());
    assertThat(csvFileWriteCall.getStatus()).isEqualTo(SendStatus.QUEUED);

    ExportFile manifestFileWriteCall = exportFileArgumentCaptor.getAllValues().get(1);
    verify(exportFileRepository).saveAndFlush(manifestFileWriteCall);
    assertThat(manifestFileWriteCall.getFilename()).isEqualTo(expectedManifestFileName);
    assertThat(manifestFileWriteCall.getExportJobId()).isEqualTo(exportJob.getId());
    assertThat(manifestFileWriteCall.getStatus()).isEqualTo(SendStatus.QUEUED);


    verify(sftpService)
        .sendMessage(eq(expectedCsvFilename), eq(responseRequiredList), eq("666"), eq(bos));

    verify(eventPublisher).publishEvent(eq("Printed file " + expectedCsvFilename));
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
          "TESTFILENAMEPREFIX", bos, exportJob, responseRequiredList, 666);
    } catch (RuntimeException ex) {
      expectedExceptionThrown = true;
    }

    // Then
    assertThat(expectedExceptionThrown).isTrue();
    verify(exportFileRepository, never()).saveAndFlush(any());
    verify(sftpService, never()).sendMessage(any(), any(), any(), any());
    verify(eventPublisher, never()).publishEvent(any());
  }
}
