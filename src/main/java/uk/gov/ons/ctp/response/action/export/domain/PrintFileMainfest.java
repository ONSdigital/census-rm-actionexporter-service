package uk.gov.ons.ctp.response.action.export.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class PrintFileMainfest {
    private double schemaVersion;
    private List<PrintFilesInfo> files;
    private String sourceName;
    private LocalDateTime manifestCreated;
    private String description;
    private String dataset;
    private double version;
}