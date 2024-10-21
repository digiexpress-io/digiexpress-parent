package io.digiexpress.eveli.client.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;


@Data
@Component
@ConfigurationProperties(prefix = "app.notification")
public class NotificationProperties {
  private boolean enabled;
}
