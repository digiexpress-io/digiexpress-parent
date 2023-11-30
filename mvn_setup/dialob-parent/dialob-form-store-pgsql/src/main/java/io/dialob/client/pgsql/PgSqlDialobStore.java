package io.dialob.client.pgsql;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.dialob.client.api.DialobStore;
import io.dialob.client.api.ImmutableStoreEntity;
import io.dialob.client.spi.DialobStoreTemplate;
import io.dialob.client.spi.store.BlobDeserializer;
import io.dialob.client.spi.store.DialobStoreConfig;
import io.dialob.client.spi.store.ImmutableDialobStoreConfig;
import io.dialob.client.spi.support.DialobAssert;
import io.dialob.client.spi.support.OidUtils;
import io.resys.thena.docdb.api.DocDB;
import io.resys.thena.docdb.store.sql.DbStateSqlImpl;
import io.resys.thena.docdb.store.sql.PgErrors;
import io.vertx.core.json.JsonObject;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.sqlclient.PoolOptions;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class PgSqlDialobStore extends DialobStoreTemplate implements DialobStore {

  public PgSqlDialobStore(DialobStoreConfig config) {
    super(config);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private String repoName;
    private String headName;
    private ObjectMapper objectMapper;
    private DialobStoreConfig.GidProvider gidProvider;
    private DialobStoreConfig.AuthorProvider authorProvider;
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
    public Builder gidProvider(DialobStoreConfig.GidProvider gidProvider) {
      this.gidProvider = gidProvider;
      return this;
    }
    public Builder authorProvider(DialobStoreConfig.AuthorProvider authorProvider) {
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


    private DialobStoreConfig.GidProvider getGidProvider() {
      return this.gidProvider == null ? type -> {
        return OidUtils.gen();
     } : this.gidProvider;
    }

    private DialobStoreConfig.AuthorProvider getAuthorProvider() {
      return this.authorProvider == null ? ()-> "not-configured" : this.authorProvider;
    }

    private ObjectMapper getObjectMapper() {
      if(this.objectMapper == null) {
        return this.objectMapper;
      }

      final ObjectMapper objectMapper = new ObjectMapper();
      objectMapper.registerModule(new GuavaModule());
      objectMapper.registerModule(new JavaTimeModule());
      objectMapper.registerModule(new Jdk8Module());
      return objectMapper;
    }

    public PgSqlDialobStore build() {
      DialobAssert.notNull(repoName, () -> "repoName must be defined!");

      final var headName = this.headName == null ? "main": this.headName;
      if(log.isDebugEnabled()) {
        log.debug(new StringBuilder()
          .append(System.lineSeparator())
          .append("Configuring Thena: ").append(System.lineSeparator())
          .append("  repoName: '").append(this.repoName).append("'").append(System.lineSeparator())
          .append("  headName: '").append(headName).append("'").append(System.lineSeparator())
          .append("  objectMapper: '").append(this.objectMapper == null ? "configuring" : "provided").append("'").append(System.lineSeparator())
          .append("  gidProvider: '").append(this.gidProvider == null ? "configuring" : "provided").append("'").append(System.lineSeparator())
          .append("  authorProvider: '").append(this.authorProvider == null ? "configuring" : "provided").append("'").append(System.lineSeparator())

          .append("  pgPool: '").append(this.pgPool == null ? "configuring" : "provided").append("'").append(System.lineSeparator())
          .append("  pgPoolSize: '").append(this.pgPoolSize).append("'").append(System.lineSeparator())
          .append("  pgHost: '").append(this.pgHost).append("'").append(System.lineSeparator())
          .append("  pgPort: '").append(this.pgPort).append("'").append(System.lineSeparator())
          .append("  pgDb: '").append(this.pgDb).append("'").append(System.lineSeparator())
          .append("  pgUser: '").append(this.pgUser == null ? "null" : "***").append("'").append(System.lineSeparator())
          .append("  pgPass: '").append(this.pgPass == null ? "null" : "***").append("'").append(System.lineSeparator()).toString());
      }

      final DocDB thena;
      if(pgPool == null) {
        DialobAssert.notNull(pgHost, () -> "pgHost must be defined!");
        DialobAssert.notNull(pgPort, () -> "pgPort must be defined!");
        DialobAssert.notNull(pgDb, () -> "pgDb must be defined!");
        DialobAssert.notNull(pgUser, () -> "pgUser must be defined!");
        DialobAssert.notNull(pgPass, () -> "pgPass must be defined!");
        DialobAssert.notNull(pgPoolSize, () -> "pgPoolSize must be defined!");

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
      final DialobStoreConfig config = ImmutableDialobStoreConfig.builder()
          .client(thena).repoName(repoName).headName(headName)
          .gidProvider(getGidProvider())
          .serializer((entity) -> {
            try {
              return new JsonObject(objectMapper.writeValueAsString(ImmutableStoreEntity.builder().from(entity).build()));
            } catch (IOException e) {
              throw new RuntimeException(e.getMessage(), e);
            }
          })
          .deserializer(new BlobDeserializer(objectMapper))
          .authorProvider(getAuthorProvider())
          .build();
      return new PgSqlDialobStore(config);
    }
  }
}
