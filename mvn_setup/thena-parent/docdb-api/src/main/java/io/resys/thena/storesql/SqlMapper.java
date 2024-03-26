package io.resys.thena.storesql;

import io.resys.thena.api.entities.Tenant;
import io.resys.thena.api.entities.doc.Doc;
import io.resys.thena.api.entities.doc.DocBranch;
import io.resys.thena.api.entities.doc.DocBranchLock;
import io.resys.thena.api.entities.doc.DocCommit;
import io.resys.thena.api.entities.doc.DocFlatted;
import io.resys.thena.api.entities.doc.DocLog;
import io.resys.thena.api.entities.git.Blob;
import io.resys.thena.api.entities.git.BlobHistory;
import io.resys.thena.api.entities.git.Branch;
import io.resys.thena.api.entities.git.Commit;
import io.resys.thena.api.entities.git.Tag;
import io.resys.thena.api.entities.git.Tree;
import io.resys.thena.api.entities.git.TreeValue;
import io.resys.thena.api.entities.org.ThenaOrgObject.OrgActorStatus;
import io.resys.thena.api.entities.org.ThenaOrgObject.OrgMember;
import io.resys.thena.api.entities.org.ThenaOrgObject.OrgMemberFlattened;
import io.resys.thena.api.entities.org.ThenaOrgObject.OrgMemberHierarchyEntry;
import io.resys.thena.api.entities.org.ThenaOrgObject.OrgMemberRight;
import io.resys.thena.api.entities.org.ThenaOrgObject.OrgMembership;
import io.resys.thena.api.entities.org.ThenaOrgObject.OrgParty;
import io.resys.thena.api.entities.org.ThenaOrgObject.OrgPartyRight;
import io.resys.thena.api.entities.org.ThenaOrgObject.OrgRight;
import io.resys.thena.api.entities.org.ThenaOrgObject.OrgRightFlattened;
import io.resys.thena.spi.DataMapper;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.sqlclient.Row;

public interface SqlMapper extends DataMapper<Row>{
  Tenant repo(Row row);

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
