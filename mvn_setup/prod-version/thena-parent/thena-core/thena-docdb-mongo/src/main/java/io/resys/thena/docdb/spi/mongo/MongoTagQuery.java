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

import io.resys.thena.docdb.api.models.Objects.Tag;
import io.resys.thena.docdb.spi.ClientQuery.DeleteResult;
import io.resys.thena.docdb.spi.ClientQuery.TagQuery;
import io.resys.thena.docdb.spi.ImmutableDeleteResult;
import io.resys.thena.docdb.spi.codec.TagCodec;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

public class MongoTagQuery implements TagQuery {

  private final MongoClientWrapper wrapper;
  private String name;
  
  public MongoTagQuery(MongoClientWrapper wrapper) {
    this.wrapper = wrapper;
  }
  @Override
  public TagQuery name(String name) {
    this.name = name;
    return this;
  }
  @Override
  public Uni<DeleteResult> delete() {
    final var ctx = this.wrapper.getNames();
    return this.wrapper.getClient()
        .getDatabase(ctx.getDb())
        .getCollection(ctx.getTags(), Tag.class)
        .deleteOne(Filters.eq(TagCodec.ID, name))
        .onItem()
        .transform(result -> ImmutableDeleteResult.builder().deletedCount(result.getDeletedCount()).build());
  }
  @Override
  public Uni<Tag> get() {
    return find().collect().first();
  }
  @Override
  public Multi<Tag> find() {
    final var ctx = this.wrapper.getNames();
    if(name == null || name.isBlank()) {
      return this.wrapper.getClient()
          .getDatabase(ctx.getDb())
          .getCollection(ctx.getTags(), Tag.class)
          .find();      
    }
    return this.wrapper.getClient()
        .getDatabase(ctx.getDb())
        .getCollection(ctx.getTags(), Tag.class)
        .find(Filters.eq(TagCodec.ID, name));
  }
}
