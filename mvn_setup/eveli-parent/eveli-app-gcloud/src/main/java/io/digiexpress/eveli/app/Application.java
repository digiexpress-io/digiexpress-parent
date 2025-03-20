package io.digiexpress.eveli.app;

import java.time.Duration;

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
import org.springframework.core.io.ResourceLoader;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.storage.Storage;

import io.digiexpress.eveli.client.api.AttachmentCommands;
import io.digiexpress.eveli.client.api.CommsClient;
import io.digiexpress.eveli.client.config.EveliAutoConfig;
import io.digiexpress.eveli.client.config.EveliAutoConfigAssets;
import io.digiexpress.eveli.client.config.EveliAutoConfigAssets.EveliEditEnvir;
import io.digiexpress.eveli.client.config.EveliAutoConfigEnvir;
import io.digiexpress.eveli.client.config.EveliAutoConfigGamut;
import io.digiexpress.eveli.client.config.EveliAutoConfigJpa;
import io.digiexpress.eveli.client.config.EveliAutoConfigMq;
import io.digiexpress.eveli.client.config.EveliAutoConfigWorker;
import io.digiexpress.eveli.client.config.EveliProps;
import io.digiexpress.eveli.client.config.EveliPropsAssets;
import io.digiexpress.eveli.client.google.AttachmentCommandsGoogle;
import io.digiexpress.eveli.client.spi.comms.CommsClientDummy;
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
  public AttachmentCommands attachmentCommands() {
    String downloadBucket = null;
    Storage storage = null;
    ResourceLoader resourceLoader = null;
    return new AttachmentCommandsGoogle(downloadBucket, storage, resourceLoader);
  }

  @Bean
  public CommsClient notificationCommands() {
    return new CommsClientDummy();
  }

}
