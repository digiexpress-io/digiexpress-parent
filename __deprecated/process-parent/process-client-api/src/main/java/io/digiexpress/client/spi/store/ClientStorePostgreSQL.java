package io.digiexpress.client.spi.store;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.dialob.client.spi.support.OidUtils;
import io.digiexpress.client.api.ClientStore;
import io.digiexpress.client.api.ImmutableStoreEntity;
import io.digiexpress.client.api.ImmutableStoreExceptionMsg;
import io.digiexpress.client.spi.support.MainBranch;
import io.digiexpress.client.spi.support.ServiceAssert;
import io.resys.thena.docdb.api.DocDB;
import io.resys.thena.docdb.api.actions.RepoActions.RepoStatus;
import io.resys.thena.docdb.api.models.Repo.RepoType;
import io.resys.thena.docdb.store.sql.DbStateSqlImpl;
import io.resys.thena.docdb.store.sql.PgErrors;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.sqlclient.PoolOptions;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.stream.Collectors;



public class ClientStorePostgreSQL extends DocDBCommandsSupport implements ClientStore {

  public ClientStorePostgreSQL(DocDBConfig config) {
    super(config);
  }
  @Override
  public StoreQuery query() {
    return new StoreQueryImpl(config);
  }
  @Override
  public StoreGid getGid() {
    return config.getGid();
  }
  @Override
  public StoreRepo repo() {
    return new StoreRepo() {
      private String repoName;
      private String headName;
      @Override public StoreRepo repoName(String repoName) { this.repoName = repoName; return this; }
      @Override public StoreRepo headName(String headName) { this.headName = headName; return this; }
      @Override public Uni<ClientStore> create() { return createRepo(repoName, headName); }
      @Override public ClientStore build() { return createClientStore(repoName, headName); }
      @Override public Uni<Boolean> createIfNot() { return createRepoOrGetRepo(); }
    };
  }
  
  public Uni<Boolean> createRepoOrGetRepo() {
    final var client = config.getClient();
    
    return client.git().project().projectName(config.getRepoName()).get().onItem().transformToUni(repo -> {
      if(repo == null) {
        return client.repo().projectBuilder().name(config.getRepoName(), RepoType.git).build().onItem().transform(newRepo -> true); 
      }
      return Uni.createFrom().item(true);
    });
  }
  
  public Uni<ClientStore> createRepo(String repoName, String headName) {
    ServiceAssert.notNull(repoName, () -> "repoName must be defined!");
    final var client = config.getClient();
    final var newRepo = client.repo().projectBuilder().name(repoName, RepoType.git).build();
    return newRepo.onItem().transform((repoResult) -> {
      if(repoResult.getStatus() != RepoStatus.OK) {
        throw new StoreException("REPO_CREATE_FAIL", null, 
            ImmutableStoreExceptionMsg.builder()
            .id(repoResult.getStatus().toString())
            .value(repoName)
            .addAllArgs(repoResult.getMessages().stream().map(message->message.getText()).collect(Collectors.toList()))
            .build()); 
      }
      
      return createClientStore(repoName, headName);
    });
  }
  
  public ClientStore createClientStore(String repoName, String headName) {
    ServiceAssert.notNull(repoName, () -> "repoName must be defined!");
    return new ClientStorePostgreSQL(ImmutableDocDBConfig.builder()
        .from(config)
        .repoName(repoName)
        .headName(headName == null ? config.getHeadName() : headName)
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
    private ClientStore.StoreGid gidProvider;
    private DocDBConfig.DocDBAuthorProvider authorProvider;
    private io.vertx.mutiny.pgclient.PgPool pgPool;
    private String pgHost;
    private String pgDb;
    private Integer pgPort;
    private String pgUser;
    private String pgPass;
    private Integer pgPoolSize;
    
    private ClientStore.StoreGid getGidProvider() {
      return this.gidProvider == null ? type -> {
        return OidUtils.gen();
     } : this.gidProvider;
    }
    
    private DocDBConfig.DocDBAuthorProvider getAuthorProvider() {
      return this.authorProvider == null ? ()-> "not-configured" : this.authorProvider;
    }

    private ObjectMapper getObjectMapper() {
      if(this.objectMapper != null) {
        return this.objectMapper;
      }
      return new ObjectMapper()
        .registerModules(
          new GuavaModule(),
          new JavaTimeModule(),
          new Jdk8Module()
        );
    }
    
    public ClientStorePostgreSQL build() {
      ServiceAssert.notNull(repoName, () -> "repoName must be defined!");
    
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
      
      final DocDB thena;
      if(pgPool == null) {
        ServiceAssert.notNull(pgHost, () -> "pgHost must be defined!");
        ServiceAssert.notNull(pgPort, () -> "pgPort must be defined!");
        ServiceAssert.notNull(pgDb, () -> "pgDb must be defined!");
        ServiceAssert.notNull(pgUser, () -> "pgUser must be defined!");
        ServiceAssert.notNull(pgPass, () -> "pgPass must be defined!");
        ServiceAssert.notNull(pgPoolSize, () -> "pgPoolSize must be defined!");
        
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
      
      final ObjectMapper objectMapper = getObjectMapper();
      final DocDBConfig config = ImmutableDocDBConfig.builder()
          .client(thena).repoName(repoName).headName(headName)
          .gid(getGidProvider())
          .serializer((entity) -> {
            try {
              return new JsonObject(objectMapper.writeValueAsString(ImmutableStoreEntity.builder().from(entity).build()));
            } catch (IOException e) {
              throw new RuntimeException(e.getMessage(), e);
            }
          })
          .deserializer(new DocDBDeserializer(objectMapper))
          .authorProvider(getAuthorProvider())
          .build();
      return new ClientStorePostgreSQL(config);
    }
  }
}
