package io.digiexpress.eveli.app.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

@Data
@ConfigurationProperties(prefix = "eveli.crm")
public class EveliPropsCrm {

  private String host;
  private String servicePathCompany;
  private String servicePathPerson;

}
