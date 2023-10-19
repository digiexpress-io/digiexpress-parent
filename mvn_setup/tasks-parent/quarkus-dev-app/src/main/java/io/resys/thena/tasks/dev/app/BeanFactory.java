package io.resys.thena.tasks.dev.app;

/*-
 * #%L
 * thena-quarkus-dev-app
 * %%
 * Copyright (C) 2021 - 2023 Copyright 2021 ReSys OÜ
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
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.quarkus.jackson.ObjectMapperCustomizer;
import io.quarkus.runtime.annotations.RegisterForReflection;
import io.resys.thena.docdb.spi.jackson.VertexExtModule;
import io.resys.thena.tasks.client.api.TaskClient;
import io.resys.thena.tasks.client.api.model.ImmutableTask;
import io.resys.thena.tasks.client.api.model.ImmutableTaskComment;
import io.resys.thena.tasks.client.api.model.ImmutableTaskExtension;
import io.resys.thena.tasks.client.spi.DocumentStoreImpl;
import io.resys.thena.tasks.client.spi.TaskClientImpl;
import io.vertx.core.json.jackson.VertxModule;
import io.vertx.mutiny.core.Vertx;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.sqlclient.PoolOptions;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Dependent
@RegisterForReflection(targets = {
    ImmutableTask.class, 
    ImmutableTaskExtension.class,
    ImmutableTaskComment.class
})
public class BeanFactory {


  @ConfigProperty(name = "tasks.db.pg.repositoryName") 
  String repositoryName;
  String branchSpecifier = "main";
  
  @ConfigProperty(name = "tasks.db.pg.pgPoolSize")
  Integer pgPoolSize;  
  @ConfigProperty(name = "tasks.db.pg.pgHost")
  String pgHost;
  @ConfigProperty(name = "tasks.db.pg.pgPort")
  Integer pgPort;
  @ConfigProperty(name = "tasks.db.pg.pgDb")
  String pgDb;
  @ConfigProperty(name = "tasks.db.pg.pgUser")
  String pgUser;
  @ConfigProperty(name = "tasks.db.pg.pgPass")
  String pgPass;

  @ConfigProperty(name = "tasks.project.id")
  String projectId;
  
  @Data @RequiredArgsConstructor
  public static class CurrentProject {
    private final String projectId;
    private final String head = "main";
  }
  
  @Data @RequiredArgsConstructor
  public static class CurrentUser {
    private String userId = "lady sybil vimes";
  }
  
  @Produces 
  public CurrentProject currentProject() {
    return new CurrentProject(projectId);
  }
  @Produces 
  public CurrentUser currentUser() {
    return new CurrentUser();
  }
  
  @Produces
  public TaskClient client(Vertx vertx, ObjectMapper om) {    
    
    final var connectOptions = new PgConnectOptions().setDatabase(pgDb)
        .setHost(pgHost).setPort(pgPort)
        .setUser(pgUser).setPassword(pgPass);
    final var poolOptions = new PoolOptions().setMaxSize(pgPoolSize);
    final var pgPool = io.vertx.mutiny.pgclient.PgPool.pool(vertx, connectOptions, poolOptions);
      
    final var store = DocumentStoreImpl.builder()
        .repoName(projectId)
        .pgPool(pgPool)
        .pgDb(pgDb)
        .pgPoolSize(pgPoolSize)
        .pgHost(pgHost)
        .pgPort(pgPort)
        .pgUser(pgUser)
        .pgPass(pgPass)
        .objectMapper(om)
        .build();
    return new TaskClientImpl(store);
  }

  /*
  @Produces
  public ObjectMapper objectMapper() {
    final var modules = new com.fasterxml.jackson.databind.Module[] {
        new JavaTimeModule(), 
        new Jdk8Module(), 
        new GuavaModule(),
        new VertxModule(),
        new VertexExtModule()
        };
      DatabindCodec.mapper().registerModules(modules);
      DatabindCodec.prettyMapper().registerModules(modules);
      
    return DatabindCodec.mapper(); 
  }*/
  
  @Produces
  public ObjectMapperCustomizer objectMapperCustomizer() {
    final var modules = new com.fasterxml.jackson.databind.Module[] {
      new JavaTimeModule(), 
      new Jdk8Module(), 
      new GuavaModule(),
      new VertxModule(),
      new VertexExtModule()
    };
    
    return new ObjectMapperCustomizer() {
      public void customize(ObjectMapper mapper) {
        mapper.registerModules(modules);
        // without this, local dates will be serialized as int array
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
      }
    };
  }

}
