package io.resys.thena.datasource;

import io.resys.thena.api.entities.Tenant;
import io.resys.thena.api.entities.doc.Doc;
import io.resys.thena.api.entities.doc.DocBranch;
import io.resys.thena.api.entities.doc.DocBranchLock;
import io.resys.thena.api.entities.doc.DocCommit;
import io.resys.thena.api.entities.doc.DocFlatted;
import io.resys.thena.api.entities.doc.DocLog;
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
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.sqlclient.Row;

public interface SqlDataMapper extends DataMapper<Row>{
  Tenant repo(Row row);
  /*
  Doc doc(Row row);
  DocFlatted docFlatted(Row row);
  DocLog docLog(Row row);
  DocBranch docBranch(Row row);
  DocCommit docCommit(Row row);
  DocBranchLock docBranchLock(Row row);*/

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
  
  SqlDataMapper withOptions(TenantTableNames options);
}
