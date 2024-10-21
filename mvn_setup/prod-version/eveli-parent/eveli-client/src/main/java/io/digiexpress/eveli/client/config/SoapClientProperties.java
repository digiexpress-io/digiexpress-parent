package io.digiexpress.eveli.client.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import lombok.Data;

@Component
@ConfigurationProperties(prefix = "app.config.soapwebapi")
@Data
public class SoapClientProperties {
  private String serviceUri;
  
  private Resource signingCertificate;
  private String signingStorePassword;
  private String signingCertificateKeyAlias ="certificate";

  private String serverCertificatePem;
}

