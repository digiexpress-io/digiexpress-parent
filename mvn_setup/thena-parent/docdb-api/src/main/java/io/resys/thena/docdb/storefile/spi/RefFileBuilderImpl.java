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
import java.util.Collections;
import java.util.stream.Collectors;

import io.resys.thena.docdb.api.models.ThenaGitObject.Branch;
import io.resys.thena.docdb.api.models.ThenaGitObject.Commit;
import io.resys.thena.docdb.spi.DbCollections;
import io.resys.thena.docdb.storefile.FileBuilder.RefFileBuilder;
import io.resys.thena.docdb.storefile.tables.ImmutableFileStatement;
import io.resys.thena.docdb.storefile.tables.ImmutableFileTuple;
import io.resys.thena.docdb.storefile.tables.ImmutableRefTableRow;
import io.resys.thena.docdb.storefile.tables.Table;
import io.resys.thena.docdb.storefile.tables.Table.FileStatement;
import io.resys.thena.docdb.storefile.tables.Table.FileTuple;
import io.vertx.mutiny.sqlclient.Tuple;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RefFileBuilderImpl implements RefFileBuilder {
  private final DbCollections ctx;
  
  @Override
  public FileStatement create() {
    return ImmutableFileStatement.builder()
        .value("create REF table if it does not exist")
        .command((root) -> {
          root.getRepoTable(ctx).getRefs().create();
          return Arrays.asList(new Table.Row() {});
        })
        .build();
  }
  @Override
  public FileStatement constraints() {
    return ImmutableFileStatement.builder()
        .value("Apply constraints on table REF")
        .command(conn -> Arrays.asList(new Table.Row() {}))
        .build();
  }
  
  @Override
  public FileStatement findAll() {
    return ImmutableFileStatement.builder()
        .value("Select all from REF table")
        .command((root) -> root.getRepoTable(ctx).getRefs().getRows())
        .build();
  }

  @Override
  public FileTuple getByName(String name) {
    return ImmutableFileTuple.builder()
        .value("Select by name from REF table")
        .command((root) -> root.getRepoTable(ctx).getRefs().getRows()
            .stream().filter(r -> r.getName().equals(name)).collect(Collectors.toList()))
        .props(Tuple.of(name))
        .build();
  }
  
  @Override
  public FileTuple getByNameOrCommit(String refNameOrCommit) {
    return ImmutableFileTuple.builder()
        .value("Select by name from REF table")
        .command((root) -> root.getRepoTable(ctx).getRefs()
            .getRows().stream()
            .filter(r -> r.getName().equals(refNameOrCommit) || r.getCommit().equals(refNameOrCommit))
            .collect(Collectors.toList()))
        .props(Tuple.of(refNameOrCommit))
        .build();
  }

  @Override
  public FileStatement getFirst() {
    return ImmutableFileStatement.builder()
        .value("Select first from REF table")
        .command((root) -> {
          final var refs = root.getRepoTable(ctx).getRefs().getRows();
          if(refs.isEmpty()) {
            return Collections.emptyList();
          }
          return Arrays.asList(refs.get(0));
        })
        .build();
  }

  @Override
  public FileTuple insertOne(Branch ref) {
    return ImmutableFileTuple.builder()
        .value("Insert new row into REF table")
        .command((root) -> {
          final var exists = root.getRepoTable(ctx).getRefs().getRows()
            .stream().filter(r -> r.getName().equals(ref.getName()))
            .findFirst();
          if(!exists.isEmpty()) {
            throw new IllegalArgumentException("REF already exists with name: " + ref.getName());
          }
          
          final var newRow = ImmutableRefTableRow.builder().name(ref.getName()).commit(ref.getCommit()).build();
          root.getRepoTable(ctx).getRefs().insert(newRow);
          return Arrays.asList(newRow);
        })
        .props(Tuple.of(ref.getName(), ref.getCommit()))
        .build();
  }

  @Override
  public FileTuple updateOne(Branch ref, Commit commit) {
    return ImmutableFileTuple.builder()
        .value("Update row in REF table")
        .command((root) -> {
          final var exists = root.getRepoTable(ctx).getRefs().getRows()
            .stream().filter(r -> r.getName().equals(ref.getName()) && r.getCommit().equals(commit.getParent().get()))
            .findFirst();
          if(exists.isEmpty()) {
            throw new IllegalArgumentException("REF does not exists with commit: " + ref.getCommit());
          }
          
          final var newRow = ImmutableRefTableRow.builder()
              .name(ref.getName())
              .commit(ref.getCommit())
              .build();
          root.getRepoTable(ctx).getRefs().update(exists.get(), newRow);
          return Arrays.asList(newRow);
        })
        .props(Tuple.of(ref.getCommit(), ref.getName(), commit.getParent().get()))
        .build();
    
  }
}
