package uk.gov.ons.ctp.response.action.export.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Clock;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.springframework.stereotype.Service;
import uk.gov.ons.ctp.response.action.export.domain.ExportFile;
import uk.gov.ons.ctp.response.action.export.domain.ExportJob;
import uk.gov.ons.ctp.response.action.export.domain.PrintFileMainfest;
import uk.gov.ons.ctp.response.action.export.domain.PrintFilesInfo;
import uk.gov.ons.ctp.response.action.export.message.EventPublisher;
import uk.gov.ons.ctp.response.action.export.message.SftpServicePublisher;
import uk.gov.ons.ctp.response.action.export.repository.ExportFileRepository;

@Service
public class NotificationAndManifestFileCreator {
  private static final Logger log =
      LoggerFactory.getLogger(NotificationAndManifestFileCreator.class);
  private static final SimpleDateFormat FILENAME_DATE_FORMAT =
      new SimpleDateFormat("ddMMyyyy_HHmmss");
  private final SftpServicePublisher sftpService;
  private final EventPublisher eventPublisher;
  private final ExportFileRepository exportFileRepository;
  private final Clock clock;
  private final ManifestBuilder manifestBuilder;

  public NotificationAndManifestFileCreator(
      SftpServicePublisher sftpService,
      EventPublisher eventPublisher,
      ExportFileRepository exportFileRepository,
      Clock clock,
      ManifestBuilder manifestBuilder) {
    this.sftpService = sftpService;
    this.eventPublisher = eventPublisher;
    this.exportFileRepository = exportFileRepository;
    this.clock = clock;
    this.manifestBuilder = manifestBuilder;
  }

  public void uploadData(
      String filenamePrefix,
      ByteArrayOutputStream data,
      ExportJob exportJob,
      String[] responseRequiredList,
      int actionCount) {

    if (actionCount == 0) {
      return;
    }

    final String now = FILENAME_DATE_FORMAT.format(clock.millis());
    String csvfilename = String.format("%s_%s.csv", filenamePrefix, now);
    writeFileToSftpAndRecordOnDB(csvfilename, exportJob, data, responseRequiredList, actionCount);

    writeFileToSftpAndRecordOnDB(
       manifestBuilder.getManifestFileName(csvfilename),
            exportJob,
            manifestBuilder.createManifestData(csvfilename, data),
            new String [0], 1);
  }

  private void writeFileToSftpAndRecordOnDB(
      String filename,
      ExportJob exportJob,
      ByteArrayOutputStream data,
      String[] responseRequiredList,
      int actionCount) {
    if (exportFileRepository.existsByFilename(filename)) {
      log.with("filename", filename)
          .warn(
              "Duplicate filename. The cron job is probably running too frequently. The "
                  + "Action Exporter service is designed to only run every minute, maximum");
      throw new RuntimeException();
    }

    log.with("filename", filename).info("Uploading file");
    ExportFile exportFile = new ExportFile();
    exportFile.setExportJobId(exportJob.getId());
    exportFile.setFilename(filename);
    exportFileRepository.saveAndFlush(exportFile);

    sftpService.sendMessage(filename, responseRequiredList, Integer.toString(actionCount), data);
    eventPublisher.publishEvent("Printed file " + filename);
  }
}
