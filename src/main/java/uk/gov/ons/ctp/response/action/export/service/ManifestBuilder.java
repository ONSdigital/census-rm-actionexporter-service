package uk.gov.ons.ctp.response.action.export.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Component;
import uk.gov.ons.ctp.response.action.export.domain.PrintFileInfo;
import uk.gov.ons.ctp.response.action.export.domain.PrintFileMainfest;

@Component
public class ManifestBuilder {
  private final ObjectMapper objectMapper;

  public ManifestBuilder(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  public ByteArrayOutputStream createManifestData(String filename, ByteArrayOutputStream data)
      throws IOException {
    PrintFileMainfest printFileMainfest = createManifest(filename, data);

    String jsonManifest = objectMapper.writeValueAsString(printFileMainfest);
    byte[] manifestBytes = jsonManifest.getBytes();
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(manifestBytes.length);
    byteArrayOutputStream.write(manifestBytes);

    return byteArrayOutputStream;
  }

  private PrintFileMainfest createManifest(String filename, ByteArrayOutputStream data) {
    String checksum = DigestUtils.md5Hex(data.toByteArray());

    PrintFileInfo printFileInfo = new PrintFileInfo(data.size(), checksum, ".\\", filename);
    List<PrintFileInfo> files = new ArrayList<>(Arrays.asList(printFileInfo));

    String manifestCreatedDateTime = DateTimeFormatter.ISO_INSTANT.format(Instant.now());

    // These are hardcoded for now, they will be update and passed in from the actionsvc in the
    // future
    return new PrintFileMainfest(
        1,
        files,
        "ONS_RM",
        manifestCreatedDateTime,
        "Initial contact letter households - England",
        "PPD1.1",
        1);
  }
}
