package io.digiexpress.eveli.client.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;


@Data
@Component
@ConfigurationProperties(prefix = "app.email")
public class EmailProperties {
  private String hostName;
  private String hostPort;
  private String senderEmail;
  private String senderPassword;
  private Boolean enabled;
  private List<String> allowedRecipients;
  private List<String> enabledDomains;
}
