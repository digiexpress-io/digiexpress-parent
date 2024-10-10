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

import io.resys.thena.docdb.api.models.Objects.Ref;
import io.resys.thena.docdb.spi.ClientQuery.RefQuery;
import io.resys.thena.docdb.spi.codec.RefCodec;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

public class MongoRefQuery implements RefQuery {

  private final MongoClientWrapper wrapper;
  
  public MongoRefQuery(MongoClientWrapper wrapper) {
    this.wrapper = wrapper;
  }
  @Override
  public Uni<Ref> nameOrCommit(String refNameOrCommit) {
    final var ctx = wrapper.getNames();
    return this.wrapper.getClient()
        .getDatabase(ctx.getDb())
        .getCollection(ctx.getRefs(), Ref.class)
        .find(Filters.or(
            Filters.eq(RefCodec.NAME, refNameOrCommit),
            Filters.eq(RefCodec.COMMIT, refNameOrCommit)
        ))
        .collect()
        .first();
  }
  @Override
  public Uni<Ref> get() {
    return find().collect().first();
  }
  @Override
  public Multi<Ref> find() {
    final var ctx = wrapper.getNames();
    return this.wrapper.getClient()
        .getDatabase(ctx.getDb())
        .getCollection(ctx.getRefs(), Ref.class)
        .find();
  }
  @Override
  public Uni<Ref> name(String name) {
    final var ctx = wrapper.getNames();
    return this.wrapper.getClient()
        .getDatabase(ctx.getDb())
        .getCollection(ctx.getRefs(), Ref.class)
        .find(Filters.eq(RefCodec.NAME, name))
        .collect()
        .first();
  }
}
