package io.resys.thena.docdb.file.spi;

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

import java.util.Arrays;
import java.util.stream.Collectors;

import io.resys.thena.docdb.api.models.ThenaGitObject.Commit;
import io.resys.thena.docdb.file.FileBuilder.CommitFileBuilder;
import io.resys.thena.docdb.file.tables.ImmutableCommitTableRow;
import io.resys.thena.docdb.file.tables.ImmutableFileStatement;
import io.resys.thena.docdb.file.tables.ImmutableFileTuple;
import io.resys.thena.docdb.file.tables.Table;
import io.resys.thena.docdb.file.tables.Table.FileStatement;
import io.resys.thena.docdb.file.tables.Table.FileTuple;
import io.resys.thena.docdb.spi.ClientCollections;
import io.vertx.mutiny.sqlclient.Tuple;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CommitFileBuilderImpl implements CommitFileBuilder {

  private final ClientCollections ctx;
  
  @Override
  public FileStatement create() {
    return ImmutableFileStatement.builder()
        .value("create COMMIT table if it does not exist")
        .command((root) -> {
          root.getRepoTable(ctx).getCommits().create();
          return Arrays.asList(new Table.Row() {});
        })
        .build();
  }
  
  @Override
  public FileStatement constraints() {
    return ImmutableFileStatement.builder()
        .value("Apply constraints on table COMMIT")
        .command(conn -> Arrays.asList(new Table.Row() {}))
        .build();
  }
  
  @Override
  public FileStatement findAll() {
    return ImmutableFileStatement.builder()
        .value("Select all from COMMIT table")
        .command((root) -> root.getRepoTable(ctx).getCommits().getRows())
        .build();
  }
  @Override
  public FileTuple getById(String id) {
    return ImmutableFileTuple.builder()
        .value("Select by id from COMMIT table")
        .command((root) -> {
          return root.getRepoTable(ctx).getCommits().getRows()
              .stream().filter(r -> r.getId().equals(id)).collect(Collectors.toList());
        })
        .props(Tuple.of(id))
        .build();
  }
  @Override
  public FileTuple insertOne(Commit commit) {
    
    var message = commit.getMessage();
    if(commit.getMessage().length() > 100) {
      message = message.substring(0, 100);
    }
    
    return ImmutableFileTuple.builder()
        .value("Insert new row into COMMIT table")
        .command((root) -> {
          final var exists = root.getRepoTable(ctx).getCommits().getRows()
              .stream().filter(r -> r.getId().equals(commit.getId()))
              .findFirst();
            if(!exists.isEmpty()) {
              return Arrays.asList(exists.get());
            }
            
            final var newRow = ImmutableCommitTableRow.builder()
                .id(commit.getId())
                .datetime(commit.getDateTime().toString())
                .author(commit.getAuthor())
                .message(commit.getMessage())
                .tree(commit.getTree())
                .parent(commit.getParent().orElse(null))
                .merge(commit.getMerge().orElse(null))
                .build();
            root.getRepoTable(ctx).getCommits().insert(newRow);
            return Arrays.asList(newRow);
          
        })
        .props(Tuple.from(Arrays.asList(
            commit.getId(), commit.getDateTime().toString(), commit.getAuthor(), message, 
            commit.getTree(), commit.getParent().orElse(null), commit.getMerge().orElse(null))))
        .build();
  }
  
  
}
