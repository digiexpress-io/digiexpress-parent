package io.resys.hdes.client.spi;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/*-
 * #%L
 * hdes-client-api
 * %%
 * Copyright (C) 2020 - 2022 Copyright 2020 ReSys OÜ
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

import groovy.util.logging.Slf4j;
import io.resys.hdes.client.api.HdesStore;
import io.resys.hdes.client.api.ImmutableBranch;
import io.resys.hdes.client.api.ImmutableStoreEntity;
import io.resys.hdes.client.spi.store.BlobDeserializer;
import io.resys.hdes.client.spi.store.ImmutableThenaConfig;
import io.resys.hdes.client.spi.store.ThenaConfig;
import io.resys.hdes.client.spi.store.ThenaConfig.AuthorProvider;
import io.resys.hdes.client.spi.store.ThenaConfig.GidProvider;
import io.resys.hdes.client.spi.store.ThenaStoreTemplate;
import io.resys.hdes.client.spi.util.HdesAssert;
import io.resys.thena.docdb.api.ThenaClient;
import io.resys.thena.docdb.storefile.DocDBFactoryFile;
import io.resys.thena.docdb.storefile.FileErrors;
import io.resys.thena.docdb.storefile.spi.FilePoolImpl;
import io.resys.thena.docdb.storefile.tables.Table.FilePool;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;

@lombok.extern.slf4j.Slf4j
public class HdesStoreFileImpl extends ThenaStoreTemplate implements HdesStore {

  public HdesStoreFileImpl(ThenaConfig config) {
    super(config);
  }

  @Override
  public HdesStore withRepo(String repoName, String headName) {
    return new HdesStoreFileImpl(ImmutableThenaConfig.builder().from(config).repoName(repoName).headName(headName).build());
  }
  @Override
  protected HdesStoreFileImpl createWithNewConfig(ThenaConfig config) {
    return new HdesStoreFileImpl(config);
  }
  
  public static Builder builder() {
    return new Builder();
  }

  @Slf4j
  public static class Builder {
    private String repoName;
    private String headName;
    private ObjectMapper objectMapper;
    private GidProvider gidProvider;
    private AuthorProvider authorProvider;
    private String db;
    private FilePool pool;
    
    public Builder repoName(String repoName) {
      this.repoName = repoName;
      return this;
    }
    public Builder objectMapper(ObjectMapper objectMapper) {
      this.objectMapper = objectMapper;
      return this;
    }
    public Builder gidProvider(GidProvider gidProvider) {
      this.gidProvider = gidProvider;
      return this;
    }
    public Builder authorProvider(AuthorProvider authorProvider) {
      this.authorProvider = authorProvider;
      return this;
    }
    public Builder pgPool(FilePool pgPool) {
      this.pool = pgPool;
      return this;
    }
    public Builder headName(String headName) {
      this.headName = headName;
      return this;
    }
    public Builder db(String pgDb) {
      this.db = pgDb;
      return this;
    }
    
    private GidProvider getGidProvider() {
      return this.gidProvider == null ? type -> {
        return UUID.randomUUID().toString();
     } : this.gidProvider;
    }
    
    private AuthorProvider getAuthorProvider() {
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
    
    public HdesStoreFileImpl build() {
      HdesAssert.notNull(repoName, () -> "repoName must be defined!");
      final var objectMapper = getObjectMapper();
      
      final var headName = this.headName == null ? "main": this.headName;
      if(log.isDebugEnabled()) {
        log.debug("""
          Configuring Thena:
            repoName: {}
            headName: {}
            objectMapper: {}
            gidProvider: {}
            authorProvider: {}
            db: {}
          """,
          this.repoName,
          headName,
          this.objectMapper == null ? "configuring" : "provided",
          this.gidProvider == null ? "configuring" : "provided",
          this.authorProvider == null ? "configuring" : "provided",
          this.db);
      }
      
      final ThenaClient thena;
      if(pool == null) {
        HdesAssert.notNull(db, () -> "asset direction for db must be defined!");
        final var pgPool = new FilePoolImpl(new File(db), objectMapper);
        
        thena = DocDBFactoryFile.create().client(pgPool).db(repoName).errorHandler(new FileErrors()).build();
      } else {
        thena = DocDBFactoryFile.create().client(pool).db(repoName).errorHandler(new FileErrors()).build();
      }
      
      final ImmutableThenaConfig config = ImmutableThenaConfig.builder()
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
      return new HdesStoreFileImpl(config);
    }
  }

  @Override
  public BranchQuery queryBranches() {
    return new BranchQuery() {
      
      @Override
      public Uni<List<Branch>> findAll() {
        return Uni.createFrom().item(Arrays.asList(ImmutableBranch.builder().name(getHeadName()).commitId("file-system").build()));
      }
    };
  }
}
