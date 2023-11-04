package io.resys.thena.docdb.file.spi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

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

import io.resys.thena.docdb.api.models.Repo;
import io.resys.thena.docdb.file.FileBuilder.RepoFileBuilder;
import io.resys.thena.docdb.file.tables.ImmutableFileStatement;
import io.resys.thena.docdb.file.tables.ImmutableFileTuple;
import io.resys.thena.docdb.file.tables.ImmutableRepoTableRow;
import io.resys.thena.docdb.file.tables.RepoTable.RepoTableRow;
import io.resys.thena.docdb.file.tables.Table;
import io.resys.thena.docdb.file.tables.Table.FileStatement;
import io.resys.thena.docdb.file.tables.Table.FileTuple;
import io.resys.thena.docdb.spi.ClientCollections;
import io.vertx.mutiny.sqlclient.Tuple;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RepoFileBuilderImpl implements RepoFileBuilder {
  private final ClientCollections ctx;

  @Override
  public FileTuple exists() {
    return ImmutableFileTuple.builder()
        .value("Does REPO table exist")
        .command((conn) -> {
          return Arrays.asList(new Table.RowExists() {
            public boolean getExists() {
              return conn.getRepoTable(ctx).getExists();
            }
          });          
        })
        .props(Tuple.of("repo"))
        .build();
  }  
  @Override
  public FileStatement create() {
    return ImmutableFileStatement.builder()
        .value("create REPO table if it does not exist")
        .command((root) -> {
          root.getRepoTable(ctx).create();
          return Arrays.asList(new Table.Row() {});
        })
        .build();
  }
  
  @Override
  public FileStatement findAll() {
    return ImmutableFileStatement.builder()
        .value("select all from REPO table")
        .command((root) -> 
          new ArrayList<>(root.getRepoTable(ctx).getRows())
        )
        .build();
  }
  @Override
  public FileTuple getByName(String name) {
    return ImmutableFileTuple.builder()
        .value("select from REPO table by name")
        .command((root) -> 
          root.getRepoTable(ctx)
          .getRows().stream().filter((RepoTableRow r) -> r.getName().equals(name))
          .collect(Collectors.toList())
        )
        .props(Tuple.of(name))
        .build();
  }
  @Override
  public FileTuple getByNameOrId(String name) {
    return ImmutableFileTuple.builder()
        .value("select from REPO table by name or id")
        .command((root) -> {
          return root.getRepoTable(ctx)
            .getRows().stream().filter((RepoTableRow r) -> r.getName().equals(name) || r.getId().equals(name))
            .collect(Collectors.toList());
        })
        .props(Tuple.of(name))
        .build();
  }
  @Override
  public FileTuple insertOne(Repo newRepo) {
    return ImmutableFileTuple.builder()
        .value("Insert new row into REPO table")
        .command((root) -> {
          
          return Arrays.asList(root.getRepoTable(ctx).insert(ImmutableRepoTableRow.builder()
              .id(newRepo.getId())
              .rev(newRepo.getRev())
              .prefix(newRepo.getPrefix())
              .name(newRepo.getName())
              .type(newRepo.getType())
              .build())
              );
        })
        .props(Tuple.of(newRepo.getId(), newRepo.getRev(), newRepo.getPrefix(), newRepo.getName(), newRepo.getType()))
        .build();
  }
}
