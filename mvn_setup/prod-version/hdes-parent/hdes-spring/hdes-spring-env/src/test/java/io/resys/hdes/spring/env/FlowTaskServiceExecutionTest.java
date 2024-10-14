package io.resys.hdes.spring.env;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Duration;

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
import org.springframework.test.context.junit.jupiter.SpringExtension;

/*-
 * #%L
 * wrench-assets-bundle
 * %%
 * Copyright (C) 2016 - 2018 Copyright 2016 ReSys OÜ
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
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.resys.hdes.client.api.HdesClient;
import io.resys.hdes.client.api.HdesStore;
import io.resys.hdes.client.api.programs.ProgramEnvir;
import io.resys.hdes.client.spi.HdesClientImpl;
import io.resys.hdes.client.spi.HdesInMemoryStore;
import io.resys.hdes.client.spi.composer.ComposerEntityMapper;
import io.resys.hdes.client.spi.config.HdesClientConfig.DependencyInjectionContext;
import io.resys.hdes.client.spi.config.HdesClientConfig.ServiceInit;
import io.resys.hdes.client.spi.flow.validators.IdValidator;

@TestMethodOrder(MethodOrderer.MethodName.class)
@EnableAutoConfiguration
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {FlowTaskServiceExecutionTest.ServiceTestConfig.class})
public class FlowTaskServiceExecutionTest {

  @Autowired
  private ProgramEnvir envir;
  @Autowired
  private HdesClient client;
  @Autowired
  private ObjectMapper objectMapper;

  @Configuration
  public static class ServiceTestConfig {
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
      return ComposerEntityMapper.toEnvir(client.envir(), source).build();
    }
  }

  /**
   * Test for issue #1 topics 1 and 4.
   * @throws IOException
   */
  @Test
  public void flowExecution() throws IOException {
    
    final ObjectNode input = objectMapper.createObjectNode();
    input.put("val1", new BigDecimal("10"));
    input.put("val2", new BigDecimal("20"));
    
    final var body = client.executor(envir).inputJson(input).flow("sumFlow").andGetTask("SumTask");
    Assertions.assertTrue(((BigDecimal) body.getReturns().get("sum")).compareTo(new BigDecimal("30")) == 0);
  }
}
