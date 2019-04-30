package uk.gov.ons.ctp.response.action.export.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PrintFileInfo {
  private long sizeBytes;
  private String md5sum;
  private String relativePath;
  private String name;
}
