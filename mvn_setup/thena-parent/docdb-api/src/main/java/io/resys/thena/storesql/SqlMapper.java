package io.resys.thena.storesql;

import io.resys.thena.api.models.Repo;
import io.resys.thena.api.models.ThenaDocObject.Doc;
import io.resys.thena.api.models.ThenaDocObject.DocBranch;
import io.resys.thena.api.models.ThenaDocObject.DocBranchLock;
import io.resys.thena.api.models.ThenaDocObject.DocCommit;
import io.resys.thena.api.models.ThenaDocObject.DocFlatted;
import io.resys.thena.api.models.ThenaDocObject.DocLog;
import io.resys.thena.api.models.ThenaGitObject.Blob;
import io.resys.thena.api.models.ThenaGitObject.BlobHistory;
import io.resys.thena.api.models.ThenaGitObject.Branch;
import io.resys.thena.api.models.ThenaGitObject.Commit;
import io.resys.thena.api.models.ThenaGitObject.Tag;
import io.resys.thena.api.models.ThenaGitObject.Tree;
import io.resys.thena.api.models.ThenaGitObject.TreeValue;
import io.resys.thena.api.models.ThenaOrgObject.OrgActorStatus;
import io.resys.thena.api.models.ThenaOrgObject.OrgMember;
import io.resys.thena.api.models.ThenaOrgObject.OrgMemberFlattened;
import io.resys.thena.api.models.ThenaOrgObject.OrgMemberHierarchyEntry;
import io.resys.thena.api.models.ThenaOrgObject.OrgMemberRight;
import io.resys.thena.api.models.ThenaOrgObject.OrgMembership;
import io.resys.thena.api.models.ThenaOrgObject.OrgParty;
import io.resys.thena.api.models.ThenaOrgObject.OrgPartyRight;
import io.resys.thena.api.models.ThenaOrgObject.OrgRight;
import io.resys.thena.api.models.ThenaOrgObject.OrgRightFlattened;
import io.resys.thena.spi.DataMapper;
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

  OrgRight orgRight(Row row);
  OrgMember orgMember(Row row);
  OrgParty orgParty(Row row);
  OrgMemberRight orgMemberRight(Row row);
  OrgPartyRight orgPartyRright(Row row);
  OrgMembership orgMembership(Row row);
  OrgMemberHierarchyEntry orgMemberHierarchyEntry(Row row);
  OrgRightFlattened orgOrgRightFlattened(Row row);
  OrgMemberFlattened orgMemberFlattened(Row row);
  OrgActorStatus orgActorStatus(Row row); 
  
  JsonObject jsonObject(Row row, String columnName);
}
