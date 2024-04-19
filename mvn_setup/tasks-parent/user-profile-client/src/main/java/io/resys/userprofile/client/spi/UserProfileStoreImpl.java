package io.resys.userprofile.client.spi;

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
import io.resys.thena.support.OidUtils;
import io.resys.thena.support.RepoAssert;
import io.resys.userprofile.client.api.model.Document.DocumentType;
import io.resys.userprofile.client.spi.store.DocumentStoreException;
import io.resys.userprofile.client.spi.store.ImmutableUserProfileStoreConfig;
import io.resys.userprofile.client.spi.store.MainBranch;
import io.resys.userprofile.client.spi.store.UserProfileStore;
import io.resys.userprofile.client.spi.store.UserProfileStoreConfig;
import io.resys.userprofile.client.spi.store.UserProfileStoreConfig.DocumentAuthorProvider;
import io.resys.userprofile.client.spi.store.UserProfileStoreConfig.DocumentGidProvider;
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
public class UserProfileStoreImpl implements UserProfileStore {
  private final UserProfileStoreConfig config;
  
  @Override
  public UserProfileStore withTenantId(String repoId) {
    return new UserProfileStoreImpl(ImmutableUserProfileStoreConfig.builder().from(config).repoId(repoId).build());
  }
  
  @Override
  public Uni<Tenant> getRepo() {
    final var client = config.getClient();
    return client.tenants().find().id(config.getRepoId()).get();
  }
  @Override public UserProfileStoreConfig getConfig() { return config; }
  @Override public UserProfileTenantQuery query() {
    return new UserProfileTenantQuery() {
      private String repoName, headName;
      @Override public UserProfileTenantQuery repoName(String repoName) { this.repoName = repoName; return this; }
      @Override public UserProfileTenantQuery headName(String headName) { this.headName = headName; return this; }
      @Override public Uni<UserProfileStore> create() { return createRepo(repoName, headName); }
      @Override public UserProfileStore build() { return createClientStore(repoName, headName); }
      @Override public Uni<UserProfileStore> createIfNot() { return createRepoOrGetRepo(repoName, headName); }
      @Override public Uni<UserProfileStore> delete() { return deleteRepo(repoName, headName); }
      @Override public Uni<Void> deleteAll() { return deleteRepos(); }
    };
  }
  
  private Uni<UserProfileStore> createRepoOrGetRepo(String repoName, String headName) {
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
  
  private Uni<UserProfileStore> deleteRepo(String repoName, String headName) {
    RepoAssert.notNull(repoName, () -> "repoName must be defined!");
    final var client = config.getClient();
    final var existingRepo = client.git(repoName).tenants().get();
    
    
    return existingRepo.onItem().transformToUni((repoResult) -> {
      if(repoResult.getStatus() != QueryEnvelopeStatus.OK) {
        throw new DocumentStoreException("CRM_REPO_GET_FOR_DELETE_FAIL", 
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
    
  private Uni<UserProfileStore> createRepo(String repoName, String headName) {
    RepoAssert.notNull(repoName, () -> "repoName must be defined!");
    
    final var client = config.getClient();
    final var newRepo = client.tenants().commit().name(repoName, StructureType.doc).build();
    return newRepo.onItem().transform((repoResult) -> {
      if(repoResult.getStatus() != CommitStatus.OK) {
        throw new DocumentStoreException("CRM_REPO_CREATE_FAIL", 
            ImmutableDocumentExceptionMsg.builder()
            .id(repoResult.getStatus().toString())
            .value(repoName)
            .addAllArgs(repoResult.getMessages().stream().map(message->message.getText()).collect(Collectors.toList()))
            .build()); 
      }
      
      return createClientStore(repoName, headName);
    });
  }
  
  private UserProfileStore createClientStore(String repoName, String headName) {
    RepoAssert.notNull(repoName, () -> "repoName must be defined!");
    return new UserProfileStoreImpl(ImmutableUserProfileStoreConfig.builder()
        .from(config)
        .repoId(repoName)
        .branchName(headName == null ? config.getBranchName() : headName)
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
    private DocumentGidProvider gidProvider;
    private DocumentAuthorProvider authorProvider;
    private io.vertx.mutiny.pgclient.PgPool pgPool;
    private String pgHost;
    private String pgDb;
    private Integer pgPort;
    private String pgUser;
    private String pgPass;
    private Integer pgPoolSize;
    
    private DocumentGidProvider getGidProvider() {
      return this.gidProvider != null ? this.gidProvider : new DocumentGidProvider() {
        @Override public String getNextVersion(DocumentType entity) { return OidUtils.gen(); }
        @Override public String getNextId(DocumentType entity) { return OidUtils.gen(); }
      };
    }
    
    private DocumentAuthorProvider getAuthorProvider() {
      return this.authorProvider == null ? ()-> "not-configured" : this.authorProvider;
    } 
    
    public UserProfileStoreImpl build() {
      RepoAssert.notNull(repoName, () -> "repoName must be defined!");
    
      final var headName = this.headName == null ? MainBranch.HEAD_NAME: this.headName;
      if(log.isDebugEnabled()) {
        log.debug("""
          Configuring Thena:
            repoName: {}
            headName: {}
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
          headName,
          this.objectMapper == null ? "configuring" : "provided",
          this.gidProvider == null ? "configuring" : "provided",
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
      
      final UserProfileStoreConfig config = ImmutableUserProfileStoreConfig.builder()
          .client(thena).repoId(repoName).branchName(headName)
          .gid(getGidProvider())
          .author(getAuthorProvider())
          .build();
      return new UserProfileStoreImpl(config);
    }
  }

}
