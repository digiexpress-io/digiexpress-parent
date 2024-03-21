package io.resys.thena.docdb.storefile.spi;

/*-
 * #%L
 * thena-docdb-api
 * %%
 * Copyright (C) 2021 - 2023 Copyright 2021 ReSys OÜ
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

import java.util.Arrays;
import java.util.stream.Collectors;

import io.resys.thena.docdb.api.models.ThenaGitObject.Tree;
import io.resys.thena.docdb.spi.DbCollections;
import io.resys.thena.docdb.storefile.FileBuilder.TreeFileBuilder;
import io.resys.thena.docdb.storefile.tables.ImmutableFileStatement;
import io.resys.thena.docdb.storefile.tables.ImmutableFileTuple;
import io.resys.thena.docdb.storefile.tables.ImmutableTreeTableRow;
import io.resys.thena.docdb.storefile.tables.Table;
import io.resys.thena.docdb.storefile.tables.Table.FileStatement;
import io.resys.thena.docdb.storefile.tables.Table.FileTuple;
import io.vertx.mutiny.sqlclient.Tuple;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TreeFileBuilderImpl implements TreeFileBuilder {

  private final DbCollections ctx;
  
  @Override
  public FileStatement create() {
    return ImmutableFileStatement.builder()
        .value("create TREE table if it does not exist")
        .command((root) -> {
          root.getRepoTable(ctx).getTrees().create();
          return Arrays.asList(new Table.Row() {});
        })
        .build();
  }
  @Override
  public FileStatement findAll() {
    return ImmutableFileStatement.builder()
        .value("Select all from TREE table")
        .command((root) -> {
          return root.getRepoTable(ctx).getTrees().getRows();
        })
        .build();
  }
  @Override
  public FileTuple getById(String id) {
    return ImmutableFileTuple.builder()
        .value("Select by id from TREE table")
        .props(Tuple.of(id))
        .command((root) -> {
          return root.getRepoTable(ctx).getTrees().getRows().stream()
              .filter(tree -> tree.getId().equals(id))
              .collect(Collectors.toList());
        })
        .build();
  }
  @Override
  public FileTuple insertOne(Tree src) {
    return ImmutableFileTuple.builder()
        .value("Insert row into TREE table")
        .props(Tuple.of(src.getId()))
        .command((root) -> {
          final var exists = root.getRepoTable(ctx).getTrees().getRows().stream()
              .filter(tree -> tree.getId().equals(src.getId()))
              .collect(Collectors.toList());
          if(!exists.isEmpty()) {
            return exists;
          }
          final var newRow = root.getRepoTable(ctx).getTrees().insert(ImmutableTreeTableRow.builder()
              .id(src.getId())
              .build());
          
          return Arrays.asList(newRow);
        })
        .build();
  }
}
