package io.digiexpress.eveli.app;

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
import org.springframework.context.annotation.Import;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.socket.config.annotation.EnableWebSocket;

import io.digiexpress.eveli.client.config.EveliAutoConfig;
import io.digiexpress.eveli.client.config.EveliAutoConfigAssets;
import io.digiexpress.eveli.client.config.EveliAutoConfigJpa;
import io.digiexpress.eveli.client.config.EveliAutoConfigGamut;
import io.digiexpress.eveli.client.config.EveliAutoConfigWorker;
import io.digiexpress.eveli.dialob.config.DialobAutoConfig;
import lombok.extern.slf4j.Slf4j;



@EnableWebSocket
@EnableWebSecurity 
@SpringBootApplication
@EnableAutoConfiguration
@EnableAsync
@EnableScheduling // DialobCallbackController
@Slf4j
@Import(value = { 
    EveliAutoConfigJpa.class, 
    EveliAutoConfigAssets.class, 
    EveliAutoConfig.class, 
    DialobAutoConfig.class,
    EveliAutoConfigGamut.class,
    EveliAutoConfigWorker.class
})
public class Application {
  public static void main(String[] args) throws Exception {
    SpringApplication.run(new Class<?>[] { Application.class }, args);
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
