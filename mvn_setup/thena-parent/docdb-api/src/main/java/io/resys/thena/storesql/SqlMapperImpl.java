package io.resys.thena.storesql;

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

import io.resys.thena.api.entities.CommitLockStatus;
import io.resys.thena.api.entities.ImmutableTenant;
import io.resys.thena.api.entities.Tenant;
import io.resys.thena.api.entities.Tenant.StructureType;
import io.resys.thena.api.entities.doc.Doc;
import io.resys.thena.api.entities.doc.DocBranch;
import io.resys.thena.api.entities.doc.DocBranchLock;
import io.resys.thena.api.entities.doc.DocCommit;
import io.resys.thena.api.entities.doc.DocFlatted;
import io.resys.thena.api.entities.doc.DocLog;
import io.resys.thena.api.entities.doc.ImmutableDoc;
import io.resys.thena.api.entities.doc.ImmutableDocBranch;
import io.resys.thena.api.entities.doc.ImmutableDocBranchLock;
import io.resys.thena.api.entities.doc.ImmutableDocCommit;
import io.resys.thena.api.entities.doc.ImmutableDocFlatted;
import io.resys.thena.api.entities.doc.ImmutableDocLog;
import io.resys.thena.api.entities.git.Blob;
import io.resys.thena.api.entities.git.BlobHistory;
import io.resys.thena.api.entities.git.Branch;
import io.resys.thena.api.entities.git.Commit;
import io.resys.thena.api.entities.git.CommitTree;
import io.resys.thena.api.entities.git.ImmutableBlob;
import io.resys.thena.api.entities.git.ImmutableBlobHistory;
import io.resys.thena.api.entities.git.ImmutableBranch;
import io.resys.thena.api.entities.git.ImmutableCommit;
import io.resys.thena.api.entities.git.ImmutableCommitTree;
import io.resys.thena.api.entities.git.ImmutableTag;
import io.resys.thena.api.entities.git.ImmutableTree;
import io.resys.thena.api.entities.git.ImmutableTreeValue;
import io.resys.thena.api.entities.git.Tag;
import io.resys.thena.api.entities.git.Tree;
import io.resys.thena.api.entities.git.TreeValue;
import io.resys.thena.api.entities.org.ImmutableOrgActorStatus;
import io.resys.thena.api.entities.org.ImmutableOrgMember;
import io.resys.thena.api.entities.org.ImmutableOrgMemberFlattened;
import io.resys.thena.api.entities.org.ImmutableOrgMemberHierarchyEntry;
import io.resys.thena.api.entities.org.ImmutableOrgMemberRight;
import io.resys.thena.api.entities.org.ImmutableOrgMembership;
import io.resys.thena.api.entities.org.ImmutableOrgParty;
import io.resys.thena.api.entities.org.ImmutableOrgPartyRight;
import io.resys.thena.api.entities.org.ImmutableOrgRight;
import io.resys.thena.api.entities.org.ImmutableOrgRightFlattened;
import io.resys.thena.api.entities.org.OrgActorStatus;
import io.resys.thena.api.entities.org.OrgMember;
import io.resys.thena.api.entities.org.OrgMemberFlattened;
import io.resys.thena.api.entities.org.OrgMemberHierarchyEntry;
import io.resys.thena.api.entities.org.OrgMemberRight;
import io.resys.thena.api.entities.org.OrgMembership;
import io.resys.thena.api.entities.org.OrgParty;
import io.resys.thena.api.entities.org.OrgPartyRight;
import io.resys.thena.api.entities.org.OrgRight;
import io.resys.thena.api.entities.org.OrgRightFlattened;
import io.resys.thena.datasource.TenantTableNames;
import io.resys.thena.datasource.SqlDataMapper;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.sqlclient.Row;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SqlMapperImpl implements SqlDataMapper {

  protected final TenantTableNames ctx;
 
  @Override
  public Tenant repo(Row row) {
    return ImmutableTenant.builder()
        .id(row.getString("id"))
        .rev(row.getString("rev"))
        .name(row.getString("name"))
        .externalId(row.getString("external_id"))
        .type(StructureType.valueOf(row.getString("type")))
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
            .status(Doc.DocStatus.valueOf(row.getString("doc_status")))
            .meta(jsonObject(row, "doc_meta"))
            .build())
        .branch(ImmutableDocBranch.builder()
            .id(row.getString("branch_id"))
            .docId(row.getString("doc_id"))
            .status(Doc.DocStatus.valueOf(row.getString("branch_status")))
            .commitId(row.getString("commit_id"))
            .branchName(row.getString("branch_name"))
            .branchNameDeleted(row.getString("branch_name_deleted"))
            .value(jsonObject(row, "branch_value"))
            .status(Doc.DocStatus.valueOf(row.getString("branch_status")))
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
        .status(Doc.DocStatus.valueOf(row.getString("doc_status")))
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
        .status(Doc.DocStatus.valueOf(row.getString("branch_status")))
        .build();
    
  }
  @Override
  public DocFlatted docFlatted(Row row) {
    return ImmutableDocFlatted.builder()
        .externalId(row.getString("external_id"))
        .docId(row.getString("doc_id"))
        .docType(row.getString("doc_type"))
        .docStatus(Doc.DocStatus.valueOf(row.getString("doc_status")))
        .docMeta(Optional.ofNullable(jsonObject(row, "doc_meta")))
        .docParentId(Optional.ofNullable(row.getString("doc_parent_id")))
        .externalIdDeleted(Optional.ofNullable(row.getString("external_id_deleted")))
        
        .branchId(row.getString("branch_id"))
        .branchName(row.getString("branch_name"))
        .branchNameDeleted(Optional.ofNullable(row.getString("branch_name_deleted")))
        .branchValue(jsonObject(row, "branch_value"))
        .branchStatus(Doc.DocStatus.valueOf(row.getString("branch_status")))
        
        .commitId(row.getString("commit_id"))
        .commitAuthor(row.getString("commit_author"))
        .commitMessage(row.getString("commit_message"))
        .commitParent(Optional.ofNullable(row.getString("commit_parent")))
        .commitDateTime(LocalDateTime.parse(row.getString("commit_datetime")))
        
        .docLogId(Optional.ofNullable(row.getString("doc_log_id")))
        .docLogValue(Optional.ofNullable(jsonObject(row, "doc_log_value")))
        
        .build();
  }
  @Override
  public OrgMember orgMember(Row row) {
    return ImmutableOrgMember.builder()
        .id(row.getString("id"))
        .externalId(row.getString("external_id"))
        .commitId(row.getString("commit_id"))
        .userName(row.getString("username"))
        .email(row.getString("email"))
        .build();
  }
	@Override
	public OrgParty orgParty(Row row) {
    return ImmutableOrgParty.builder()
        .id(row.getString("id"))
        .externalId(row.getString("external_id"))
        .parentId(row.getString("parent_id"))
        .commitId(row.getString("commit_id"))
        .partyName(row.getString("party_name"))
        .partyDescription(row.getString("party_description"))
        .build();
	}
	@Override
	public OrgRight orgRight(Row row) {
    return ImmutableOrgRight.builder()
        .id(row.getString("id"))
        .externalId(row.getString("external_id"))
        .rightName(row.getString("right_name"))
        .rightDescription(row.getString("right_description"))
        .commitId(row.getString("commit_id"))
        .build();
	}
	@Override
	public OrgMembership orgMembership(Row row) {
    return ImmutableOrgMembership.builder()
        .id(row.getString("id"))
        .commitId(row.getString("commit_id"))
        .memberId(row.getString("member_id"))
        .partyId(row.getString("party_id"))
        .build();
	}
	
	@Override
	public OrgPartyRight orgPartyRright(Row row) {
    return ImmutableOrgPartyRight.builder()
        .id(row.getString("id"))
        .commitId(row.getString("commit_id"))
        .rightId(row.getString("right_id"))
        .partyId(row.getString("party_id"))
        .build();
	}
	
	@Override
	public OrgMemberRight orgMemberRight(Row row) {
    return ImmutableOrgMemberRight.builder()
        .id(row.getString("id"))
        .commitId(row.getString("commit_id"))
        .rightId(row.getString("right_id"))
        .memberId(row.getString("member_id"))
        .partyId(row.getString("party_id"))
        .build();
	}
	@Override
	public OrgMemberHierarchyEntry orgMemberHierarchyEntry(Row row) {
		final var roleStatus = row.getString("right_status");
		final var groupStatus = row.getString("status");
		
		return ImmutableOrgMemberHierarchyEntry.builder()
		  	.partyId(row.getString("id"))
		  	.partyParentId(row.getString("parent_id"))
		  	.partyName(row.getString("party_name"))
        .partyDescription(row.getString("party_description"))
		  	.membershipId(row.getString("membership_id"))
		  	
		  	.partyStatusId(row.getString("status_id"))
		  	.partyStatus(groupStatus != null ? OrgActorStatus.OrgActorStatusType.valueOf(groupStatus) : null)
		  	.partyStatusMemberId(row.getString("status_member_id"))
		  	
		  	.rightId(row.getString("right_id"))
		  	.rightName(row.getString("right_name"))
        .rightDescription(row.getString("right_description"))
        
		  	.rightStatus(roleStatus != null ? OrgActorStatus.OrgActorStatusType.valueOf(roleStatus) : null)
		  	.rightStatusId(row.getString("right_status_id"))
				.build();
	}
	
  @Override
  public OrgRightFlattened orgOrgRightFlattened(Row row) {
    final var roleStatus = row.getString("right_status");
    return ImmutableOrgRightFlattened.builder()
        .rightId(row.getString("right_id"))
        .rightName(row.getString("right_name"))
        .rightDescription(row.getString("right_description"))
        .rightStatus(roleStatus != null ? OrgActorStatus.OrgActorStatusType.valueOf(roleStatus) : null)
        .rightStatusId(row.getString("right_status_id"))
        .build();
  }
  @Override
  public OrgMemberFlattened orgMemberFlattened(Row row) {
    final var userStatus = row.getString("user_status");
    return ImmutableOrgMemberFlattened.builder()
        .id(row.getString("id"))
        .externalId(row.getString("external_id"))
        .commitId(row.getString("commit_id"))
        .userName(row.getString("username"))
        .email(row.getString("email"))
        .status(userStatus != null ? OrgActorStatus.OrgActorStatusType.valueOf(userStatus) : null)
        .statusId(row.getString("user_status_id"))
        .build();
  }
  @Override
  public OrgActorStatus orgActorStatus(Row row) {
    final var actorStatus = row.getString("actor_status");
    return ImmutableOrgActorStatus.builder()
        .id(row.getString("id"))
        .commitId(row.getString("commit_id"))
        .memberId(row.getString("member_id"))
        .rightId(row.getString("right_id"))
        .partyId(row.getString("party_id"))
        .value(actorStatus != null ? OrgActorStatus.OrgActorStatusType.valueOf(actorStatus) : null)
        .build();
  }
  @Override
  public SqlDataMapper withOptions(TenantTableNames options) {
    return new SqlMapperImpl(options);
  }
}
