package io.resys.thena.api.entities.org;

public interface ThenaOrgObject {
  interface IsOrgObject { String getId(); OrgDocType getDocType(); }
  interface IsOrgVersionObject { String getCommitId(); }  

  enum OrgDocType {
    OrgActorData, 
    OrgActorStatus,
    OrgUserRole,
    OrgGroupRole,
    OrgUserMembership,
    OrgUser,
    OrgRole,
    OrgGroup,
    OrgCommit,
    OrgCommitTree
  }
}
