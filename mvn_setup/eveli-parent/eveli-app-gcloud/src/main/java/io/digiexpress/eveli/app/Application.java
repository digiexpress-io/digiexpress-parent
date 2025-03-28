package io.digiexpress.eveli.app;

import java.time.Duration;
import java.util.function.Supplier;

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
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ResourceLoader;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.storage.Storage;

import fi.suomi.asiointitili.ObjectFactory;
import io.digiexpress.eveli.app.config.AppProperties;
import io.digiexpress.eveli.client.api.AttachmentCommands;
import io.digiexpress.eveli.client.api.CommsClient;
import io.digiexpress.eveli.client.api.CommsClient.CustomerMessageBuilder;
import io.digiexpress.eveli.client.api.CommsClient.EmailBuilder;
import io.digiexpress.eveli.client.api.OrgClient;
import io.digiexpress.eveli.client.config.EveliAutoConfig;
import io.digiexpress.eveli.client.config.EveliAutoConfigAssets;
import io.digiexpress.eveli.client.config.EveliAutoConfigAssets.EveliEditEnvir;
import io.digiexpress.eveli.client.config.EveliAutoConfigEnvir;
import io.digiexpress.eveli.client.config.EveliAutoConfigGamut;
import io.digiexpress.eveli.client.config.EveliAutoConfigJpa;
import io.digiexpress.eveli.client.config.EveliAutoConfigWorker;
import io.digiexpress.eveli.client.config.EveliProps;
import io.digiexpress.eveli.client.config.EveliPropsAssets;
import io.digiexpress.eveli.client.config.EveliPropsEmail;
import io.digiexpress.eveli.client.config.EveliPropsOrg;
import io.digiexpress.eveli.client.google.AttachmentCommandsGoogle;
import io.digiexpress.eveli.client.spi.comms.CustomerSmsBuilderImpl;
import io.digiexpress.eveli.client.spi.comms.EmailBuilderDummy;
import io.digiexpress.eveli.client.spi.comms.EmailBuilderJakarta;
import io.digiexpress.eveli.client.spi.mq.EveliAutoConfigMq;
import io.digiexpress.eveli.client.spi.org.OrgClientImpl;
import io.digiexpress.eveli.dialob.config.DialobAutoConfig;
import io.digiexpress.notification.client.CustomerSmsBuilderSuomifiRest;
import io.digiexpress.notification.client.CustomerSmsBuilderSuomifiWsl;
import io.digiexpress.notification.client.NotificationRestServiceClient;
import io.digiexpress.notification.client.NotificationWebServiceClient;
import io.digiexpress.notification.client.SuomiFiRestProperties;
import io.digiexpress.notification.client.SuomiFiWSLProperties;
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
    EveliAutoConfigEnvir.class,
})

@EnableConfigurationProperties(value = {
    EveliPropsEmail.class,
    SuomiFiWSLProperties.class,
    SuomiFiRestProperties.class,
    
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
  public AttachmentCommands attachmentCommands(
      ResourceLoader resourceLoader,
      Storage storage,
      AppProperties properties) {
    return new AttachmentCommandsGoogle(properties.getDownloadBucket(), storage, resourceLoader);
  }

  @Bean
  public ObjectFactory objectFactory() {
    return new ObjectFactory();
  }
  
  @Bean
  public OrgClient orgClient(RestTemplate client, EveliPropsOrg orgProps) {
    return new OrgClientImpl(client, orgProps.getServiceUrl());    
  }
  
  @Bean
  public CommsClient commsClient(
      SuomiFiRestProperties restApi, 
      SuomiFiWSLProperties wslApi, 
      EveliPropsEmail email,
      ObjectMapper mapper,
      ObjectFactory factory) {
    
    final Supplier<CustomerMessageBuilder> createCustomerSms;  
    final Supplier<EmailBuilder> createEmail;

    if(restApi.isEnabled()) {
      createCustomerSms = () -> new CustomerSmsBuilderSuomifiRest(new NotificationRestServiceClient(restApi, mapper));
    } else if(wslApi.isEnabled()) {
      createCustomerSms =  () -> new CustomerSmsBuilderSuomifiWsl(new NotificationWebServiceClient(wslApi, factory));
    } else {
      createCustomerSms = () -> new CustomerSmsBuilderImpl();
    }
    
    if(Boolean.TRUE.equals(email.getEnabled())) {
      createEmail = () -> new EmailBuilderJakarta(email);
    } else {
      createEmail = () -> new EmailBuilderDummy();
    }
    
    return new CommsClient() {
      @Override public CustomerMessageBuilder createCustomerSms() { return createCustomerSms.get(); }
      @Override public EmailBuilder createEmail() { return createEmail.get(); }
    };
  }

}
