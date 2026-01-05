package io.digiexpress.eveli.app;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/*-
 * #%L
 * eveli-app
 * %%
 * Copyright (C) 2015 - 2025 Copyright 2022 ReSys OÜ
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

import io.digiexpress.eveli.client.api.OrgClient;
import io.digiexpress.eveli.client.config.EveliAutoConfigDb.EveliPropsDbResolved;
import io.digiexpress.eveli.client.config.EveliPropsMq;
import io.digiexpress.thena.mq.client.api.ThenaMqClient;
import io.digiexpress.thena.mq.client.spi.persistence.ThenaMqChannelStateImpl;
import io.vertx.core.net.PemTrustOptions;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.SslMode;
import io.vertx.sqlclient.PoolOptions;



@Configuration
public class AppConfig {
  @Bean
  public ThenaMqClient mqClient2(EveliPropsMq props, EveliPropsDbResolved dbConfig) {
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
      new PoolOptions().setMaxSize(1));
    return ThenaMqChannelStateImpl.create()
        .db(props.getChannelName()).client(pgPool)
        .build();
  }
  
  
  @Bean
  public OrgClient orgClient() {
    return new OrgClient() {
      @Override
      public GroupEmailQuery queryGroupEmails() {
        return new GroupEmailQuery() {
          @Override
          public List<String> findAllByGroupName(String groupName) {
            return Collections.emptyList();
          }
        };
      }
      @Override
      public GroupQuery queryGroups() {
        return new GroupQuery() {
          @Override
          public List<Group> findAll() {
            return Collections.emptyList();
          }
        };
      }
    };
  }
}
