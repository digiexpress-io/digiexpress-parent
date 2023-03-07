package io.resys.thena.docdb.file;

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

import io.resys.thena.docdb.api.models.ImmutableBlob;
import io.resys.thena.docdb.api.models.ImmutableCommit;
import io.resys.thena.docdb.api.models.ImmutableRef;
import io.resys.thena.docdb.api.models.ImmutableRepo;
import io.resys.thena.docdb.api.models.ImmutableTag;
import io.resys.thena.docdb.api.models.ImmutableTree;
import io.resys.thena.docdb.api.models.ImmutableTreeValue;
import io.resys.thena.docdb.api.models.Objects.Blob;
import io.resys.thena.docdb.api.models.Objects.BlobHistory;
import io.resys.thena.docdb.api.models.Objects.Commit;
import io.resys.thena.docdb.api.models.Objects.Ref;
import io.resys.thena.docdb.api.models.Objects.Tag;
import io.resys.thena.docdb.api.models.Objects.Tree;
import io.resys.thena.docdb.api.models.Objects.TreeValue;
import io.resys.thena.docdb.api.models.Repo;
import io.resys.thena.docdb.file.tables.BlobTable.BlobTableRow;
import io.resys.thena.docdb.file.tables.CommitTable.CommitTableRow;
import io.resys.thena.docdb.file.tables.RefTable.RefTableRow;
import io.resys.thena.docdb.file.tables.RepoTable.RepoTableRow;
import io.resys.thena.docdb.file.tables.Table.FileMapper;
import io.resys.thena.docdb.file.tables.Table.Row;
import io.resys.thena.docdb.file.tables.TagTable.TagTableRow;
import io.resys.thena.docdb.file.tables.TreeItemTable.TreeItemTableRow;
import io.resys.thena.docdb.file.tables.TreeTable.TreeTableRow;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DefaultFileMapper implements FileMapper {
   
  
  @Override
  public Repo repo(Row src) {
    RepoTableRow row = src.toType(RepoTableRow.class);
    
    return ImmutableRepo.builder()
        .id(row.getId())
        .rev(row.getRev())
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
  public Ref ref(Row src) {
    RefTableRow row = src.toType(RefTableRow.class);
    
    return ImmutableRef.builder()
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
  public BlobHistory blobHistory(Row row) {
    throw new IllegalArgumentException("Not required for filebase impl.");
  }
}
