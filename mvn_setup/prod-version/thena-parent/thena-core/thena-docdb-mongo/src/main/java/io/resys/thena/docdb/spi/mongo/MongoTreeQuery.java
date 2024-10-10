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

import io.resys.thena.docdb.api.models.Objects.Tree;
import io.resys.thena.docdb.spi.ClientQuery.TreeQuery;
import io.resys.thena.docdb.spi.codec.TreeCodec;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

public class MongoTreeQuery implements TreeQuery {

  private final MongoClientWrapper wrapper;
  
  public MongoTreeQuery(MongoClientWrapper wrapper) {
    this.wrapper = wrapper;
  }
  @Override
  public Uni<Tree> id(String tree) {
    final var ctx = wrapper.getNames();
    return wrapper.getClient()
        .getDatabase(ctx.getDb())
        .getCollection(ctx.getTrees(), Tree.class)
        .find(Filters.eq(TreeCodec.ID, tree))
        .collect().first();
  }
  @Override
  public Multi<Tree> find() {
    final var ctx = wrapper.getNames();
    return wrapper.getClient()
        .getDatabase(ctx.getDb())
        .getCollection(ctx.getTrees(), Tree.class)
        .find();
  }
}
