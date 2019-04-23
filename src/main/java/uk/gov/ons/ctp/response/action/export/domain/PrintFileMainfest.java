package uk.gov.ons.ctp.response.action.export.domain;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PrintFileMainfest {
  private int schemaVersion;
  private List<PrintFilesInfo> files;
  private String sourceName;
  private String manifestCreated;

  private String description;
  private String dataset;
  private int version;
}
