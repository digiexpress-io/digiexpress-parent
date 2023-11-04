package io.resys.thena.docdb.file.spi;

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
import java.util.Collections;
import java.util.stream.Collectors;

import io.resys.thena.docdb.api.models.ThenaGitObject.Tag;
import io.resys.thena.docdb.file.FileBuilder.TagFileBuilder;
import io.resys.thena.docdb.file.tables.ImmutableFileStatement;
import io.resys.thena.docdb.file.tables.ImmutableFileTuple;
import io.resys.thena.docdb.file.tables.ImmutableTagTableRow;
import io.resys.thena.docdb.file.tables.Table;
import io.resys.thena.docdb.file.tables.Table.FileStatement;
import io.resys.thena.docdb.file.tables.Table.FileTuple;
import io.resys.thena.docdb.spi.DbCollections;
import io.vertx.mutiny.sqlclient.Tuple;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TagFileBuilderImpl implements TagFileBuilder {
  
  private final DbCollections ctx;
  
  @Override
  public FileStatement create() {
    return ImmutableFileStatement.builder()
        .value("create TAG table if it does not exist")
        .command((root) -> {
          root.getRepoTable(ctx).getTags().create();
          return Arrays.asList(new Table.Row() {});
        })
        .build();
  }
  @Override
  public FileStatement constraints() {
    return ImmutableFileStatement.builder()
        .value("Apply constraints on table TAG")
        .command(conn -> Arrays.asList(new Table.Row() {}))
        .build();
  }
  
  @Override
  public FileStatement findAll() {
    return ImmutableFileStatement.builder()
        .value("Select all from TAG table")
        .command((root) -> root.getRepoTable(ctx).getTags().getRows())
        .build();
  }
  @Override
  public FileTuple getByName(String name) {
    return ImmutableFileTuple.builder()
        .value("Select by name from TAG table")
        .command((root) -> root.getRepoTable(ctx).getTags().getRows()
            .stream().filter(r -> r.getId().equals(name)).collect(Collectors.toList()))
        .props(Tuple.of(name))
        .build();
  }
  @Override
  public FileStatement getFirst() {
    return ImmutableFileStatement.builder()
        .value("Select first from TAG table")
        .command((root) -> {
          final var refs = root.getRepoTable(ctx).getTags().getRows();
          if(refs.isEmpty()) {
            return Collections.emptyList();
          }
          return Arrays.asList(refs.get(0));
        })
        .build();
  }
  @Override
  public FileTuple deleteByName(String name) {
    return ImmutableFileTuple.builder()
        .value("Delete row from TAG table")
        .command((root) -> {
          
          final var exists = root.getRepoTable(ctx).getTags().getRows()
            .stream().filter(r -> r.getId().equals(name))
            .findFirst();
          if(exists.isEmpty()) {
            throw new IllegalArgumentException("Can't find TAG with name: " + name);
          }
          
          root.getRepoTable(ctx).getTags().delete(exists.get());
          
          return Arrays.asList(exists.get());
        })
        .props(Tuple.of(name))
        .build();
  }
  @Override
  public FileTuple insertOne(Tag newTag) {
    return ImmutableFileTuple.builder()
        .value("Insert new row into TAG table")
        .command((root) -> {
          
          final var exists = root.getRepoTable(ctx).getTags().getRows()
            .stream().filter(r -> r.getId().equals(newTag.getName()))
            .findFirst();
          if(!exists.isEmpty()) {
            return Arrays.asList(exists.get());
          }
          
          final var newRow = ImmutableTagTableRow.builder()
              .id(newTag.getName()) 
              .commit(newTag.getCommit()) 
              .datetime(newTag.getDateTime().toString()) 
              .author(newTag.getAuthor())
              .message(newTag.getMessage())
              .build();
          root.getRepoTable(ctx).getTags().insert(newRow);
          
          return Arrays.asList(newRow);
        })
        .props(Tuple.of(newTag.getName(), newTag.getCommit(), newTag.getDateTime().toString(), newTag.getAuthor(), newTag.getMessage()))
        .build();
  }
}
