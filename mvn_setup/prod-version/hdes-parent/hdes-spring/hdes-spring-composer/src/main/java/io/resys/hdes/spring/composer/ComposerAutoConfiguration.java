package io.resys.hdes.spring.composer;

/*-
 * #%L
 * hdes-spring-composer
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


import java.util.Optional;

import org.immutables.value.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.resys.hdes.client.api.HdesClient;
import io.resys.hdes.client.api.HdesStore;
import io.resys.hdes.client.spi.HdesClientImpl;
import io.resys.hdes.client.spi.config.HdesClientConfig.DependencyInjectionContext;
import io.resys.hdes.client.spi.config.HdesClientConfig.ServiceInit;
import io.resys.hdes.client.spi.flow.validators.IdValidator;
import io.resys.hdes.spring.composer.controllers.exception.AssetExceptionMapping;
import jakarta.servlet.http.HttpServletRequest;

@Configuration
@ConditionalOnProperty(name = "wrench.assets.enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties({
  GitConfigBean.class,
  PgConfigBean.class,
  ComposerConfigBean.class})
@Import({
  ComposerIdeConfig.class,
  InMemoryConfig.class, 
  GitConfig.class, 
  PgConfiguration.class })
public class ComposerAutoConfiguration {

  @Bean
  public AssetExceptionMapping assetExceptionMapping() {
    return new AssetExceptionMapping();
  }
  @Bean
  public HdesClient hdesClient(
      ApplicationContext context, 
      ObjectMapper objectMapper, 
      HdesStore store) {
    
    final ServiceInit init = new ServiceInit() {
      @Override
      public <T> T get(Class<T> type) {
        return context.getAutowireCapableBeanFactory().createBean(type);
      }
    };

    final HdesClientImpl hdesClient = HdesClientImpl.builder()
        .store(store)
        .objectMapper(objectMapper)
        .serviceInit(init)
        .dependencyInjectionContext(new DependencyInjectionContext() {
          @Override
          public <T> T get(Class<T> type) {
            return context.getBean(type);
          }
        })
        .flowVisitors(new IdValidator())
        .build();
    
    return hdesClient;
  }
  
  
  @FunctionalInterface
  public interface SpringIdeTokenSupplier {
    Optional<IdeToken> get(HttpServletRequest request);
  }
  
  @Value.Immutable
  public interface IdeToken {
    String getKey();
    String getValue();
  }
}
