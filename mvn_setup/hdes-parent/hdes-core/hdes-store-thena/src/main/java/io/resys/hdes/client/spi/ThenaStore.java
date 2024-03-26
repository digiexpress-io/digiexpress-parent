package io.resys.hdes.client.spi;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.resys.hdes.client.api.HdesStore;
import io.resys.hdes.client.api.ImmutableBranch;
import io.resys.hdes.client.api.ImmutableStoreEntity;
import io.resys.hdes.client.api.ImmutableStoreExceptionMsg;
import io.resys.hdes.client.api.exceptions.StoreException;
import io.resys.hdes.client.spi.store.BlobDeserializer;
import io.resys.hdes.client.spi.store.ImmutableThenaConfig;
import io.resys.hdes.client.spi.store.ThenaConfig;
import io.resys.hdes.client.spi.store.ThenaStoreTemplate;
import io.resys.hdes.client.spi.util.HdesAssert;
import io.resys.thena.api.ThenaClient;
import io.resys.thena.api.envelope.QueryEnvelope.QueryEnvelopeStatus;
import io.resys.thena.storesql.DbStateSqlImpl;
import io.resys.thena.storesql.PgErrors;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.sqlclient.PoolOptions;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ThenaStore extends ThenaStoreTemplate implements HdesStore {

  public ThenaStore(ThenaConfig config) {
    super(config);
  }

  @Override
  public HdesStore withRepo(String repoName, String headName) {
    return new ThenaStore(ImmutableThenaConfig.builder().from(config).repoName(repoName).headName(headName).build());
  }
  @Override
  protected HdesStore createWithNewConfig(ThenaConfig config) {
    return new ThenaStore(config);
  }
  @Override
  public BranchQuery queryBranches() {
    return new BranchQuery() {
      @Override
      public Uni<List<Branch>> findAll() {
        return getConfig().getClient().git(getRepoName()).project()
            .get().onItem().transform(objects -> {
              if(objects.getStatus() != QueryEnvelopeStatus.OK) {
                throw new StoreException("HDES_BRANCH_QUERY_FAIL", null, 
                    ImmutableStoreExceptionMsg.builder()
                    .id(objects.getRepo().getId())
                    .value(objects.getRepo().getName())
                    .addAllArgs(objects.getMessages().stream().map(message->message.getText()).collect(Collectors.toList()))
                    .build()); 
              }
              
              return objects.getObjects().getBranches().values().stream()
                  .map(branch -> {
                    final Branch result = ImmutableBranch.builder().commitId(branch.getCommit()).name(branch.getName()).build();
                    
                    return result;
                  })
                  .toList();
            });
      }
    };
  }
  
  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private String repoName;
    private String headName;
    private ObjectMapper objectMapper;
    private ThenaConfig.GidProvider gidProvider;
    private ThenaConfig.AuthorProvider authorProvider;
    private io.vertx.mutiny.pgclient.PgPool pgPool;
    private String pgHost;
    private String pgDb;
    private Integer pgPort;
    private String pgUser;
    private String pgPass;
    private Integer pgPoolSize;
    
    public Builder repoName(String repoName) {
      this.repoName = repoName;
      return this;
    }
    public Builder objectMapper(ObjectMapper objectMapper) {
      this.objectMapper = objectMapper;
      return this;
    }
    public Builder gidProvider(ThenaConfig.GidProvider gidProvider) {
      this.gidProvider = gidProvider;
      return this;
    }
    public Builder authorProvider(ThenaConfig.AuthorProvider authorProvider) {
      this.authorProvider = authorProvider;
      return this;
    }
    public Builder pgPool(io.vertx.mutiny.pgclient.PgPool pgPool) {
      this.pgPool = pgPool;
      return this;
    }
    public Builder headName(String headName) {
      this.headName = headName;
      return this;
    }
    public Builder pgHost(String pgHost) {
      this.pgHost = pgHost;
      return this;
    }
    public Builder pgDb(String pgDb) {
      this.pgDb = pgDb;
      return this;
    }
    public Builder pgPort(Integer pgPort) {
      this.pgPort = pgPort;
      return this;
    }
    public Builder pgUser(String pgUser) {
      this.pgUser = pgUser;
      return this;
    }
    public Builder pgPass(String pgPass) {
      this.pgPass = pgPass;
      return this;
    }
    public Builder pgPoolSize(Integer pgPoolSize) {
      this.pgPoolSize = pgPoolSize;
      return this;
    }
    
    
    private ThenaConfig.GidProvider getGidProvider() {
      return this.gidProvider == null ? type -> {
        return UUID.randomUUID().toString();
     } : this.gidProvider;
    }
    
    private ThenaConfig.AuthorProvider getAuthorProvider() {
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
    
    public ThenaStore build() {
      HdesAssert.notNull(repoName, () -> "repoName must be defined!");
    
      final var headName = this.headName == null ? "main": this.headName;
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
        HdesAssert.notNull(pgHost, () -> "pgHost must be defined!");
        HdesAssert.notNull(pgPort, () -> "pgPort must be defined!");
        HdesAssert.notNull(pgDb, () -> "pgDb must be defined!");
        HdesAssert.notNull(pgUser, () -> "pgUser must be defined!");
        HdesAssert.notNull(pgPass, () -> "pgPass must be defined!");
        HdesAssert.notNull(pgPoolSize, () -> "pgPoolSize must be defined!");
        
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
      final ThenaConfig config = ImmutableThenaConfig.builder()
          .client(thena).repoName(repoName).headName(headName)
          .gidProvider(getGidProvider())
          .serializer((entity) -> {
            try {
              return new JsonObject(objectMapper.writeValueAsString(ImmutableStoreEntity.builder().from(entity).hash("").build()));
            } catch (IOException e) {
              throw new RuntimeException(e.getMessage(), e);
            }
          })
          .deserializer(new BlobDeserializer(objectMapper))
          .authorProvider(getAuthorProvider())
          .build();
      return new ThenaStore(config);
    }
  }
}
