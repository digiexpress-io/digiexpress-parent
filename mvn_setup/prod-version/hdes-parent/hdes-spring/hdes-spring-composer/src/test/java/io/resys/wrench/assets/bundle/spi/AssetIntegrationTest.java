package io.resys.wrench.assets.bundle.spi;

import java.io.IOException;
import java.nio.charset.Charset;
import java.time.Duration;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/*-
 * #%L
 * wrench-component-assets-integrations
 * %%
 * Copyright (C) 2016 - 2017 Copyright 2016 ReSys OÜ
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

import io.resys.hdes.client.api.HdesClient;
import io.resys.hdes.client.api.HdesStore;
import io.resys.hdes.client.api.programs.ProgramEnvir;
import io.resys.hdes.client.api.programs.ProgramEnvir.ProgramStatus;
import io.resys.hdes.client.spi.HdesClientImpl;
import io.resys.hdes.client.spi.HdesInMemoryStore;
import io.resys.hdes.client.spi.composer.ComposerEntityMapper;
import io.resys.hdes.client.spi.config.HdesClientConfig.DependencyInjectionContext;
import io.resys.hdes.client.spi.config.HdesClientConfig.ServiceInit;
import io.resys.hdes.client.spi.flow.validators.IdValidator;
import io.resys.hdes.spring.composer.controllers.exception.AssetExceptionMapping;


@TestMethodOrder(MethodOrderer.MethodName.class)
@EnableAutoConfiguration
@ExtendWith(SpringExtension.class)
@TestPropertySource(properties = {"wrench.assets.ide = false"})
@ContextConfiguration(classes = {AssetIntegrationTest.ServiceTestConfig.class})
public class AssetIntegrationTest {
  @Autowired
  private ProgramEnvir envir;
  @Autowired
  private ApplicationContext context;

  @Configuration
  public static class ServiceTestConfig {
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
    
    
    @Bean
    public ObjectMapper objectMapper() {
      return new ObjectMapper();
    }
    @Bean
    public HdesStore hdesStore(ObjectMapper objectMapper) {
      return HdesInMemoryStore.builder().objectMapper(objectMapper).build();
    }
    @Bean
    public ProgramEnvir staticAssets(HdesClient client) {
      final var source = client.store().query().get().await().atMost(Duration.ofMinutes(1));
      return ComposerEntityMapper.toEnvir(client.envir().tagName("test"), source).build();
    }
  }

  @Test
  public void services() {
    final var services = envir.getValues();
    Assertions.assertEquals(12, services.size());
  }

  @Test
  public void dt() throws IOException {
    final var dt = envir.getDecisionsByName().get("test decision table");
    Assertions.assertEquals(ProgramStatus.UP, dt.getStatus());
  }

  @Test
  public void flow() throws IOException {
    final var flow = envir.getFlowsByName().get("evaluateRating");
    Assertions.assertEquals(ProgramStatus.UP, flow.getStatus());
  }

  @Test
  public void flowTasks() {
    var task = envir.getServicesByName().get("RuleGroup1");
    Assertions.assertEquals(ProgramStatus.UP, task.getStatus());

    task = envir.getServicesByName().get("RuleGroup2");
    Assertions.assertEquals(ProgramStatus.UP, task.getStatus());
  }

  public String getContent(String location) throws IOException {
    return IOUtils.toString(context.getResource("classpath:" + location).getInputStream(), Charset.forName("utf-8"));
  }
}
