package uk.gov.ons.ctp.response.action.export.message;

import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.integration.annotation.MessageEndpoint;
import uk.gov.ons.ctp.response.action.message.feedback.ActionFeedback;

/**
 * Service implementation responsible for publishing an action feedback message to the action
 * service.
 */
@MessageEndpoint
public class ActionFeedbackPublisher {
  private static final Logger log = LoggerFactory.getLogger(ActionFeedbackPublisher.class);

  @Qualifier("actionFeedbackRabbitTemplate")
  @Autowired
  private RabbitTemplate rabbitTemplate;

  /**
   * To publish an ActionFeedback message
   *
   * @param actionFeedback the ActionFeedback to publish.
   */
  public void sendActionFeedback(ActionFeedback actionFeedback) {
    log.with("action_id", actionFeedback.getActionId())
        .debug("Entering sendActionFeedback for actionId ");
    rabbitTemplate.convertAndSend(actionFeedback);
  }
}
