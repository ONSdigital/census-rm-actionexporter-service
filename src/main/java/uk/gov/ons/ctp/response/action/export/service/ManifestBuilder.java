package uk.gov.ons.ctp.response.action.export.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.springframework.stereotype.Component;
import uk.gov.ons.ctp.response.action.export.domain.PrintFileMainfest;
import uk.gov.ons.ctp.response.action.export.domain.PrintFilesInfo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class ManifestBuilder {
    private final ObjectMapper objectMapper;

    public ManifestBuilder(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String getManifestFileName(String csvFileName) {
        return csvFileName.replace(".csv", ".manifest");
    }

    public ByteArrayOutputStream createManifestData(String filename, ByteArrayOutputStream data) {
        PrintFileMainfest printFileMainfest = createManifest(filename, data);

        try {
            String jsonManifest = objectMapper.writeValueAsString(printFileMainfest);
            byte[] manifestBytes = jsonManifest.getBytes();
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(manifestBytes.length);
            byteArrayOutputStream.write(manifestBytes);

            return byteArrayOutputStream;
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Must handle this, throwing up equals pain.");
            throw new RuntimeException(e.getMessage());
        }
    }

    private PrintFileMainfest createManifest(String filename, ByteArrayOutputStream data) {
        String checksum = DigestUtils.md5Hex(data.toByteArray());

        PrintFilesInfo printFilesInfo = new PrintFilesInfo(data.size(), checksum, ".\\", filename);
        List<PrintFilesInfo> files = new ArrayList<>(Arrays.asList(printFilesInfo));

        String manifestCreatedDateTime = DateTimeFormatter.ISO_INSTANT.format(Instant.now());

        // These are hardcoded for now, they will be update and passed in from the actionsvc in the future
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
