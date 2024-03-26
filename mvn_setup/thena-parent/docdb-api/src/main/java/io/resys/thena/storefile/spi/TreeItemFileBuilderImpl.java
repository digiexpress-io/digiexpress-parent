package io.resys.thena.storefile.spi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

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

import java.util.stream.Collectors;

import io.resys.thena.api.models.ThenaGitObject.Tree;
import io.resys.thena.api.models.ThenaGitObject.TreeValue;
import io.resys.thena.spi.DbCollections;
import io.resys.thena.storefile.FileBuilder.TreeItemFileBuilder;
import io.resys.thena.storefile.tables.ImmutableFileStatement;
import io.resys.thena.storefile.tables.ImmutableFileTuple;
import io.resys.thena.storefile.tables.ImmutableFileTupleList;
import io.resys.thena.storefile.tables.ImmutableTreeItemTableRow;
import io.resys.thena.storefile.tables.Table;
import io.resys.thena.storefile.tables.Table.FileStatement;
import io.resys.thena.storefile.tables.Table.FileTuple;
import io.resys.thena.storefile.tables.Table.FileTupleList;
import io.resys.thena.storefile.tables.TreeItemTable.TreeItemTableRow;
import io.vertx.mutiny.sqlclient.Tuple;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TreeItemFileBuilderImpl implements TreeItemFileBuilder {

  private final DbCollections ctx;
  
  @Override
  public FileStatement create() {
    return ImmutableFileStatement.builder()
        .value("create TREE_ITEM table if it does not exist")
        .command((root) -> {
          root.getRepoTable(ctx).getTreeItems().create();
          return Arrays.asList(new Table.Row() {});
        })
        .build();
  }
  @Override
  public FileStatement constraints() {
    return ImmutableFileStatement.builder()
        .value("Apply constraints on table TREE_ITEM")
        .command(conn -> Arrays.asList(new Table.Row() {}))
        .build();
  }
  
  @Override
  public FileStatement findAll() {
    return ImmutableFileStatement.builder()
        .value("Select all from TREE_ITEM table")
        .command((root) -> root.getRepoTable(ctx).getTreeItems().getRows())
        .build();
  }
  @Override
  public FileTuple getByTreeId(String treeId) {
    return ImmutableFileTuple.builder()
        .value("Select by tree from TREE_ITEM table")
        .command((root) -> root.getRepoTable(ctx).getTreeItems().getRows()
            .stream().filter(r -> r.getTree().equals(treeId)).collect(Collectors.toList()))
        .props(Tuple.of(treeId))
        .build();
  }
  @Override
  public FileTuple insertOne(Tree tree, TreeValue item) {
    return ImmutableFileTuple.builder()
        .value("Insert new row into TREE_ITEM table")
        .command((root) -> {
          
          final var exists = root.getRepoTable(ctx).getTreeItems().getRows()
            .stream().filter(r -> 
              r.getName().equals(item.getName()) &&
              r.getBlob().equals(item.getBlob()) &&
              r.getTree().equals(tree.getId()) 
                )
            .findFirst();
          if(!exists.isEmpty()) {
            return Arrays.asList(exists.get());
          }
          
          final var newRow = ImmutableTreeItemTableRow.builder()
              .id(UUID.randomUUID().toString())
              .name(item.getName())
              .blob(item.getBlob())
              .tree(tree.getId())
              .build();
          root.getRepoTable(ctx).getTreeItems().insert(newRow);
          return Arrays.asList(newRow);
        })
        
        .props(Tuple.of(item.getName(), item.getBlob(), tree.getId()))
        .build();
  }
  @Override
  public FileTupleList insertAll(Tree tree) {

      return ImmutableFileTupleList.builder()
          .value("Insert new rows into TREE_ITEM table")
          .props(tree.getValues().values().stream()
              .map(v -> Tuple.of(v.getName(), v.getBlob(), tree.getId()))
              .collect(Collectors.toList()))
          .command((root) -> {
            final var byId = root.getRepoTable(ctx).getTreeItems()
                .getRows().stream()
                .collect(Collectors.toMap(
                    e -> (e.getName() + e.getBlob() + e.getTree()), 
                    e -> e));
            
            final var inserts = new ArrayList<TreeItemTableRow>();
            final var results = new ArrayList<TreeItemTableRow>();
            
            tree.getValues().values().stream().forEach(treeItem -> {
              final var key = (treeItem.getName() + treeItem.getBlob() + tree.getId());
              
              if(byId.containsKey(key)) {
                results.add(byId.get(key));
              } else {
                final var newRow = ImmutableTreeItemTableRow.builder()
                    .id(UUID.randomUUID().toString())
                    .name(treeItem.getName())
                    .blob(treeItem.getBlob())
                    .tree(tree.getId())
                    .build();
                inserts.add(newRow);
                results.add(newRow);
              }  
            });
            root.getRepoTable(ctx).getTreeItems().insertAll(inserts);
            return results;
          })
          .build();
  }
}
