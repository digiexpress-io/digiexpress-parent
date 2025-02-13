package io.resys.thena.api.entities.org;

public interface ThenaOrgObject {
  interface IsOrgObject { String getId(); OrgDocType getDocType(); }
  interface IsOrgVersionObject { String getCommitId(); }  

  enum OrgDocSubType {
    SYSTEM, NORMAL // system type needs confirmation on API to updated/delete
  }
  
  enum OrgDocType {
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
