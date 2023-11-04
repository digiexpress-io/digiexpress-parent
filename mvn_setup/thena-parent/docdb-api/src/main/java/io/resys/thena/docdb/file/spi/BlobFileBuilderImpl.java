package io.resys.thena.docdb.file.spi;

import java.util.ArrayList;
import java.util.Arrays;

/*-
 * #%L
 * thena-docdb-pgsql
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

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import io.resys.thena.docdb.api.models.ThenaGitObject.Blob;
import io.resys.thena.docdb.file.FileBuilder.BlobFileBuilder;
import io.resys.thena.docdb.file.tables.BlobTable.BlobTableRow;
import io.resys.thena.docdb.file.tables.ImmutableBlobTableRow;
import io.resys.thena.docdb.file.tables.ImmutableFileStatement;
import io.resys.thena.docdb.file.tables.ImmutableFileTuple;
import io.resys.thena.docdb.file.tables.ImmutableFileTupleList;
import io.resys.thena.docdb.file.tables.Table;
import io.resys.thena.docdb.file.tables.Table.FileStatement;
import io.resys.thena.docdb.file.tables.Table.FileTuple;
import io.resys.thena.docdb.file.tables.Table.FileTupleList;
import io.resys.thena.docdb.spi.DbCollections;
import io.vertx.mutiny.sqlclient.Tuple;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class BlobFileBuilderImpl implements BlobFileBuilder {
  
  private final DbCollections ctx;
  
  @Override
  public FileStatement create() {
    return ImmutableFileStatement.builder()
        .value("create BLOB table if it does not exist")
        .command((root) -> {
          root.getRepoTable(ctx).getBlobs().create();
          return Arrays.asList(new Table.Row() {});
        })
        .build();
  }
  @Override
  public FileStatement findAll() {
    return ImmutableFileStatement.builder()
        .value("Select all from BLOB table")
        .command((root) -> root.getRepoTable(ctx).getBlobs().getRows())
        .build();
  }
  @Override
  public FileTuple getById(String blobId) {
    return ImmutableFileTuple.builder()
        .value("Select by id from BLOB table")
        .command((root) -> root.getRepoTable(ctx).getBlobs().getRows()
            .stream().filter(r -> r.getId().equals(blobId)).collect(Collectors.toList()))
        .props(Tuple.of(blobId))
        .build();
  }
  @Override
  public FileTuple findByIds(Collection<String> blobId) {
    List<String> tuple = new ArrayList<>();
    for(final var id : blobId) {
      tuple.add(id);
    }
    
    return ImmutableFileTuple.builder()
        .value("Select by multiple id-s from BLOB table")
        .command((root) -> root.getRepoTable(ctx).getBlobs().getRows()
            .stream().filter(r -> blobId.contains(r.getId())).collect(Collectors.toList()))
        .props(Tuple.from(tuple))
        .build();
  }
  @Override
  public FileTuple findByTreeId(String tree) {
    return ImmutableFileTuple.builder()
        .value("Select by tree from BLOB and TREE_ITEM table")
        .command((root) -> {
          final var ids = root.getRepoTable(ctx).getTreeItems().getRows().stream()
            .filter(item -> item.getTree().equals(tree))
            .map(item -> item.getBlob())
            .collect(Collectors.toList());
          
          return root.getRepoTable(ctx).getBlobs().getRows()
              .stream().filter(r -> ids.contains(r.getId()))
              .collect(Collectors.toList());
          
        })
        .props(Tuple.of(tree))
        .build();
  }
  @Override
  public FileTuple insertOne(Blob blob) {
    return ImmutableFileTuple.builder()
        .value("Insert new row into BLOB table")
        .command((root) -> {
          
          final var exists = root.getRepoTable(ctx).getBlobs().getRows()
            .stream().filter(r -> r.getId().equals(blob.getId()))
            .findFirst();
          if(!exists.isEmpty()) {
            return Arrays.asList(exists.get());
          }
          
          final var newRow = ImmutableBlobTableRow.builder().id(blob.getId()).value(blob.getValue()).build();
          root.getRepoTable(ctx).getBlobs().insert(newRow);
          
          return Arrays.asList(newRow);
        })
        .props(Tuple.of(blob.getId(), blob.getValue()))
        .build();
  }
  @Override
  public FileTupleList insertAll(Collection<Blob> blobs) {
    return ImmutableFileTupleList.builder()
        .value("Insert new rows into BLOB table")
        .command((root) -> {
          final var byId = root.getRepoTable(ctx).getBlobs()
              .getRows().stream()
              .collect(Collectors.toMap(e -> e.getId(), e -> e));
          
          final var inserts = new ArrayList<BlobTableRow>();
          final var results = new ArrayList<BlobTableRow>();
          
          for (final var blob : blobs) {

            if (byId.containsKey(blob.getId())) {
              results.add(byId.get(blob.getId()));
            } else {
              final var newRow = ImmutableBlobTableRow.builder().id(blob.getId()).value(blob.getValue()).build();
              inserts.add(newRow);
              results.add(newRow);

            }
          }
          
          root.getRepoTable(ctx).getBlobs().insertAll(inserts);
          return results;
        })
        .props(blobs.stream()
            .map(v -> Tuple.of(v.getId(), v.getValue()))
            .collect(Collectors.toList()))
        .build();
  }
}
