package io.digiexpress.eveli.client.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;


@Data
@Component
@ConfigurationProperties(prefix = "app.organization")
public class OrganizationProperties {
  private String id;
  private String serviceId;
  private String messageVersion;
  private String messageCertCName;
  private String organizationName;
}
