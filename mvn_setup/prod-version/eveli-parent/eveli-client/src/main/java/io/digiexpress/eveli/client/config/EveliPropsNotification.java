package io.digiexpress.eveli.client.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;


@Data
@ConfigurationProperties(prefix = "eveli.notification")
public class EveliPropsNotification {
  private Boolean enabled;
  
  // service url providing REST API for notification sending
  private String serviceUrl;
}
