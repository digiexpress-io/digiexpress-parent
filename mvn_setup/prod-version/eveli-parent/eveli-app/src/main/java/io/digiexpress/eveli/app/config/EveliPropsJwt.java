package io.digiexpress.eveli.app.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

@Data
@ConfigurationProperties(prefix = "eveli.jwt")
public class EveliPropsJwt {

  private String gamutPublicKeyValue;
  private String gamutIssuer;
  private String eveliPublicKeyValue;
  private String eveliIssuer;
}
