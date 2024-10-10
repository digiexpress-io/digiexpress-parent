package io.resys.thena.docdb.spi.mongo;

import java.util.List;

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

import java.util.stream.Collectors;

import com.mongodb.client.model.Filters;

import io.resys.thena.docdb.api.models.Objects.Blob;
import io.resys.thena.docdb.api.models.Objects.Tree;
import io.resys.thena.docdb.spi.ClientQuery.BlobQuery;
import io.resys.thena.docdb.spi.codec.BlobCodec;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

public class MongoBlobQuery implements BlobQuery {

  private final MongoClientWrapper wrapper;
  
  public MongoBlobQuery(MongoClientWrapper wrapper) {
    this.wrapper = wrapper;
  }
  @Override
  public Uni<Blob> id(String blobId) {
    final var ctx = wrapper.getNames();
    return this.wrapper.getClient()
        .getDatabase(ctx.getDb())
        .getCollection(ctx.getBlobs(), Blob.class)
        .find(Filters.eq(BlobCodec.ID, blobId))
        .collect().first();
  }
  @Override
  public Uni<List<Blob>> id(List<String> blobId) {
    final var ctx = wrapper.getNames();
    return this.wrapper.getClient()
        .getDatabase(ctx.getDb())
        .getCollection(ctx.getBlobs(), Blob.class)
        .find(Filters.in(BlobCodec.ID, blobId))
        .collect().asList();
  }
  @Override
  public Multi<Blob> find() {
    final var ctx = wrapper.getNames();
    return this.wrapper.getClient()
        .getDatabase(ctx.getDb())
        .getCollection(ctx.getBlobs(), Blob.class)
        .find();
  }
  @Override
  public Multi<Blob> find(Tree tree) {
    final var ctx = wrapper.getNames();
    return this.wrapper.getClient()
        .getDatabase(ctx.getDb())
        .getCollection(ctx.getBlobs(), Blob.class)
        .find(Filters.or(
            tree.getValues().values().stream()
            .map(e -> Filters.eq(e.getBlob()))
            .collect(Collectors.toList())));
  }
}
