package io.resys.thena.docdb.spi;

/*-
 * #%L
 * thena-docdb-mongo
 * %%
 * Copyright (C) 2021 Copyright 2021 ReSys OÜ
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

import io.quarkus.mongodb.reactive.ReactiveMongoClient;
import io.resys.thena.docdb.api.DocDB;
import io.resys.thena.docdb.api.models.Repo;
import io.resys.thena.docdb.spi.mongo.ImmutableMongoClientWrapper;
import io.resys.thena.docdb.spi.mongo.MongoClientInsertBuilder;
import io.resys.thena.docdb.spi.mongo.MongoClientQuery;
import io.resys.thena.docdb.spi.mongo.MongoRepoBuilder;
import io.resys.thena.docdb.spi.support.RepoAssert;
import io.smallrye.mutiny.Uni;

public class DocDBFactory {
  
  public static Builder create() {
    return new Builder();
  }
  
  public static ClientState state(ClientCollections ctx, ReactiveMongoClient client) {
    final var handler = new ErrorHandler() {
      @Override
      public boolean notFound(Throwable e) {
        return false;
      }
      @Override
      public boolean duplicate(Throwable e) {
        return false;
      }
      @Override
      public void deadEnd(String additionalMsg) {
      }
      @Override
      public void deadEnd(String additionalMsg, Throwable e) {
      }
    };
    
    return new ClientState() {
      @Override
      public ClientCollections getCollections() {
        return ctx;
      }
      @Override
      public RepoBuilder repos() {
        return new MongoRepoBuilder(client, ctx);
      }
      @Override
      public Uni<ClientInsertBuilder> insert(String repoNameOrId) {
        return repos().getByNameOrId(repoNameOrId).onItem().transform(repo -> insert(repo));
      }
      @Override
      public ClientInsertBuilder insert(Repo repo) {
        final var wrapper = ImmutableMongoClientWrapper.builder()
            .repo(repo)
            .client(client)
            .names(ctx.toRepo(repo))
            .build();
        return new MongoClientInsertBuilder(wrapper);
      }
      @Override
      public Uni<ClientQuery> query(String repoNameOrId) {
        return repos().getByNameOrId(repoNameOrId).onItem().transform(repo -> query(repo));
      }
      @Override
      public ClientQuery query(Repo repo) {
        final var wrapper = ImmutableMongoClientWrapper.builder()
            .repo(repo)
            .client(client)
            .names(ctx.toRepo(repo))
            .build();
        return new MongoClientQuery(wrapper);
      }
      @Override
      public ClientRepoState withRepo(Repo repo) {
        final var wrapper = ImmutableMongoClientWrapper.builder()
            .repo(repo)
            .client(client)
            .names(ctx.toRepo(repo))
            .build();
        return new ClientRepoState() {
          @Override
          public ClientQuery query() {
            return new MongoClientQuery(wrapper);
          }
          @Override
          public ClientInsertBuilder insert() {
            return new MongoClientInsertBuilder(wrapper);
          }
        };
      }
      @Override
      public Uni<ClientRepoState> withRepo(String repoNameOrId) {
        return repos().getByNameOrId(repoNameOrId).onItem().transform(repo -> withRepo(repo));
      }
      @Override
      public ErrorHandler getErrorHandler() {
        return handler;
      }
    };
  }
  
  public static class Builder {
    private ReactiveMongoClient client;
    private String db = "docdb";

    public Builder db(String db) {
      this.db = db;
      return this;
    }
    public Builder client(ReactiveMongoClient client) {
      this.client = client;
      return this;
    }
    
    
    public DocDB build() {
      RepoAssert.notNull(client, () -> "client must be defined!");
      RepoAssert.notNull(db, () -> "db must be defined!");
      final var ctx = ClientCollections.defaults(db);
      return new DocDBDefault(state(ctx, client));
    }
  }
}
