package io.resys.thena.docdb.store.sql;

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

import io.resys.thena.docdb.api.models.Repo;
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
import io.resys.thena.docdb.api.models.ThenaGitObject.Tag;
import io.resys.thena.docdb.api.models.ThenaGitObject.Tree;
import io.resys.thena.docdb.api.models.ThenaGitObject.TreeValue;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgActorStatus;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgGroup;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgUserHierarchyEntry;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgPartyRight;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgRole;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgRoleFlattened;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgMember;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgUserFlattened;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgMembership;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgMemberRight;
import io.resys.thena.docdb.spi.DataMapper;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.sqlclient.Row;

public interface SqlMapper extends DataMapper<Row>{
  Repo repo(Row row);

  Doc doc(Row row);
  DocFlatted docFlatted(Row row);
  DocLog docLog(Row row);
  DocBranch docBranch(Row row);
  DocCommit docCommit(Row row);
  DocBranchLock docBranchLock(Row row);
  
  Commit commit(Row row);
  Tree tree(Row row);
  TreeValue treeItem(Row row);
  Tag tag(Row row);
  Branch ref(Row row);
  Blob blob(Row row);
  BlobHistory blobHistory(Row row);

  OrgRole orgRole(Row row);
  OrgMember orgUser(Row row);
  OrgGroup orgGroup(Row row);
  OrgMemberRight orgUserRole(Row row);
  OrgPartyRight orgGroupRole(Row row);
  OrgMembership orgUserMemberships(Row row);
  OrgUserHierarchyEntry orgUserHierarchyEntry(Row row);
  OrgRoleFlattened orgOrgRoleFlattened(Row row);
  OrgUserFlattened orgUserFlattened(Row row);
  OrgActorStatus orgActorStatus(Row row); 
  
  JsonObject jsonObject(Row row, String columnName);
}
