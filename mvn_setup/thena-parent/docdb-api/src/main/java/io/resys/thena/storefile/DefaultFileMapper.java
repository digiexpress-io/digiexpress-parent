package io.resys.thena.storefile;

/*-
 * #%L
 * thena-docdb-api
 * %%
 * Copyright (C) 2021 - 2022 Copyright 2021 ReSys OÜ
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

import java.time.LocalDateTime;
import java.util.Optional;

import io.resys.thena.api.entities.ImmutableTenant;
import io.resys.thena.api.entities.Tenant;
import io.resys.thena.api.entities.git.Blob;
import io.resys.thena.api.entities.git.BlobHistory;
import io.resys.thena.api.entities.git.Branch;
import io.resys.thena.api.entities.git.Commit;
import io.resys.thena.api.entities.git.CommitTree;
import io.resys.thena.api.entities.git.ImmutableBlob;
import io.resys.thena.api.entities.git.ImmutableBranch;
import io.resys.thena.api.entities.git.ImmutableCommit;
import io.resys.thena.api.entities.git.ImmutableTag;
import io.resys.thena.api.entities.git.ImmutableTree;
import io.resys.thena.api.entities.git.ImmutableTreeValue;
import io.resys.thena.api.entities.git.Tag;
import io.resys.thena.api.entities.git.Tree;
import io.resys.thena.api.entities.git.TreeValue;
import io.resys.thena.storefile.tables.BlobTable.BlobTableRow;
import io.resys.thena.storefile.tables.CommitTable.CommitTableRow;
import io.resys.thena.storefile.tables.RefTable.RefTableRow;
import io.resys.thena.storefile.tables.RepoTable.RepoTableRow;
import io.resys.thena.storefile.tables.Table.FileMapper;
import io.resys.thena.storefile.tables.Table.Row;
import io.resys.thena.storefile.tables.TagTable.TagTableRow;
import io.resys.thena.storefile.tables.TreeItemTable.TreeItemTableRow;
import io.resys.thena.storefile.tables.TreeTable.TreeTableRow;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DefaultFileMapper implements FileMapper {
  @Override
  public Tenant repo(Row src) {
    RepoTableRow row = src.toType(RepoTableRow.class);
    
    return ImmutableTenant.builder()
        .id(row.getId())
        .rev(row.getRev())
        .type(row.getType())
        .name(row.getName())
        .prefix(row.getPrefix())
        .build();
  }
  @Override
  public Commit commit(Row src) {
    CommitTableRow row = src.toType(CommitTableRow.class);
    
    return ImmutableCommit.builder()
        .id(row.getId())
        .author(row.getAuthor())
        .dateTime(LocalDateTime.parse(row.getDatetime()))
        .message(row.getMessage())
        .parent(Optional.ofNullable(row.getParent()))
        .merge(Optional.ofNullable(row.getMerge()))
        .tree(row.getTree())
        .build();
  }
  @Override
  public Tree tree(Row src) {
    TreeTableRow row = src.toType(TreeTableRow.class);
    
    return ImmutableTree.builder().id(row.getId()).build();
  }
  @Override
  public TreeValue treeItem(Row src) {
    TreeItemTableRow row = src.toType(TreeItemTableRow.class);
    
    return ImmutableTreeValue.builder()
        .name(row.getName())
        .blob(row.getBlob())
        .build();
  }
  @Override
  public Tag tag(Row src) {
    TagTableRow row = src.toType(TagTableRow.class);
    
    return ImmutableTag.builder()
        .author(row.getAuthor())
        .dateTime(LocalDateTime.parse(row.getDatetime()))
        .message(row.getMessage())
        .commit(row.getCommit())
        .name(row.getId())
        .build();
  }
  @Override
  public Branch ref(Row src) {
    RefTableRow row = src.toType(RefTableRow.class);
    
    return ImmutableBranch.builder()
        .name(row.getName())
        .commit(row.getCommit())
        .build();
  }
  @Override
  public Blob blob(Row src) {
    BlobTableRow row = src.toType(BlobTableRow.class);
    
    return ImmutableBlob.builder()
        .id(row.getId())
        .value(row.getValue())
        .build();
  }
  @Override
  public CommitTree commitTreeWithBlobs(Row row) {
    throw new IllegalArgumentException("Not required for filebase impl.");
  }
  @Override
  public CommitTree commitTree(Row row) {
    throw new IllegalArgumentException("Not required for filebase impl.");
  }
  @Override
  public BlobHistory blobHistory(Row row) {
    throw new IllegalArgumentException("Not required for filebase impl.");
  }
}
