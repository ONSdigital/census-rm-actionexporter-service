package uk.gov.ons.ctp.response.action.export.service;

import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.time.Clock;
import org.springframework.stereotype.Service;
import uk.gov.ons.ctp.response.action.export.config.AppConfig;
import uk.gov.ons.ctp.response.action.export.domain.ExportFile;
import uk.gov.ons.ctp.response.action.export.domain.ExportJob;
import uk.gov.ons.ctp.response.action.export.message.EventPublisher;
import uk.gov.ons.ctp.response.action.export.message.SftpServicePublisher;
import uk.gov.ons.ctp.response.action.export.repository.ExportFileRepository;

@Service
public class NotificationAndManifestFileCreator {

  private static final Logger log =
      LoggerFactory.getLogger(NotificationAndManifestFileCreator.class);

  private static final SimpleDateFormat FILENAME_DATE_FORMAT =
      new SimpleDateFormat("yyyy-MM-dd'T'HH-mm-ss");

  private final SftpServicePublisher sftpService;

  private final EventPublisher eventPublisher;

  private final ExportFileRepository exportFileRepository;
  private final ManifestBuilder manifestBuilder;

  private final Clock clock;

  private final AppConfig appConfig;

  public NotificationAndManifestFileCreator(
      SftpServicePublisher sftpService,
      EventPublisher eventPublisher,
      ExportFileRepository exportFileRepository,
      Clock clock,
      AppConfig appConfig,
      ManifestBuilder manifestBuilder) {
    this.sftpService = sftpService;
    this.eventPublisher = eventPublisher;
    this.exportFileRepository = exportFileRepository;
    this.clock = clock;
    this.appConfig = appConfig;
    this.manifestBuilder = manifestBuilder;
  }

  public void uploadData(
      String directorySuffix,
      String filenamePrefix,
      ByteArrayOutputStream data,
      ExportJob exportJob,
      String[] responseRequiredList,
      int actionCount) {
    if (actionCount == 0) {
      return;
    }

    final String now = FILENAME_DATE_FORMAT.format(clock.millis());
    String csvFilename = String.format("%s_%s.csv", filenamePrefix, now);
    String manifestFilename = csvFilename.replace(".csv", ".manifest");

    // Fudge for transactional management of rolling back SI's immediate sftp file commit
    if (exportFileRepository.existsByFilename(csvFilename)
        || exportFileRepository.existsByFilename(manifestFilename)) {
      log.with("filename", csvFilename)
          .warn(
              "Duplicate filename. The cron job is probably running too frequently. The "
                  + "Action Exporter service is designed to only run every minute, maximum");
      throw new RuntimeException();
    }

    String directory = appConfig.getSftp().getDirectory();
    if (directorySuffix != null) {
      directory = directory.concat(directorySuffix).concat("/");
    }

    writeFileToSftpAndRecordOnDB(
        directory, csvFilename, exportJob, data, responseRequiredList, actionCount);

    writeFileToSftpAndRecordOnDB(
        directory,
        manifestFilename,
        exportJob,
        manifestBuilder.createManifestData(csvFilename, data),
        new String[0],
        1);
  }

  private void writeFileToSftpAndRecordOnDB(
      String directory,
      String filename,
      ExportJob exportJob,
      ByteArrayOutputStream data,
      String[] responseRequiredList,
      int actionCount) {

    log.with("filename", filename).info("Uploading file");
    ExportFile exportFile = new ExportFile();
    exportFile.setExportJobId(exportJob.getId());
    exportFile.setFilename(filename);
    exportFileRepository.saveAndFlush(exportFile);

    sftpService.sendMessage(
        directory, filename, responseRequiredList, Integer.toString(actionCount), data);
    eventPublisher.publishEvent("Printed file " + filename);
  }
}
