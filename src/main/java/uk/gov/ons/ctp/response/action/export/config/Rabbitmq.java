package uk.gov.ons.ctp.response.action.export.config;

import lombok.Data;
import net.sourceforge.cobertura.CoverageIgnore;

@CoverageIgnore
@Data
public class Rabbitmq {
  private String username;
  private String password;
  private String host;
  private int port;
  private String virtualHost;
  private String cron;
}
