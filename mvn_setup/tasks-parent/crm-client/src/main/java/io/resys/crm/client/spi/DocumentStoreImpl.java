package io.resys.crm.client.spi;

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

import io.resys.crm.client.api.model.Document.DocumentType;
import io.resys.crm.client.spi.store.DocumentConfig;
import io.resys.crm.client.spi.store.DocumentConfig.DocumentAuthorProvider;
import io.resys.crm.client.spi.store.DocumentConfig.DocumentGidProvider;
import io.resys.thena.api.ThenaClient;
import io.resys.thena.api.actions.TenantActions.CommitStatus;
import io.resys.thena.api.entities.Tenant;
import io.resys.thena.api.entities.Tenant.StructureType;
import io.resys.thena.api.envelope.QueryEnvelope.QueryEnvelopeStatus;
import io.resys.thena.storesql.DbStateSqlImpl;
import io.resys.thena.storesql.PgErrors;
import io.resys.thena.support.OidUtils;
import io.resys.thena.support.RepoAssert;
import io.resys.crm.client.spi.store.DocumentStore;
import io.resys.crm.client.spi.store.DocumentStoreException;
import io.resys.crm.client.spi.store.ImmutableDocumentConfig;
import io.resys.crm.client.spi.store.ImmutableDocumentExceptionMsg;
import io.resys.crm.client.spi.store.MainBranch;
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
public class DocumentStoreImpl implements DocumentStore {
  private final DocumentConfig config;
  
  @Override
  public DocumentStore withRepoId(String repoId) {
    return new DocumentStoreImpl(ImmutableDocumentConfig.builder().from(config).repoId(repoId).build());
  }
  
  @Override
  public Uni<Tenant> getRepo() {
    final var client = config.getClient();
    return client.tenants().find().id(config.getRepoId()).get();
  }
  @Override public DocumentConfig getConfig() { return config; }
  @Override public DocumentRepositoryQuery query() {
    return new DocumentRepositoryQuery() {
      private String repoName, headName;
      @Override public DocumentRepositoryQuery repoName(String repoName) { this.repoName = repoName; return this; }
      @Override public DocumentRepositoryQuery headName(String headName) { this.headName = headName; return this; }
      @Override public Uni<DocumentStore> create() { return createRepo(repoName, headName); }
      @Override public DocumentStore build() { return createClientStore(repoName, headName); }
      @Override public Uni<DocumentStore> createIfNot() { return createRepoOrGetRepo(repoName, headName); }
      @Override public Uni<DocumentStore> delete() { return deleteRepo(repoName, headName); }
      @Override public Uni<Void> deleteAll() { return deleteRepos(); }
    };
  }
  
  private Uni<DocumentStore> createRepoOrGetRepo(String repoName, String headName) {
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
  
  private Uni<DocumentStore> deleteRepo(String repoName, String headName) {
    RepoAssert.notNull(repoName, () -> "repoName must be defined!");
    final var client = config.getClient();
    final var existingRepo = client.git(repoName).project().get();
    
    
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
    
  private Uni<DocumentStore> createRepo(String repoName, String headName) {
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
  
  private DocumentStore createClientStore(String repoName, String headName) {
    RepoAssert.notNull(repoName, () -> "repoName must be defined!");
    return new DocumentStoreImpl(ImmutableDocumentConfig.builder()
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
    
    public DocumentStoreImpl build() {
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
        
        thena = DbStateSqlImpl.create().client(pgPool).db(repoName).errorHandler(new PgErrors()).build();
      } else {
        thena = DbStateSqlImpl.create().client(pgPool).db(repoName).errorHandler(new PgErrors()).build();
      }
      
      final DocumentConfig config = ImmutableDocumentConfig.builder()
          .client(thena).repoId(repoName).branchName(headName)
          .gid(getGidProvider())
          .author(getAuthorProvider())
          .build();
      return new DocumentStoreImpl(config);
    }
  }

}
