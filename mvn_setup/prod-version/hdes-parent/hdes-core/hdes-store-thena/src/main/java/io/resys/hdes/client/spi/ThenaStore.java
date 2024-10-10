package io.resys.hdes.client.spi;

/*-
 * #%L
 * hdes-store-thena
 * %%
 * Copyright (C) 2020 - 2023 Copyright 2020 ReSys OÜ
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
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.resys.hdes.client.api.HdesStore;
import io.resys.hdes.client.api.ImmutableStoreEntity;
import io.resys.hdes.client.spi.store.BlobDeserializer;
import io.resys.hdes.client.spi.store.ImmutableThenaConfig;
import io.resys.hdes.client.spi.store.ThenaConfig;
import io.resys.hdes.client.spi.store.ThenaStoreTemplate;
import io.resys.hdes.client.spi.util.HdesAssert;
import io.resys.thena.docdb.api.DocDB;
import io.resys.thena.docdb.spi.pgsql.PgErrors;
import io.resys.thena.docdb.sql.DocDBFactorySql;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.SslMode;
import io.vertx.sqlclient.PoolOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class ThenaStore extends ThenaStoreTemplate implements HdesStore {
  private static final Logger LOGGER = LoggerFactory.getLogger(ThenaStore.class);
  private final Optional<String> branchName;

  public ThenaStore(ThenaConfig config) {
    super(config);
    this.branchName = Optional.empty();
  }

  public ThenaStore(ThenaConfig config, String branchName) {
    super(config);
    this.branchName = Optional.ofNullable(branchName);
  }

  @Override
  protected HdesStore createWithNewConfig(ThenaConfig config) {
    return new ThenaStore(config);
  }
  public static Builder builder() {
    return new Builder();
  }

  @Override
  public Optional<String> getBranchName() {
    return branchName;
  }

  @Override
  public HdesStore withBranch(String branchName) {
    Objects.requireNonNull(branchName, () -> "branchName can't be null!");
    return new ThenaStore(config, branchName);
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
    // value from io.vertx.pgclient.SslMode
    private String pgSslMode;
    
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
    public Builder pgSslMode(String pgSslMode) {
      this.pgSslMode = pgSslMode;
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
      if(this.objectMapper == null) {
        return this.objectMapper;
      }
      
      final ObjectMapper objectMapper = new ObjectMapper();
      objectMapper.registerModule(new GuavaModule());
      objectMapper.registerModule(new JavaTimeModule());
      objectMapper.registerModule(new Jdk8Module());
      return objectMapper;
    }
    
    public ThenaStore build() {
      HdesAssert.notNull(repoName, () -> "repoName must be defined!");
    
      final var headName = this.headName == null ? "main": this.headName;
      if(LOGGER.isDebugEnabled()) {
        final String ls = System.lineSeparator();
        final var log = new StringBuilder()
          .append(ls)
          .append("Configuring Thena: ").append(ls)
          .append("  repoName: '").append(this.repoName).append("'").append(ls)
          .append("  headName: '").append(headName).append("'").append(ls)
          .append("  objectMapper: '").append(this.objectMapper == null ? "configuring" : "provided").append("'").append(ls)
          .append("  gidProvider: '").append(this.gidProvider == null ? "configuring" : "provided").append("'").append(ls)
          .append("  authorProvider: '").append(this.authorProvider == null ? "configuring" : "provided").append("'").append(ls)
          
          .append("  pgPool: '").append(this.pgPool == null ? "configuring" : "provided").append("'").append(ls)
          .append("  pgPoolSize: '").append(this.pgPoolSize).append("'").append(ls)
          .append("  sslMode: '").append(this.pgSslMode).append("'").append(ls)
          .append("  pgHost: '").append(this.pgHost).append("'").append(ls)
          .append("  pgPort: '").append(this.pgPort).append("'").append(ls)
          .append("  pgDb: '").append(this.pgDb).append("'").append(ls)
          .append("  pgUser: '").append(this.pgUser == null ? "null" : "***").append("'").append(ls)
          .append("  pgPass: '").append(this.pgPass == null ? "null" : "***").append("'").append(ls);
          
        LOGGER.debug(log.toString());
      }
      
      final DocDB thena;
      if(pgPool == null) {
        HdesAssert.notNull(pgHost, () -> "pgHost must be defined!");
        HdesAssert.notNull(pgPort, () -> "pgPort must be defined!");
        HdesAssert.notNull(pgDb, () -> "pgDb must be defined!");
        HdesAssert.notNull(pgUser, () -> "pgUser must be defined!");
        HdesAssert.notNull(pgPass, () -> "pgPass must be defined!");
        HdesAssert.notNull(pgPoolSize, () -> "pgPoolSize must be defined!");
        
        SslMode sslMode = SslMode.DISABLE;
        try {
          if (pgSslMode != null) {
            sslMode = SslMode.valueOf(pgSslMode);
          }
        }
        catch (IllegalArgumentException e){
          LOGGER.warn("Ssl mode parsing exception, disabling ssl", e);
        }
        
        final PgConnectOptions connectOptions = new PgConnectOptions()
            .setHost(pgHost)
            .setPort(pgPort)
            .setDatabase(pgDb)
            .setUser(pgUser)
            .setPassword(pgPass)
            .setSslMode(sslMode);
        final PoolOptions poolOptions = new PoolOptions()
            .setMaxSize(pgPoolSize);
        
        final io.vertx.mutiny.pgclient.PgPool pgPool = io.vertx.mutiny.pgclient.PgPool.pool(connectOptions, poolOptions);
        
        thena = DocDBFactorySql.create().client(pgPool).db(repoName).errorHandler(new PgErrors()).build();
      } else {
        thena = DocDBFactorySql.create().client(pgPool).db(repoName).errorHandler(new PgErrors()).build();
      }
      
      final ObjectMapper objectMapper = getObjectMapper();
      final ThenaConfig config = ImmutableThenaConfig.builder()
          .client(thena).repoName(repoName).headName(headName)
          .gidProvider(getGidProvider())
          .serializer((entity) -> {
            try {
              return objectMapper.writeValueAsString(ImmutableStoreEntity.builder().from(entity).hash("").build());
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
