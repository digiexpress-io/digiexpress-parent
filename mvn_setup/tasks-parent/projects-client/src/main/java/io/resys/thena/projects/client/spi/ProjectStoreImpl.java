package io.resys.thena.projects.client.spi;

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
import io.resys.thena.api.entities.doc.ImmutableThenaDocConfig;
import io.resys.thena.api.entities.doc.ThenaDocConfig;
import io.resys.thena.api.envelope.QueryEnvelope.QueryEnvelopeStatus;
import io.resys.thena.projects.client.spi.store.ImmutableDocumentExceptionMsg;
import io.resys.thena.projects.client.spi.store.ProjectStore;
import io.resys.thena.projects.client.spi.store.ProjectStoreException;
import io.resys.thena.storesql.DbStateSqlImpl;
import io.resys.thena.support.RepoAssert;
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
public class ProjectStoreImpl implements ProjectStore {
  private final ThenaDocConfig config;
  

  @Override
  public Uni<Tenant> getRepo() {
    final var client = config.getClient();
    return client.tenants().find().id(config.getRepoId()).get();
  }
  @Override public ThenaDocConfig getConfig() { return config; }
  @Override public DocumentRepositoryQuery query() {
    return new DocumentRepositoryQuery() {
      private String repoName, externalId;
      private StructureType repoType;
      @Override public DocumentRepositoryQuery externalId(String externalId) { this.externalId = externalId; return this; }
      @Override public DocumentRepositoryQuery repoName(String repoName) { this.repoName = repoName; return this; }
      @Override public DocumentRepositoryQuery repoType(StructureType repoType) { this.repoType = repoType; return this; }
      @Override public Uni<ProjectStore> create() { return createRepo(repoName, externalId, repoType); }
      @Override public ProjectStore build() { return createClientStore(repoName); }
      @Override public Uni<ProjectStore> createIfNot() { return createRepoOrGetRepo(repoName, externalId, repoType); }
      @Override public Uni<ProjectStore> delete() { return deleteRepo(repoName); }
      @Override public Uni<Void> deleteAll() { return deleteRepos(); }
    };
  }
  
  private Uni<ProjectStore> createRepoOrGetRepo(String repoName, String externalId, StructureType type) {
    final var client = config.getClient();
    
    return client.tenants().find().id(repoName).get()
        .onItem().transformToUni(repo -> {        
          if(repo == null) {
            return createRepo(repoName, externalId, type); 
          }
          return Uni.createFrom().item(createClientStore(repoName));
    });
  }
  
  private Uni<Void> deleteRepos() {
    final var client = config.getClient();    
    return client.tenants().delete()
    .onItem().transformToUni((junk) -> Uni.createFrom().voidItem());
  }
  
  private Uni<ProjectStore> deleteRepo(String repoName) {
    RepoAssert.notNull(repoName, () -> "repoName must be defined!");
    final var client = config.getClient();
    final var existingRepo = client.git(repoName).tenants().get();
    
    
    return existingRepo.onItem().transformToUni((repoResult) -> {
      if(repoResult.getStatus() != QueryEnvelopeStatus.OK) {
        throw new ProjectStoreException("REPO_GET_FOR_DELETE_FAIL", 
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
    
  private Uni<ProjectStore> createRepo(String repoName, String externalId, StructureType repoType) {
    RepoAssert.notNull(repoName, () -> "repoName must be defined!");
    RepoAssert.notNull(repoType, () -> "repoType must be defined!");
    
    final var client = config.getClient();
    final var newRepo = client.tenants().commit().externalId(externalId).name(repoName, repoType).build();
    return newRepo.onItem().transform((repoResult) -> {
      if(repoResult.getStatus() != CommitStatus.OK) {
        throw new ProjectStoreException("REPO_CREATE_FAIL", 
            ImmutableDocumentExceptionMsg.builder()
            .id(repoResult.getStatus().toString())
            .value(repoName)
            .addAllArgs(repoResult.getMessages().stream().map(message->message.getText()).collect(Collectors.toList()))
            .build()); 
      }
      
      return createClientStore(repoName);
    });
  }
  
  private ProjectStore createClientStore(String repoName) {
    RepoAssert.notNull(repoName, () -> "repoName must be defined!");
    return new ProjectStoreImpl(ImmutableThenaDocConfig.builder().from(config).repoId(repoName).build());
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
    private String headName;
    private ObjectMapper objectMapper;
    private ThenaDocConfig.AuthorProvider authorProvider;
    private io.vertx.mutiny.pgclient.PgPool pgPool;
    private String pgHost;
    private String pgDb;
    private Integer pgPort;
    private String pgUser;
    private String pgPass;
    private Integer pgPoolSize;
    
    private ThenaDocConfig.AuthorProvider getAuthorProvider() {
      return this.authorProvider == null ? ()-> "not-configured" : this.authorProvider;
    } 
    
    public ProjectStoreImpl build() {
      RepoAssert.notNull(repoName, () -> "repoName must be defined!");
    
      
      if(log.isDebugEnabled()) {
        log.debug("""
          Configuring Thena:
            repoName: {}
            objectMapper: {}
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
      
      final ThenaDocConfig config = ImmutableThenaDocConfig.builder().client(thena).repoId(repoName).author(getAuthorProvider()).build();
      return new ProjectStoreImpl(config);
    }
  }

  @Override
  public ProjectStore withRepoId(String repoId) {
    return new ProjectStoreImpl(ImmutableThenaDocConfig.builder().from(config).repoId(repoId).build());
  }
}
