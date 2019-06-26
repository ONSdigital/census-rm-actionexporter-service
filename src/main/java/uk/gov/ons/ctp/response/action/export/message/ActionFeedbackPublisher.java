package uk.gov.ons.ctp.response.action.export.message;

import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import org.springframework.integration.annotation.MessageEndpoint;
import uk.gov.ons.ctp.response.action.export.actioninstuction.ActionFeedback;

/**
 * Service implementation responsible for publishing an action feedback message to the action
 * service.
 */
// TODO:remove requirement for this

@MessageEndpoint
public class ActionFeedbackPublisher {
  private static final Logger log = LoggerFactory.getLogger(ActionFeedbackPublisher.class);

  public void sendActionFeedback(ActionFeedback actionFeedback) {
    log.error("Don't call this");
  }
}
