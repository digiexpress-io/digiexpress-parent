package io.digiexpress.eveli.client.test.task;

/*-
 * #%L
 * eveli-client
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

import java.time.Duration;
import java.util.HashMap;

import org.junit.jupiter.api.BeforeAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.digiexpress.eveli.client.api.ProcessClient;
import io.digiexpress.eveli.client.api.TaskClient;
import io.digiexpress.eveli.client.persistence.repositories.ProcessRepository;
import io.digiexpress.eveli.client.spi.crm.CustomerAccountClientImpl;
import io.digiexpress.eveli.client.spi.process.ProcessClientImpl;
import io.digiexpress.eveli.client.spi.task.ImmutableTaskStoreConfig;
import io.digiexpress.eveli.client.spi.task.TaskClientImpl;
import io.digiexpress.eveli.client.spi.task.TaskStoreImpl;
import io.resys.hdes.client.api.HdesClient;
import io.resys.hdes.client.api.HdesClient.HdesTypesMapper;
import io.resys.hdes.client.spi.HdesClientImpl;
import io.resys.hdes.client.spi.HdesInMemoryStore;
import io.resys.hdes.client.spi.HdesTypeDefsFactory;
import io.resys.hdes.client.spi.config.HdesClientConfig.DependencyInjectionContext;
import io.resys.hdes.client.spi.config.HdesClientConfig.ServiceInit;
import io.resys.thena.api.entities.Tenant.StructureType;
import io.resys.thena.grim.api.GrimClient;
import io.resys.thena.grim.spi.GrimClientImpl;
import io.vertx.mutiny.sqlclient.Pool;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.sqlclient.PoolOptions;
import lombok.extern.slf4j.Slf4j;



@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class})
@ContextConfiguration(classes = { 
    TaskEnvirSetupDebugDb.TaskEnvirSetupConfig.class 
})
public abstract class TaskEnvirSetupDebugDb {
  private static io.vertx.mutiny.pgclient.PgPool PGPOOL;
  
  private static GrimClient THENA_STATE;
  
  public static ServiceInit SERVICE_INIT = new ServiceInit() {
    @Override
    public <T> T get(Class<T> type) {
      try {
        return type.getDeclaredConstructor().newInstance();
      } catch(Exception e) {
        throw new RuntimeException(e.getMessage(), e);
      }
    }
  };
  
  public static DependencyInjectionContext DI = new DependencyInjectionContext() {
    @Override
    public <T> T get(Class<T> type) {
      return null;
    }
  };
    
  
  @BeforeAll
  public static void start() {
    PGPOOL = io.vertx.mutiny.pgclient.PgPool.pool(
        new PgConnectOptions()
          .setHost("localhost")
          .setPort(5433)
          .setDatabase("eveli-app")
          .setUser("eveli-app")
          .setPassword("password123"), 
        new PoolOptions().setMaxSize(5));
    waitUntilPostgresqlAcceptsConnections(PGPOOL);
    
    THENA_STATE = GrimClientImpl.create()
        .db("junit")
        .client(PGPOOL)
        .build();

  }
  
  private static void waitUntilPostgresqlAcceptsConnections(Pool pool) {
    // On some platforms there may be some delay before postgresql starts to respond.
    // Try until postgresql connection is successfully opened.
    var connection = pool.getConnection()
            .onFailure()
            .retry().withBackOff(Duration.ofMillis(10), Duration.ofSeconds(3)).atMost(20)
            .await().atMost(Duration.ofSeconds(60));
    connection.closeAndForget();
  }
  
  public static void end() {
  }  

  @Configuration @Slf4j
  public static class TaskEnvirSetupConfig {
    @Autowired ObjectMapper objectMapper;
    @Autowired ApplicationEventPublisher publisher;

    @Bean
    public HdesClient hdesClient() {
      return HdesClientImpl.builder()
        .objectMapper(objectMapper)
        .store(new HdesInMemoryStore(new HashMap<>()))
        .dependencyInjectionContext(DI)
        .serviceInit(SERVICE_INIT)
        .build();
    }
    
    @Bean
    public HdesTypesMapper hdesTypes(ObjectMapper objectMapper) {
      return new HdesTypeDefsFactory(objectMapper);
    }
    
    
    @Bean
    public TaskClient taskClient(ApplicationEventPublisher publisher, ProcessRepository proc) {
      final var config = ImmutableTaskStoreConfig.builder()
          .tenantName("task-tenant")
          .client(THENA_STATE)
          .build();

      final var store = new TaskStoreImpl(config);
      
      // create task project
      final var repo = THENA_STATE.tenants().createOneTenant()
          .name("task-tenant", StructureType.grim)
          .build()
          .await().atMost(Duration.ofMinutes(1));

      log.info("repo created: {}", repo);
      final var customer = new CustomerAccountClientImpl(new ProcessClientImpl(proc, null, null));
      return new TaskClientImpl(null, null, null, store, customer);
    }
    
    @Bean
    public ProcessClient processClient() {
      final var processClient = new ProcessClientImpl(null, null, null);
      return processClient;
    }
  }
}
