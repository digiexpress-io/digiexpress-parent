package io.digiexpress.eveli.app;

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

import java.time.Duration;
import java.util.Collections;
import java.util.List;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.digiexpress.eveli.client.api.OrgClient;
import io.digiexpress.eveli.client.config.EveliAutoConfig.EveliPropsDbResolved;
import io.digiexpress.eveli.client.config.EveliAutoConfigAssets;
import io.digiexpress.eveli.client.config.EveliAutoConfigAssets.EveliEditEnvir;
import io.digiexpress.eveli.client.config.EveliProps;
import io.digiexpress.eveli.client.config.EveliPropsAssets;
import io.digiexpress.eveli.client.config.EveliPropsMq;
import io.digiexpress.thena.mq.client.api.ThenaMqClient;
import io.digiexpress.thena.mq.client.spi.persistence.ThenaMqChannelStateImpl;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.SslMode;
import io.vertx.sqlclient.PoolOptions;

@Configuration
public class AppConfig {
  @Bean
  public ThenaMqClient mqClient2(EveliPropsMq props, EveliPropsDbResolved dbConfig) {
    final var sslMode = SslMode.ALLOW;
    
    final io.vertx.mutiny.pgclient.PgPool pgPool = io.vertx.mutiny.pgclient.PgPool.pool(
        new PgConnectOptions()
          .setHost(dbConfig.getHost())
          .setPort(dbConfig.getPort())
          .setDatabase(dbConfig.getDatabase())
          .setUser(dbConfig.getUsername())
          .setPassword(dbConfig.getPassword())
          .setSslMode(sslMode), 
        new PoolOptions().setMaxSize(1));
    return ThenaMqChannelStateImpl.create()
        .db(props.getChannelName()).client(pgPool)
        .build();
  }
  
  
  
  // Bean controlling that stencil/wrench assets can be edited
  @Bean(name = EveliAutoConfigAssets.BEAN_NAME)
  public EveliEditEnvir eveliEditEnvir(
      EveliProps eveliProps, 
      EveliPropsAssets assetProps,
      ObjectMapper objectMapper,
      ApplicationContext context,
      io.vertx.mutiny.pgclient.PgPool pgPool) {
    
    return EveliAutoConfigAssets.getOrCreateDb(EveliAutoConfigAssets.eveliEditEnvir(eveliProps, assetProps, objectMapper, context, pgPool))
        .await().atMost(Duration.ofMinutes(5));
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
      
    };
  }
}
