package io.resys.thena.docdb.models.org;

import java.util.List;

import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgCommit;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgGroup;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgRole;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgUser;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

public interface OrgQueries {
  
  GroupQuery groups();
  RoleQuery roles();
  UserQuery users();
  CommitQuery commits();
  
  
  interface CommitQuery {
    Multi<OrgCommit> findAll();
    Uni<OrgCommit> getById(String id);
  }
  
  
  interface UserQuery {
    Multi<OrgUser> findAll();
    Multi<OrgUser> findAll(List<String> id);
    Uni<OrgUser> getById(String id); //user.id or user.email or user.external_id 
  }
  
  interface RoleQuery {    
    Multi<OrgRole> findAll();
    Multi<OrgRole> findAll(List<String> id);
    Uni<OrgRole> getById(String id);
  }
  
  interface GroupQuery {
    Multi<OrgGroup> findAll();
    Multi<OrgGroup> findAll(List<String> id);
    Uni<OrgGroup> getById(String id);
  }
}
