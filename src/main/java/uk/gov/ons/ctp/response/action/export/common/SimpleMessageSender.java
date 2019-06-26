package uk.gov.ons.ctp.response.action.export.common;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import uk.gov.ons.ctp.response.action.export.config.Rabbitmq;

public class SimpleMessageSender extends SimpleMessageBase {
  public SimpleMessageSender(String host, int port, String username, String password) {
    super(host, port, username, password);
  }

  SimpleMessageSender() {}

  public SimpleMessageSender(Rabbitmq rabbitmq) {
    super(rabbitmq);
  }

  public void sendMessage(String exchange, String routingKey, String message) {
    RabbitTemplate rabbitTemplate = this.getRabbitTemplate();
    rabbitTemplate.convertAndSend(exchange, routingKey, message);
  }

  public void sendMessage(String exchange, String message) {
    RabbitTemplate rabbitTemplate = this.getRabbitTemplate();
    rabbitTemplate.convertAndSend(exchange, message);
  }

  public void sendMessageToQueue(String queueName, String message) {
    RabbitTemplate rabbitTemplate = this.getRabbitTemplate();
    rabbitTemplate.convertAndSend(queueName, message);
  }
}
