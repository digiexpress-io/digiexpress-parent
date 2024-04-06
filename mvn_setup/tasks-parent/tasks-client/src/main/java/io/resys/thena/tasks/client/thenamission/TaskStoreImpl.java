package io.resys.thena.tasks.client.thenamission;

import java.util.stream.Collectors;

/*-
 * #%L
 * thena-tasks-client
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


import com.fasterxml.jackson.databind.ObjectMapper;

import io.resys.thena.api.ThenaClient;
import io.resys.thena.api.actions.TenantActions.CommitStatus;
import io.resys.thena.api.entities.Tenant;
import io.resys.thena.api.entities.Tenant.StructureType;
import io.resys.thena.api.envelope.QueryEnvelope.QueryEnvelopeStatus;
import io.resys.thena.spi.ImmutableDocumentExceptionMsg;
import io.resys.thena.storesql.DbStateSqlImpl;
import io.resys.thena.support.RepoAssert;
import io.resys.thena.tasks.client.thenagit.store.DocumentStoreException;
import io.resys.thena.tasks.client.thenamission.TaskStoreConfig.TaskAuthorProvider;
import io.smallrye.mutiny.Uni;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.sqlclient.PoolOptions;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;



@RequiredArgsConstructor
public class TaskStoreImpl implements TaskStore {
  private final TaskStoreConfig config;
  

  @Override
  public TaskStore withTenantId(String repoId) {
    return new TaskStoreImpl(ImmutableTaskStoreConfig.builder().from(config).tenantName(repoId).build());
  }
  @Override
  public Uni<Tenant> getRepo() {
    final var client = config.getClient();
    return client.tenants().find().id(config.getTenantName()).get();
  }
  @Override public TaskStoreConfig getConfig() { return config; }
  @Override public TaskTenantQuery query() {
    return new TaskTenantQuery() {
      private String tenantName;
      @Override public TaskTenantQuery tenantName(String tenantName) { this.tenantName = tenantName; return this; }
      @Override public Uni<TaskStore> create() { return createRepo(tenantName); }
      @Override public TaskStore build() { return createClientStore(tenantName); }
      @Override public Uni<TaskStore> createIfNot() { return createRepoOrGetRepo(tenantName); }
      @Override public Uni<TaskStore> delete() { return deleteRepo(tenantName); }
    };
  }
  
  private Uni<TaskStore> createRepoOrGetRepo(String repoName) {
    final var client = config.getClient();
    
    return client.tenants().find().id(repoName).get()
        .onItem().transformToUni(repo -> {        
          if(repo == null) {
            return createRepo(repoName); 
          }
          return Uni.createFrom().item(createClientStore(repoName));
    });
  }
  private Uni<TaskStore> deleteRepo(String repoName) {
    RepoAssert.notNull(repoName, () -> "repoName must be defined!");
    final var client = config.getClient();
    final var existingRepo = client.git(repoName).tenants().get();
    
    
    return existingRepo.onItem().transformToUni((repoResult) -> {
      if(repoResult.getStatus() != QueryEnvelopeStatus.OK) {
        throw new DocumentStoreException("REPO_GET_FOR_DELETE_FAIL", 
            ImmutableDocumentExceptionMsg.builder()
            .id(repoResult.getStatus().toString())
            .value(repoName)
            .addAllArgs(repoResult.getMessages().stream().map(message-> message.getText()).collect(Collectors.toList()))
            .build()); 
      }
      
      final var repoId = repoResult.getRepo().getId();
      final var rev = repoResult.getRepo().getRev();
      final var docStore = createClientStore(repoName);
      
      return client.tenants().find().id(repoId).rev(rev).delete()
          .onItem().transform(junk -> docStore);
    });
  }
    
  private Uni<TaskStore> createRepo(String repoName) {
    RepoAssert.notNull(repoName, () -> "repoName must be defined!");
    final var client = config.getClient();
    final var newRepo = client.tenants().commit().name(repoName, StructureType.grim).build();
    return newRepo.onItem().transform((repoResult) -> {
      if(repoResult.getStatus() != CommitStatus.OK) {
        throw new DocumentStoreException("REPO_CREATE_FAIL", 
            ImmutableDocumentExceptionMsg.builder()
            .id(repoResult.getStatus().toString())
            .value(repoName)
            .addAllArgs(repoResult.getMessages().stream().map(message->message.getText()).collect(Collectors.toList()))
            .build()); 
      }
      
      return createClientStore(repoName);
    });
  }
  
  private TaskStore createClientStore(String repoName) {
    RepoAssert.notNull(repoName, () -> "repoName must be defined!");
    return new TaskStoreImpl(ImmutableTaskStoreConfig.builder()
        .from(config)
        .tenantName(repoName)
        .build());
    
  }
  
  public static Builder builder() {
    return new Builder();
  }
  
  @Slf4j
  @Accessors(fluent = true, chain = true)
  @Getter(AccessLevel.NONE)
  @Data
  public static class Builder {
    private String repoName;
    private ObjectMapper objectMapper;
    private TaskAuthorProvider authorProvider;
    private io.vertx.mutiny.pgclient.PgPool pgPool;
    private String pgHost;
    private String pgDb;
    private Integer pgPort;
    private String pgUser;
    private String pgPass;
    private Integer pgPoolSize;
    
    private TaskAuthorProvider getAuthorProvider() {
      return this.authorProvider == null ? ()-> "not-configured" : this.authorProvider;
    } 
    
    public TaskStoreImpl build() {
      RepoAssert.notNull(repoName, () -> "repoName must be defined!");
    
      if(log.isDebugEnabled()) {
        log.debug("""
          Configuring Thena:
            repoName: {}
            objectMapper: {}
            gidProvider: {}
            authorProvider: {}
            pgPool: {}
            pgPoolSize: {}
            pgHost: {}
            pgPort: {}
            pgDb: {}
            pgUser: {}
            pgPass: {}
          """,
          this.repoName,
          this.objectMapper == null ? "configuring" : "provided",
          this.authorProvider == null ? "configuring" : "provided",
          this.pgPool == null ? "configuring" : "provided",
          this.pgPoolSize,
          this.pgHost,
          this.pgPort,
          this.pgDb,
          this.pgUser == null ? "null" : "***",
          this.pgPass == null ? "null" : "***");
      }

      final ThenaClient thena;
      if(pgPool == null) {
        RepoAssert.notNull(pgHost, () -> "pgHost must be defined!");
        RepoAssert.notNull(pgPort, () -> "pgPort must be defined!");
        RepoAssert.notNull(pgDb, () -> "pgDb must be defined!");
        RepoAssert.notNull(pgUser, () -> "pgUser must be defined!");
        RepoAssert.notNull(pgPass, () -> "pgPass must be defined!");
        RepoAssert.notNull(pgPoolSize, () -> "pgPoolSize must be defined!");
        
        final PgConnectOptions connectOptions = new PgConnectOptions()
            .setHost(pgHost)
            .setPort(pgPort)
            .setDatabase(pgDb)
            .setUser(pgUser)
            .setPassword(pgPass);
        final PoolOptions poolOptions = new PoolOptions()
            .setMaxSize(pgPoolSize);
        
        final io.vertx.mutiny.pgclient.PgPool pgPool = io.vertx.mutiny.pgclient.PgPool.pool(connectOptions, poolOptions);
        
        thena = DbStateSqlImpl.create().client(pgPool).db(repoName).build();
      } else {
        thena = DbStateSqlImpl.create().client(pgPool).db(repoName).build();
      }
      
      final var config = ImmutableTaskStoreConfig.builder()
          .client(thena).tenantName(repoName)
          .author(getAuthorProvider())
          .build();
      return new TaskStoreImpl(config);
    }
  }
}
