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
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import io.digiexpress.eveli.client.config.EveliAutoConfig;
import io.digiexpress.eveli.client.config.EveliAutoConfigAssets;
import io.digiexpress.eveli.client.config.EveliAutoConfigDB;
import io.digiexpress.eveli.dialob.config.DialobAutoConfig;
import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@EnableAutoConfiguration
@EnableAsync
@Slf4j
@Import(value = { EveliAutoConfigDB.class, EveliAutoConfigAssets.class, EveliAutoConfig.class, DialobAutoConfig.class })
public class Application {
  public static void main(String[] args) throws Exception {
    SpringApplication.run(new Class<?>[]{Application.class}, args);
  }
  
  
  @EventListener
  public void handleContextRefresh(ContextRefreshedEvent event) {
    final var applicationContext = event.getApplicationContext();
    final var requestMappingHandlerMapping = applicationContext.getBean("requestMappingHandlerMapping", RequestMappingHandlerMapping.class);
    final var endpoints = requestMappingHandlerMapping.getHandlerMethods();
    
    final var msg = new StringBuilder("\r\nREST API:");
    
    endpoints.forEach((key, value) -> {
      
      
      msg.append("  - ").append(key).append(" = ").append(value).append("\r\n");
    });
    
    log.info(msg.toString());
  }
  // HHH015007 - https://hibernate.atlassian.net/browse/HHH-17612
}
