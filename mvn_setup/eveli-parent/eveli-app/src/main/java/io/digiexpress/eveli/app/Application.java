package io.digiexpress.eveli.app;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

/*-
 * #%L
 * eveli-app
 * %%
 * Copyright (C) 2015 - 2024 Copyright 2022 ReSys OÜ
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

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.digiexpress.eveli.client.api.OrgClient;
import io.digiexpress.eveli.client.config.EveliAutoConfig;
import io.digiexpress.eveli.client.config.EveliAutoConfigAssets;
import io.digiexpress.eveli.client.config.EveliAutoConfigAssets.EveliEditEnvir;
import io.digiexpress.eveli.client.spi.mq.EveliAutoConfigMq;
import io.digiexpress.eveli.client.config.EveliAutoConfigEnvir;
import io.digiexpress.eveli.client.config.EveliAutoConfigGamut;
import io.digiexpress.eveli.client.config.EveliAutoConfigJpa;
import io.digiexpress.eveli.client.config.EveliAutoConfigWorker;
import io.digiexpress.eveli.client.config.EveliProps;
import io.digiexpress.eveli.client.config.EveliPropsAssets;
import io.digiexpress.eveli.dialob.config.DialobAutoConfig;
import lombok.extern.slf4j.Slf4j;


@EnableWebSecurity 
@SpringBootApplication
@EnableAutoConfiguration
@EnableAsync
@EnableScheduling // DialobCallbackController
@Slf4j
@Import(value = { 
    EveliAutoConfigJpa.class, 
    EveliAutoConfigAssets.class,
    EveliAutoConfigMq.class,
    EveliAutoConfig.class, 
    DialobAutoConfig.class,
    EveliAutoConfigGamut.class,
    EveliAutoConfigWorker.class,
    EveliAutoConfigEnvir.class
})
public class Application {
  public static void main(String[] args) throws Exception {
    SpringApplication.run(new Class<?>[] { Application.class }, args);
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

  @EventListener
  public void handleContextRefresh(ContextRefreshedEvent event) {
    final var applicationContext = event.getApplicationContext();
    final var requestMappingHandlerMapping = applicationContext.getBean("requestMappingHandlerMapping", RequestMappingHandlerMapping.class);

    final var newLog = new ApplicationConfigLogger().log(requestMappingHandlerMapping);
    log.info(newLog);
  }
  
  // HHH015007 - https://hibernate.atlassian.net/browse/HHH-17612
}
