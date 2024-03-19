package io.resys.thena.docdb.models.org.anytree;

import java.util.List;

import io.resys.thena.docdb.api.models.ImmutableOrgGroupHierarchy;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgActorStatusType;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgGroup;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgPartyRight;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgRole;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgMember;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgMembership;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgMemberRight;
import io.resys.thena.docdb.api.visitors.OrgGroupContainerVisitor;
import io.resys.thena.docdb.api.visitors.OrgGroupContainerVisitor.GroupVisitor;
import io.resys.thena.docdb.api.visitors.OrgTreeContainer.OrgAnyTreeContainerContext;
import io.resys.thena.docdb.api.visitors.OrgTreeContainer.OrgAnyTreeContainerVisitor;



public class GroupHierarchyContainerVisitor extends OrgGroupContainerVisitor<ImmutableOrgGroupHierarchy> 
  implements OrgAnyTreeContainerVisitor<ImmutableOrgGroupHierarchy>, GroupVisitor {
  
  private final String groupIdOrNameOrExternalId;
  private final ImmutableOrgGroupHierarchy.Builder builder = ImmutableOrgGroupHierarchy.builder();
  
  private String foundGroupId;
  
  
  public GroupHierarchyContainerVisitor(String groupIdOrNameOrExternalId) {
    super(true);
    this.groupIdOrNameOrExternalId = groupIdOrNameOrExternalId;
  }
  
  private boolean isDirectGroup(OrgGroup group) {
    if(foundGroupId != null) {
      return group.getId().equals(foundGroupId);
    }
    return false;  
  }
  
  @Override
  public void visitMembershipWithInheritance(OrgGroup group, OrgMembership membership, OrgMember user, boolean isDisabled) {
    if(isDisabled) {
      return;
    }
    
    if(foundGroupId == null) {
      return;
    }
    if(isDirectGroup(group)) {
      builder.addParenUsers(user);
    } else {
      builder.addChildUsers(user);
    }
    
  }
  @Override
  public void visitMembership(OrgGroup group, OrgMembership membership, OrgMember user, boolean isDisabled) {
    if(isDisabled) {
      return;
    }
    
    
    if(!isDirectGroup(group)) {
      return;
    }
    builder.addDirectUsers(user);
  }
  @Override
  public void visitRole(OrgGroup group, OrgPartyRight groupRole, OrgRole role, boolean isDisabled) {
    if(isDisabled) {
      return;
    }
    
    if(foundGroupId == null) {
      return;
    }
    builder.addDirectRoleNames(role);    
  }
  @Override
  public void visitRole(OrgGroup group, OrgMemberRight groupRole, OrgRole role, boolean isDisabled) {
    if(isDisabled) {
      return;
    }
    
  }
  
  @Override
  public void visitChild(OrgGroup group, boolean isDisabled) {
    if(isDisabled) {
      return;
    }
    
    if(foundGroupId == null) {
      return;
    }
    builder.addChildGroups(group);
  }
  @Override
  public void start(OrgGroup group, List<OrgGroup> parents, boolean isDisabled) {

    
    if( groupIdOrNameOrExternalId.equals(group.getExternalId()) ||
        groupIdOrNameOrExternalId.equals(group.getGroupName()) ||
        groupIdOrNameOrExternalId.equals(group.getId())) {
      
      foundGroupId = group.getId();
      builder
        .groupId(group.getId())
        .groupName(group.getGroupName())
        .externalId(group.getExternalId())
        .commitId(group.getCommitId())
        .parentGroupId(group.getParentId())
        .parentGroups(parents)
        .status(isDisabled ? OrgActorStatusType.DISABLED : OrgActorStatusType.IN_FORCE);
    }
  }
  @Override
  public void end(OrgGroup group, List<OrgGroup> parents, boolean isDisabled) {
    if(group.getId().equals(foundGroupId)) {
      foundGroupId = null;
    }
  }
  
  @Override
  public ImmutableOrgGroupHierarchy close() {
    return builder.log("").build();
  }
  @Override
  protected GroupVisitor visitTop(OrgGroup group, OrgAnyTreeContainerContext worldState) {
    return this;
  }
  @Override
  protected GroupVisitor visitChild(OrgGroup group, OrgAnyTreeContainerContext worldState) {
    return this;
  }
}
