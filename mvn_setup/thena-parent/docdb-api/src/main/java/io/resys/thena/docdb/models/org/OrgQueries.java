package io.resys.thena.docdb.models.org;

import java.util.List;

import javax.annotation.Nullable;

import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgGroup;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgRole;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgUser;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

public interface OrgQueries {
  
  GroupQuery groups();
  RoleQuery roles();
  UserQuery users();
  
  interface UserQuery {
    Multi<OrgUser> findAllByUserIdOrExternalIdOrEmail(
     @Nullable String userId, 
     @Nullable String externalId, 
     @Nullable String email
    );
    Multi<OrgUser> findAll();
    Uni<OrgUser> getById(String id);
  }
  
  interface RoleQuery {
    Multi<OrgRole> findAllByRoleNameOrExternalId(
     @Nullable String roleName, 
     @Nullable String externalId
    );
    
    Multi<OrgRole> findAll();
    Multi<OrgRole> findAll(List<String> id);
    Uni<OrgRole> getById(String id);
  }
  
  interface GroupQuery {
    Multi<OrgGroup> findAllByGroupNameOrExternalId(
      @Nullable String groupName, 
      @Nullable String externalId
    );
    Multi<OrgGroup> findAll();
    Multi<OrgGroup> findAll(List<String> id);
    Uni<OrgGroup> getById(String id);
  }
}
