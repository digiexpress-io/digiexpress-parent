package io.resys.avatar.client.spi;

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

import io.resys.avatar.client.spi.store.AvatarStore;
import io.resys.avatar.client.spi.store.AvatarStoreConfig;
import io.resys.avatar.client.spi.store.AvatarStoreException;
import io.resys.avatar.client.spi.store.AvatarStoreConfig.AuthorProvider;
import io.resys.thena.api.ThenaClient;
import io.resys.thena.api.actions.TenantActions.CommitStatus;
import io.resys.thena.api.entities.Tenant;
import io.resys.thena.api.entities.Tenant.StructureType;
import io.resys.thena.api.envelope.QueryEnvelope.QueryEnvelopeStatus;
import io.resys.thena.spi.ImmutableDocumentExceptionMsg;
import io.resys.thena.storesql.DbStateSqlImpl;
import io.resys.thena.support.RepoAssert;
import io.resys.userprofile.client.spi.store.ImmutableAvatarStoreConfig;
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
public class AvatarStoreImpl implements AvatarStore {
  private final AvatarStoreConfig config;
  
  @Override
  public AvatarStore withTenantId(String repoId) {
    return new AvatarStoreImpl(ImmutableAvatarStoreConfig.builder().from(config).tenantId(repoId).build());
  }
  
  @Override
  public Uni<Tenant> getTenant() {
    final var client = config.getClient();
    return client.tenants().find().id(config.getTenantId()).get();
  }
  @Override public AvatarStoreConfig getConfig() { return config; }
  @Override public InternalAvatarTenantQuery query() {
    return new InternalAvatarTenantQuery() {
      private String repoName, headName;
      @Override public InternalAvatarTenantQuery repoName(String repoName) { this.repoName = repoName; return this; }
      @Override public InternalAvatarTenantQuery headName(String headName) { this.headName = headName; return this; }
      @Override public Uni<AvatarStore> create() { return createRepo(repoName, headName); }
      @Override public AvatarStore build() { return createClientStore(repoName, headName); }
      @Override public Uni<AvatarStore> createIfNot() { return createRepoOrGetRepo(repoName, headName); }
      @Override public Uni<AvatarStore> delete() { return deleteRepo(repoName, headName); }
      @Override public Uni<Void> deleteAll() { return deleteRepos(); }
    };
  }
  
  private Uni<AvatarStore> createRepoOrGetRepo(String repoName, String headName) {
    final var client = config.getClient();
    
    return client.tenants().find().id(repoName).get()
        .onItem().transformToUni(repo -> {        
          if(repo == null) {
            return createRepo(repoName, headName); 
          }
          return Uni.createFrom().item(createClientStore(repoName, headName));
    });
  }
  
  private Uni<Void> deleteRepos() {
    final var client = config.getClient();
    final var existingRepos = client.tenants().find().findAll();
    
    
    return existingRepos.onItem().transformToUni((repo) -> {
        
        final var repoId = repo.getId();
        final var rev = repo.getRev();
        
        return client.tenants().find().id(repoId).rev(rev).delete();
      })
      .concatenate().collect().asList()
      .onItem().transformToUni((junk) -> Uni.createFrom().voidItem());
  }
  
  private Uni<AvatarStore> deleteRepo(String repoName, String headName) {
    RepoAssert.notNull(repoName, () -> "repoName must be defined!");
    final var client = config.getClient();
    final var existingRepo = client.git(repoName).tenants().get();
    
    
    return existingRepo.onItem().transformToUni((repoResult) -> {
      if(repoResult.getStatus() != QueryEnvelopeStatus.OK) {
        throw new AvatarStoreException("AVATAR_REPO_GET_FOR_DELETE_FAIL", 
            ImmutableDocumentExceptionMsg.builder()
            .id(repoResult.getStatus().toString())
            .value(repoName)
            .addAllArgs(repoResult.getMessages().stream().map(message-> message.getText()).collect(Collectors.toList()))
            .build()); 
      }
      
      final var repoId = repoResult.getRepo().getId();
      final var rev = repoResult.getRepo().getRev();
      final var docStore = createClientStore(repoName, headName);
      
      return client.tenants().find().id(repoId).rev(rev).delete()
          .onItem().transform(junk -> docStore);
    });
  }
    
  private Uni<AvatarStore> createRepo(String repoName, String headName) {
    RepoAssert.notNull(repoName, () -> "repoName must be defined!");
    
    final var client = config.getClient();
    final var newRepo = client.tenants().commit().name(repoName, StructureType.doc).build();
    return newRepo.onItem().transform((repoResult) -> {
      if(repoResult.getStatus() != CommitStatus.OK) {
        throw new AvatarStoreException("AVATAR_REPO_CREATE_FAIL", 
            ImmutableDocumentExceptionMsg.builder()
            .id(repoResult.getStatus().toString())
            .value(repoName)
            .addAllArgs(repoResult.getMessages().stream().map(message->message.getText()).collect(Collectors.toList()))
            .build()); 
      }
      
      return createClientStore(repoName, headName);
    });
  }
  
  private AvatarStore createClientStore(String repoName, String headName) {
    RepoAssert.notNull(repoName, () -> "repoName must be defined!");
    return new AvatarStoreImpl(ImmutableAvatarStoreConfig.builder()
        .from(config)
        .tenantId(repoName)
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
    private String headName;
    private ObjectMapper objectMapper;
    private AuthorProvider authorProvider;
    private io.vertx.mutiny.pgclient.PgPool pgPool;
    private String pgHost;
    private String pgDb;
    private Integer pgPort;
    private String pgUser;
    private String pgPass;
    private Integer pgPoolSize;
    
    
    private AuthorProvider getAuthorProvider() {
      return this.authorProvider == null ? ()-> "not-configured" : this.authorProvider;
    } 
    
    public AvatarStoreImpl build() {
      RepoAssert.notNull(repoName, () -> "repoName must be defined!");
    
      final var headName = this.headName == null ? AvatarStoreConfig.HEAD_NAME: this.headName;
      if(log.isDebugEnabled()) {
        log.debug("""
          Configuring Thena:
            repoName: {}
            headName: {}
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
          headName,
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
      
      final AvatarStoreConfig config = ImmutableAvatarStoreConfig.builder()
          .client(thena).tenantId(repoName)
          .author(getAuthorProvider())
          .build();
      return new AvatarStoreImpl(config);
    }
  }
}
