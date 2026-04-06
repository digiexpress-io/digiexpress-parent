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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.storage.Storage;
import fi.suomi.asiointitili.ObjectFactory;
import io.digiexpress.eveli.app.config.AppProperties;
import io.digiexpress.eveli.client.api.AttachmentCommands;
import io.digiexpress.eveli.client.api.CommsClient;
import io.digiexpress.eveli.client.api.CommsClient.CustomerMessageBuilder;
import io.digiexpress.eveli.client.api.CommsClient.EmailBuilder;
import io.digiexpress.eveli.client.api.OrgClient;
import io.digiexpress.eveli.client.config.*;
import io.digiexpress.eveli.client.config.EveliAutoConfigAssets.EveliEditEnvir;
import io.digiexpress.eveli.client.google.AttachmentCommandsGoogle;
import io.digiexpress.eveli.client.spi.comms.CustomerSmsBuilderImpl;
import io.digiexpress.eveli.client.spi.comms.EmailBuilderDummy;
import io.digiexpress.eveli.client.spi.comms.EmailBuilderJakarta;
import io.digiexpress.eveli.client.spi.dms.DocContainerClient;
import io.digiexpress.eveli.client.spi.dms.DocContainerClientDummy;
import io.digiexpress.eveli.client.spi.org.OrgClientImpl;
import io.digiexpress.notification.client.*;
import io.resys.limaone.program.Runtime.EnvironmentProperties;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.Optional;
import java.util.function.Supplier;

@Configuration
public class AppConfig {
  
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
  
  // Bean controlling that stencil/wrench assets can be edited
  @Bean(name = EveliAutoConfigAssets.BEAN_NAME)
  public EveliEditEnvir eveliEditEnvir(
      EveliProps eveliProps, 
      EveliPropsAssets assetProps,
      ApplicationContext context,
      io.vertx.mutiny.sqlclient.Pool pgPool,
      EnvironmentProperties envir,
      io.resys.limaone.program.Runtime runtime
  ) {
    
    final var dev = EveliAutoConfigAssets.eveliEditEnvir(context, pgPool, envir, runtime, assetProps);
    
    return EveliAutoConfigAssets.getOrCreateDb(dev)
        .await().atMost(Duration.ofMinutes(5));
  }
  
  @Bean
  public OrgClient orgClient(RestTemplate client, EveliPropsOrg orgProps) {
    return new OrgClientImpl(client, orgProps.getServiceUrl(), Optional.empty());    
  }
  
  @Bean
  @ConditionalOnMissingBean
  public DocContainerClient docContainerClient() {
    return new DocContainerClientDummy();
  }
}
