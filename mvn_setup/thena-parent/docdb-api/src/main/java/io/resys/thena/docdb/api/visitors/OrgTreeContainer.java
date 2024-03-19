package io.resys.thena.docdb.api.visitors;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgActorStatus;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgGroup;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgPartyRight;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgRole;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgMember;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgMembership;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgMemberRight;

public interface OrgTreeContainer {
  <T> T accept(OrgAnyTreeContainerVisitor<T> visitor);
  
  
  interface OrgAnyTreeContainerVisitor<T> {
    void start(OrgAnyTreeContainerContext ctx);
    T close();
  }
  
  interface OrgAnyTreeContainerContext {
    OrgMember getUser(String id);
    OrgRole getRole(String id);
    Collection<OrgRole> getRoles();
    List<OrgMemberRight> getUserRoles(String id);
    
    // Group related 
    OrgGroup getGroup(String groupId);
    List<OrgGroup> getGroupChildren(String groupId);
    List<OrgMembership> getGroupMemberships(String groupId);
    List<OrgPartyRight> getGroupRoles(String groupId);
    List<OrgGroup> getGroupTops();
    List<OrgGroup> getGroupBottoms();
    
    List<OrgMembership> getGroupInheritedUsers(String groupId);

    // Status for all entities
    Optional<OrgActorStatus> getStatus(OrgGroup group);
    Optional<OrgActorStatus> getStatus(OrgMembership membership);
    Optional<OrgActorStatus> getStatus(OrgMemberRight role);
    Optional<OrgActorStatus> getStatus(OrgPartyRight role);
    Optional<OrgActorStatus> getStatus(OrgMember user);
    Optional<OrgActorStatus> getStatus(OrgRole role);
    boolean isStatusDisabled(Optional<OrgActorStatus> status);
    boolean isGroupDisabledUpward(OrgGroup group);
  }
}
