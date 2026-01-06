package io.digiexpress.eveli.client.config;

/*-
 * #%L
 * eveli-client
 * %%
 * Copyright (C) 2015 - 2026 Copyright 2022 ReSys OÜ
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.util.Objects;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;

import io.vertx.core.net.PemTrustOptions;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.SslMode;
import io.vertx.sqlclient.PoolOptions;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;



@Configuration
@EnableConfigurationProperties(value = { EveliPropsDb.class })
@Slf4j
public class EveliAutoConfigDb {

  @Data
  @Builder
  public static class EveliPropsDbResolved {
    private String host;
    private int port;
    private String username;
    private String password;
    private String database;
    @Builder.Default
    private String sslMode = "allow"; // default ssl mode, can be overridden by EveliPropsDb.sslMode
    @Builder.Default
    private Boolean sslTrustAll = false; // By default, don't trust all, can be overridden by EveliPropsDb.sslTrustAll
    private String certPath; // Path to the certificate file, can be set in EveliPropsDb.certPath
  }
  
  @Bean
  @ConditionalOnMissingBean
  public EveliPropsDbResolved eveliPropsDbResolved(
    @Value("${spring.datasource.url}") String datasourceUrl,
    @Value("${spring.datasource.username}") String datasourceUsername,
    @Value("${spring.datasource.password}") String datasourcePassword
  ) {
    Assert.isTrue(datasourceUrl.startsWith("jdbc:postgresql://"), "postgresql is only supported database type.");
    var matcher = Pattern.compile("^jdbc:postgresql://(?<host>[\\p{Lower}-.\\d]+)(:(?<port>\\d+))?/(?<database>[^?]+)(\\?(?<params>.*))?$")
      .matcher(datasourceUrl);
    if (!matcher.matches()) {
      throw new IllegalArgumentException("Invalid datasource URL: " + datasourceUrl);
    }
    var pgHost = matcher.group("host");
    var portMatch = matcher.group("port");
    var port = Integer.parseInt(Objects.toString(portMatch, "5432")); // default PostgreSQL port
    var database = matcher.group("database");
    var params = matcher.group("params");
    var builder = EveliPropsDbResolved.builder()
      .host(pgHost)
      .port(port)
      .database(database)
      .username(datasourceUsername)
      .password(datasourcePassword);

    if (params != null && !params.isEmpty()) {
      for (String param : params.split("&")) {
        var paramNameAndValue = param.split("=");
        var paramName = paramNameAndValue[0];
        var paramValue = paramNameAndValue[1];
        if (StringUtils.isBlank(paramValue)) {
          log.warn("Parameter '{}' in datasource URL is empty, skipping.", paramName);
          continue;
        }
        switch (paramName) {
          case "sslmode":
            builder = builder.sslMode(paramValue);
            break;
          case "ssltrustall":
            builder = builder.sslTrustAll(Boolean.parseBoolean(paramValue));
            break;
          case "sslrootcert":
            builder = builder.certPath(paramValue);
            break;
          default:
            log.warn("Unknown parameter in datasource URL: {}", paramName);
        }
      }
    }
    return builder.build();
  }
  
  @Bean
  public io.vertx.mutiny.sqlclient.Pool pgPool(EveliPropsDb db, EveliPropsDbResolved dbConfig) {
    var trustOptions = new PemTrustOptions();
    if (StringUtils.isNotBlank(dbConfig.getCertPath())) {
      trustOptions.addCertPath(dbConfig.getCertPath());
    }
    final io.vertx.mutiny.pgclient.PgPool pgPool = io.vertx.mutiny.pgclient.PgPool.pool(
        new PgConnectOptions()
          .setHost(dbConfig.getHost())
          .setPort(dbConfig.getPort())
          .setDatabase(dbConfig.getDatabase())
          .setUser(dbConfig.getUsername())
          .setPassword(dbConfig.getPassword())
          .setTrustAll(dbConfig.getSslTrustAll())
          .setPemTrustOptions(trustOptions)
          .setSslMode(SslMode.of(dbConfig.getSslMode())),
        new PoolOptions().setMaxSize(db.getPoolMaxSize() == null ? 5 : db.getPoolMaxSize()));
    return pgPool;
  }
}
