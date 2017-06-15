package uk.gov.ons.ctp.response.action.export.domain;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Representation of a message being sent.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@Slf4j
public class ExportMessage {

  private Map<String, List<UUID>> actionRequestIds = new HashMap<String, List<UUID>>();
  private Map<String, ByteArrayOutputStream> outputStreams = new HashMap<String, ByteArrayOutputStream>();

  /**
   * Return actionIds for actionRequests for key.
   *
   * @param key for which to get actionIds.
   * @return List of actionIds for key.
   */
  public List<UUID> getActionRequestIds(String key) {
    return actionRequestIds.get(key);
  }

  /**
   * Checks if list are empty
   * @return boolean
   */
  public boolean isEmpty() {
    if (actionRequestIds.isEmpty() || outputStreams.isEmpty()) {
      return true;
    } else {
      return false;
    }
  }

  /**
   * Return all actionIds.
   * @return List of all actionIds.
   */
  public List<UUID> getMergedActionRequestIds() {
    List<UUID> actionIds = new ArrayList<UUID>();
    actionRequestIds.forEach((key, mergeIds) -> {
      actionIds.addAll(mergeIds);
    });
    return actionIds;
  }

  /**
   * Return all outputStreams merged.
   * @return ByteArrayOutputStream.
   */
  public ByteArrayOutputStream getMergedOutputStreams() {

    ByteArrayOutputStream mergedStream = new ByteArrayOutputStream();
    for (Map.Entry<String, ByteArrayOutputStream> outputStream : outputStreams.entrySet()) {
      try {
        mergedStream.write(outputStream.getValue().toByteArray());
      } catch (IOException ex) {
        log.error("Error merging ExportMessage ByteArrayOutputStreams: {}", ex.getMessage());
        return null;
      }
    }
    return mergedStream;
  }
}
