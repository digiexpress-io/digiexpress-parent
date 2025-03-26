package io.digiexpress.eveli.client.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import io.digiexpress.eveli.client.config.factory.YamlPropertySourceFactory;
import lombok.Data;

@Configuration
@Data
@ConfigurationProperties(prefix = "eveli.authorization")
@PropertySource(value = "classpath:eveliPermissions.yaml", factory = YamlPropertySourceFactory.class)
public class EveliAutoConfigPermissions {

  private List<Access> worker;
  @Data
  public static class Access {
    private String pathPattern;
    private String method;
    private List<String> roles;

  }

}
