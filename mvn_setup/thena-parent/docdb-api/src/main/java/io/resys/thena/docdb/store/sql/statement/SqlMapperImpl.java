package io.resys.thena.docdb.store.sql.statement;

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

import java.time.LocalDateTime;
import java.util.Optional;

import io.resys.thena.docdb.api.models.ImmutableBlob;
import io.resys.thena.docdb.api.models.ImmutableBlobHistory;
import io.resys.thena.docdb.api.models.ImmutableBranch;
import io.resys.thena.docdb.api.models.ImmutableCommit;
import io.resys.thena.docdb.api.models.ImmutableCommitTree;
import io.resys.thena.docdb.api.models.ImmutableDoc;
import io.resys.thena.docdb.api.models.ImmutableDocBranch;
import io.resys.thena.docdb.api.models.ImmutableDocBranchLock;
import io.resys.thena.docdb.api.models.ImmutableDocCommit;
import io.resys.thena.docdb.api.models.ImmutableDocFlatted;
import io.resys.thena.docdb.api.models.ImmutableDocLog;
import io.resys.thena.docdb.api.models.ImmutableRepo;
import io.resys.thena.docdb.api.models.ImmutableTag;
import io.resys.thena.docdb.api.models.ImmutableTree;
import io.resys.thena.docdb.api.models.ImmutableTreeValue;
import io.resys.thena.docdb.api.models.Repo;
import io.resys.thena.docdb.api.models.Repo.RepoType;
import io.resys.thena.docdb.api.models.ThenaDocObject;
import io.resys.thena.docdb.api.models.ThenaDocObject.Doc;
import io.resys.thena.docdb.api.models.ThenaDocObject.DocBranch;
import io.resys.thena.docdb.api.models.ThenaDocObject.DocBranchLock;
import io.resys.thena.docdb.api.models.ThenaDocObject.DocCommit;
import io.resys.thena.docdb.api.models.ThenaDocObject.DocFlatted;
import io.resys.thena.docdb.api.models.ThenaDocObject.DocLog;
import io.resys.thena.docdb.api.models.ThenaGitObject.Blob;
import io.resys.thena.docdb.api.models.ThenaGitObject.BlobHistory;
import io.resys.thena.docdb.api.models.ThenaGitObject.Branch;
import io.resys.thena.docdb.api.models.ThenaGitObject.Commit;
import io.resys.thena.docdb.api.models.ThenaGitObject.CommitLockStatus;
import io.resys.thena.docdb.api.models.ThenaGitObject.CommitTree;
import io.resys.thena.docdb.api.models.ThenaGitObject.Tag;
import io.resys.thena.docdb.api.models.ThenaGitObject.Tree;
import io.resys.thena.docdb.api.models.ThenaGitObject.TreeValue;
import io.resys.thena.docdb.spi.DbCollections;
import io.resys.thena.docdb.store.sql.SqlMapper;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.sqlclient.Row;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SqlMapperImpl implements SqlMapper {

  protected final DbCollections ctx;
 
  @Override
  public Repo repo(Row row) {
    return ImmutableRepo.builder()
        .id(row.getString("id"))
        .rev(row.getString("rev"))
        .name(row.getString("name"))
        .type(RepoType.valueOf(row.getString("type")))
        .prefix(row.getString("prefix"))
        .build();
  }
  @Override
  public Commit commit(Row row) {
    return ImmutableCommit.builder()
        .id(row.getString("id"))
        .author(row.getString("author"))
        .dateTime(LocalDateTime.parse(row.getString("datetime")))
        .message(row.getString("message"))
        .parent(Optional.ofNullable(row.getString("parent")))
        .merge(Optional.ofNullable(row.getString("merge")))
        .tree(row.getString("tree"))
        .build();
  }
  @Override
  public Tree tree(Row row) {
    return ImmutableTree.builder().id(row.getString("id")).build();
  }
  @Override
  public TreeValue treeItem(Row row) {
    return ImmutableTreeValue.builder()
        .name(row.getString("name"))
        .blob(row.getString("blob"))
        .build();
  }
  @Override
  public Tag tag(Row row) {
    return ImmutableTag.builder()
        .author(row.getString("author"))
        .dateTime(LocalDateTime.parse(row.getString("datetime")))
        .message(row.getString("message"))
        .commit(row.getString("commit"))
        .name(row.getString("id"))
        .build();
  }
  @Override
  public Branch ref(Row row) {
    return ImmutableBranch.builder()
        .name(row.getString("name"))
        .commit(row.getString("commit"))
        .build();
  }
  @Override
  public Blob blob(Row row) {
    return ImmutableBlob.builder()
        .id(row.getString("id"))
        .value(jsonObject(row, "value"))
        .build();
  }
  @Override
  public BlobHistory blobHistory(Row row) { 
    return ImmutableBlobHistory.builder()
        .treeId(row.getString("tree"))
        .treeValueName(row.getString("blob_name"))
        .commit(row.getString("commit_id"))
        .blob(ImmutableBlob.builder()
            .id(row.getString("blob_id"))
            .value(jsonObject(row, "blob_value"))
            .build())
        .build();
  }
  @Override
  public CommitTree commitTree(Row row) {
    return commitTreeInternal(row)
        .blob(Optional.empty())
        .build();
  }
  @Override
  public CommitTree commitTreeWithBlobs(Row row) {
    return commitTreeInternal(row)
        .blob(ImmutableBlob.builder()
            .id(row.getString("blob_id"))
            .value(jsonObject(row, "blob_value"))
            .build())
        .build();
  }
  
  
  public ImmutableCommitTree.Builder commitTreeInternal(Row row) {
    final var blob = row.getString("blob_id");
    final var blobName = row.getString("blob_name");
    return ImmutableCommitTree.builder()
        .treeId(row.getString("tree_id"))
        .commitId(row.getString("commit_id"))
        .commitParent(row.getString("commit_parent"))
        .commitAuthor(row.getString("author"))
        .commitDateTime(LocalDateTime.parse(row.getString("datetime")))
        .commitMessage(row.getString("message"))
        .commitMerge(row.getString("merge"))
        .branchName(row.getString("ref_name"))
        .treeValue(blob == null ? Optional.empty() : Optional.of(ImmutableTreeValue.builder()
            .blob(blob)
            .name(blobName)
            .build()));
  }
  
  
  @Override
  public JsonObject jsonObject(Row row, String columnName) {
    // string based - new JsonObject(row.getString(columnName));
    return row.getJsonObject(columnName);
  }
  @Override
  public DocCommit docCommit(Row row) {
    return ImmutableDocCommit.builder()
        .id(row.getString("id"))
        .author(row.getString("author"))
        .dateTime(LocalDateTime.parse(row.getString("datetime")))
        .message(row.getString("message"))
        .parent(Optional.ofNullable(row.getString("parent")))
        .branchId(row.getString("branch_id"))
        .docId(row.getString("doc_id"))
        .build();
  }
  
  @Override
  public DocBranchLock docBranchLock(Row row) {
    return ImmutableDocBranchLock.builder()
        .status(CommitLockStatus.LOCK_TAKEN)
        .doc(ImmutableDoc.builder()
            .id(row.getString("doc_id"))
            .externalId(row.getString("external_id"))
            .externalIdDeleted(row.getString("external_id_deleted"))
            .parentId(row.getString("doc_parent_id"))
            .type(row.getString("doc_type"))
            .status(ThenaDocObject.DocStatus.valueOf(row.getString("doc_status")))
            .meta(jsonObject(row, "doc_meta"))
            .build())
        .branch(ImmutableDocBranch.builder()
            .id(row.getString("branch_id"))
            .docId(row.getString("doc_id"))
            .status(ThenaDocObject.DocStatus.valueOf(row.getString("branch_status")))
            .commitId(row.getString("commit_id"))
            .branchName(row.getString("branch_name"))
            .branchNameDeleted(row.getString("branch_name_deleted"))
            .value(jsonObject(row, "branch_value"))
            .status(ThenaDocObject.DocStatus.valueOf(row.getString("branch_status")))
            .build())
        .commit(ImmutableDocCommit.builder()
            .id(row.getString("commit_id"))
            .author(row.getString("author"))
            .dateTime(LocalDateTime.parse(row.getString("datetime")))
            .message(row.getString("message"))
            .parent(Optional.ofNullable(row.getString("commit_parent")))
            .branchId(row.getString("branch_id"))
            .docId(row.getString("doc_id"))
            .build())
        .build();
  }
  @Override
  public Doc doc(Row row) {
    return ImmutableDoc.builder()
        .id(row.getString("id"))
        .externalId(row.getString("external_id"))
        .parentId(row.getString("doc_parent_id"))
        .externalIdDeleted(row.getString("external_id_deleted"))
        .type(row.getString("doc_type"))
        .status(ThenaDocObject.DocStatus.valueOf(row.getString("doc_status")))
        .meta(jsonObject(row, "doc_meta"))
        .build();
  }
  @Override
  public DocLog docLog(Row row) {
    return ImmutableDocLog.builder()
        .id(row.getString("id"))
        .docId(row.getString("doc_id"))
        .branchId(row.getString("branch_id"))
        .docCommitId(row.getString("commit_id"))
        .value(jsonObject(row, "value"))
        .build();
  }
  @Override
  public DocBranch docBranch(Row row) {
    return ImmutableDocBranch.builder()
        .id(row.getString("branch_id"))
        .docId(row.getString("doc_id"))
        .commitId(row.getString("commit_id"))
        .branchName(row.getString("branch_name"))
        .branchNameDeleted(row.getString("branch_name_deleted"))
        .value(jsonObject(row, "value"))
        .status(ThenaDocObject.DocStatus.valueOf(row.getString("branch_status")))
        .build();
    
  }
  @Override
  public DocFlatted docFlatted(Row row) {
    return ImmutableDocFlatted.builder()
        .externalId(row.getString("external_id"))
        .docId(row.getString("doc_id"))
        .docType(row.getString("doc_type"))
        .docStatus(ThenaDocObject.DocStatus.valueOf(row.getString("doc_status")))
        .docMeta(Optional.ofNullable(jsonObject(row, "doc_meta")))
        .docParentId(Optional.ofNullable(row.getString("doc_parent_id")))
        .externalIdDeleted(Optional.ofNullable(row.getString("external_id_deleted")))
        
        .branchId(row.getString("branch_id"))
        .branchName(row.getString("branch_name"))
        .branchNameDeleted(Optional.ofNullable(row.getString("branch_name_deleted")))
        .branchValue(jsonObject(row, "branch_value"))
        .branchStatus(ThenaDocObject.DocStatus.valueOf(row.getString("branch_status")))
        
        .commitId(row.getString("commit_id"))
        .commitAuthor(row.getString("commit_author"))
        .commitMessage(row.getString("commit_message"))
        .commitParent(Optional.ofNullable(row.getString("commit_parent")))
        .commitDateTime(LocalDateTime.parse(row.getString("commit_datetime")))
        
        .docLogId(Optional.ofNullable(row.getString("doc_log_id")))
        .docLogValue(Optional.ofNullable(jsonObject(row, "doc_log_value")))
        
        .build();
  }
}
