package uk.gov.ons.ctp.response.action.export.common;

import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import uk.gov.ons.ctp.response.action.export.config.Rabbitmq;

public class SimpleMessageBase {
  private ConnectionFactory connectionFactory;
  private RabbitAdmin rabbitAdmin;

  public SimpleMessageBase(String host, int port, String username, String password) {
    CachingConnectionFactory connectionFactory = new CachingConnectionFactory(host, port);
    connectionFactory.setUsername(username);
    connectionFactory.setPassword(password);
    this.connectionFactory = connectionFactory;
    this.rabbitAdmin = new RabbitAdmin(connectionFactory);
  }

  public SimpleMessageBase(Rabbitmq rabbitmq) {
    this(rabbitmq.getHost(), rabbitmq.getPort(), rabbitmq.getUsername(), rabbitmq.getPassword());
  }

  SimpleMessageBase() {}

  protected RabbitAdmin getRabbitAdmin() {
    return this.rabbitAdmin;
  }

  protected RabbitTemplate getRabbitTemplate() {
    return this.rabbitAdmin.getRabbitTemplate();
  }

  ConnectionFactory getConnectionFactory() {
    return this.connectionFactory;
  }

  void setRabbitAdmin(RabbitAdmin rabbitAdmin) {
    this.rabbitAdmin = rabbitAdmin;
  }

  public static enum ExchangeType {
    Direct,
    Topic,
    Fanout;

    private ExchangeType() {}
  }
}
