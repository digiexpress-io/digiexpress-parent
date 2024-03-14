package io.resys.thena.docdb.models.org.anytree;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgActorStatus;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgGroup;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgGroupRole;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgRole;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgUser;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgUserMembership;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgUserRole;

public interface AnyTreeContainer<T> {
  T accept(AnyTreeContainerVisitor<T> visitor);
  
  
  interface AnyTreeContainerVisitor<T> {
    void start(AnyTreeContainerContext ctx);
    T close();
  }
  
  interface AnyTreeContainerContext {
    OrgUser getUser(String id);
    OrgRole getRole(String id);
    Collection<OrgRole> getRoles();
    List<OrgUserRole> getUserRoles(String id);
    
    // Group related 
    OrgGroup getGroup(String groupId);
    List<OrgGroup> getGroupChildren(String groupId);
    List<OrgUserMembership> getGroupMemberships(String groupId);
    List<OrgGroupRole> getGroupRoles(String groupId);
    List<OrgGroup> getGroupTops();
    List<OrgGroup> getGroupBottoms();
    
    List<OrgUserMembership> getGroupInheritedUsers(String groupId);

    // Status for all entities
    Optional<OrgActorStatus> getStatus(OrgGroup group);
    Optional<OrgActorStatus> getStatus(OrgUserMembership membership);
    Optional<OrgActorStatus> getStatus(OrgUserRole role);
    Optional<OrgActorStatus> getStatus(OrgGroupRole role);
    Optional<OrgActorStatus> getStatus(OrgUser user);
    Optional<OrgActorStatus> getStatus(OrgRole role);
    boolean isStatusDisabled(Optional<OrgActorStatus> status);
    boolean isGroupDisabledUpward(OrgGroup group);
  }
}
