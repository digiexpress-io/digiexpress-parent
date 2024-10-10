package io.resys.thena.docdb.spi.mongo;

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

import com.mongodb.client.model.Filters;
import com.mongodb.client.result.InsertOneResult;

import io.quarkus.mongodb.reactive.ReactiveMongoClient;
import io.resys.thena.docdb.api.models.Repo;
import io.resys.thena.docdb.spi.ClientCollections;
import io.resys.thena.docdb.spi.ClientState.RepoBuilder;
import io.resys.thena.docdb.spi.codec.RepoCodec;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

public class MongoRepoBuilder implements RepoBuilder {

  private final ReactiveMongoClient client;
  private final ClientCollections names;
  
  public MongoRepoBuilder(ReactiveMongoClient client, ClientCollections names) {
    super();
    this.client = client;
    this.names = names;
  }

  @Override
  public Uni<Repo> getByName(String name) {
    final var ctx = names;
    return client
        .getDatabase(ctx.getDb())
        .getCollection(ctx.getRepos(), Repo.class)
        .find(Filters.or(
            Filters.eq(RepoCodec.NAME, name)))
        .collect()
        .first();
  }

  @Override
  public Uni<Repo> getByNameOrId(String nameOrId) {
    final var ctx = names;
    return client
        .getDatabase(ctx.getDb())
        .getCollection(ctx.getRepos(), Repo.class)
        .find(Filters.or(
            Filters.eq(RepoCodec.ID, nameOrId),
            Filters.eq(RepoCodec.NAME, nameOrId)))
        .collect()
        .first();
  }

  @Override
  public Uni<Repo> insert(Repo newRepo) {
    final var ctx = names;
    return client
        .getDatabase(ctx.getDb())
        .getCollection(ctx.getRepos(), Repo.class)
        .insertOne(newRepo).onItem()
        .transform((InsertOneResult insertOne) -> newRepo);
  }

  @Override
  public Multi<Repo> find() {
    final var ctx = names;
    return client
        .getDatabase(ctx.getDb())
        .getCollection(ctx.getRepos(), Repo.class)
        .find();
  }

  @Override
  public Uni<Void> create() {
    final var ctx = names;
    return client
        .getDatabase(ctx.getDb())
        .createCollection(ctx.getRepos());
  }

}
