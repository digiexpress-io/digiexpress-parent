package io.resys.hdes.spring.env;

/*-
 * #%L
 * hdes-spring-env
 * %%
 * Copyright (C) 2020 - 2021 Copyright 2020 ReSys OÜ
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

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.resys.hdes.client.api.HdesClient;
import io.resys.hdes.client.api.programs.ProgramEnvir;
import io.resys.hdes.client.spi.HdesClientImpl;
import io.resys.hdes.client.spi.HdesInMemoryStore;
import io.resys.hdes.client.spi.composer.ComposerEntityMapper;
import io.resys.hdes.client.spi.config.HdesClientConfig.DependencyInjectionContext;
import io.resys.hdes.client.spi.config.HdesClientConfig.ServiceInit;

@Configuration
public class InMemoryAutoConfiguration {

  @Bean
  public ExceptionMapping assetExceptionMapping() {
    return new ExceptionMapping();
  }
  
  
  @Bean
  public HdesClient hdesClient(ObjectMapper objectMapper, ApplicationContext context) {
    final ServiceInit init = new ServiceInit() {
      @Override
      public <T> T get(Class<T> type) {
        return context.getAutowireCapableBeanFactory().createBean(type);
      }
    };
    final var store = HdesInMemoryStore.builder().objectMapper(objectMapper).build();
    return HdesClientImpl.builder().objectMapper(objectMapper)
        .dependencyInjectionContext(new DependencyInjectionContext() {
          @Override
          public <T> T get(Class<T> type) {
            return context.getBean(type);
          }
        })
        .serviceInit(init).store(store).build();
    
  }
  
  @Bean
  public ProgramEnvir staticAssets(HdesClient client) {
    final var source = client.store().query().get().await().atMost(Duration.ofMinutes(1));
    return ComposerEntityMapper.toEnvir(client.envir(), source).build();
  }

}
