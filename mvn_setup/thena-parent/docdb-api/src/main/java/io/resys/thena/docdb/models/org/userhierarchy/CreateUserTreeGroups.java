package io.resys.thena.docdb.models.org.userhierarchy;

import java.util.List;
import java.util.Optional;

import org.immutables.value.Value;
import org.slf4j.Logger;

import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgRightFlattened;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgMemberFlattened;
import io.vertx.core.json.JsonObject;



public class CreateUserTreeGroups extends BottomUpVisitorTemplate<ImmutableUserGroupsData> {

  private final Logger logger;
  private final OrgMemberFlattened user;
  private final ImmutableUserGroupsData.Builder groups = ImmutableUserGroupsData.builder();
  
  public CreateUserTreeGroups(List<OrgRightFlattened> globalRoles, Logger logger, OrgMemberFlattened user) {
    super(globalRoles);
    this.logger = logger;
    this.user = user;
  }

  @Override
  public void visitParent(UserTree parent) {
    super.visitParent(parent);
    
    if(parent.getParent() == null && logger != null) {
      logger.debug("org/user_tree_inputs/{}/user_group_tree \r\n{}", user.getUserName(), JsonObject.mapFrom(parent).encodePrettily());
    }
  }  
  @Override
  public ImmutableUserGroupsData close() {
    return groups.build();
  }
  @Override
  public void visitGlobalRoleEnabled(String roleName) {
    groups.addRoleNames(roleName);
    groups.addRoleNamesWithDirectMembership(roleName);    
  }
  @Override
  public void visitGlobalRoleDisabled(String roleName) {

  }
  @Override
  public void visitGroupEnabled(String groupName, Optional<String> parentGroupName, boolean isDirect) {
    if(isDirect) {
      groups.addGroupNamesWithDirectMembership(groupName);
    }
    groups.addGroupNames(groupName);    
  }
  @Override
  public void visitGroupDisabled(String groupName, Optional<String> parentGroupName, boolean isDirect) {
    if(isDirect) {
      groups.addGroupNamesWithDirectMembershipDisabled(groupName);
    }
  }
  @Override
  public void visitRoleEnabled(String roleName, String groupName, Optional<String> parentGroupName, boolean isDirect) {
    groups.addRoleNames(roleName);
    if(isDirect) {
      groups.addRoleNamesWithDirectMembership(roleName);
    }
  }
  @Override
  public void visitRoleDisabled(String roleName, String groupName, Optional<String> parentGroupName, boolean isDirect) {    
  
  }
  @Value.Immutable
  public interface UserGroupsData {
    List<String> getRoleNames();
    List<String> getGroupNames();
    List<String> getGroupNamesWithDirectMembership();
    List<String> getRoleNamesWithDirectMembership();
    List<String> getGroupNamesWithDirectMembershipDisabled();
  }


}
