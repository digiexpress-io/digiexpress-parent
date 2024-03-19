package io.resys.thena.docdb.models.org.userhierarchy;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.resys.thena.docdb.api.LogConstants;
import io.resys.thena.docdb.api.models.ImmutableOrgUserHierarchy;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgActorStatusType;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgMemberHierarchyEntry;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgRightFlattened;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgMemberFlattened;
import io.resys.thena.docdb.api.models.ThenaOrgObjects.OrgUserHierarchy;
import io.resys.thena.docdb.support.RepoAssert;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class UserTreeBuilder {

  private Logger logger;
  
  private OrgMemberFlattened user;
  private List<OrgMemberHierarchyEntry> groupData;
  private List<OrgRightFlattened> roleData;
  
  public UserTreeBuilder user(OrgMemberFlattened user) {                         this.user = RepoAssert.notNull(user,            () -> "user can't be empty!"); return this; }
  public UserTreeBuilder groupData(List<OrgMemberHierarchyEntry> groupData) { this.groupData = RepoAssert.notNull(groupData,  () -> "groupData can't be empty!"); return this; }
  public UserTreeBuilder roleData(List<OrgRightFlattened> roleData) {           this.roleData = RepoAssert.notNull(roleData,    () -> "roleData can't be null!"); return this; }
  
  
  public OrgUserHierarchy build() {
    RepoAssert.notNull(user,      () -> "user can't be empty!");
    RepoAssert.notNull(groupData, () -> "groupData can't be empty!");
    RepoAssert.notNull(roleData,  () -> "roleData can't be null!");
    initLogger();
    logInputs();
    
    // format data
    final var visitor = new UserContainerVisitorImpl(roleData);
    final UserTreeContainer container = new UserContainer(groupData).accept(visitor);

    // create end result
    final ImmutableUserGroupsData result = container.accept(new CreateUserTreeGroups(roleData, logger, user));
    final String log = container.accept(new CreateUserTreeGroupsLog(roleData, user));
    
    
    return ImmutableOrgUserHierarchy.builder()
      .userId(user.getId())
      .userName(user.getUserName())
      .externalId(user.getExternalId())
      .email(user.getEmail())
      .commitId(user.getCommitId())
      .status(user.getStatus() == null ? OrgActorStatusType.IN_FORCE : user.getStatus())
      .addAllUserRoleStatus(container.getRoleStatus())
      .addAllUserGroupStatus(container.getGroupStatus())
      .roleNames(result.getRoleNames())
      .groupNames(result.getGroupNames())
      .addAllDirectRoleNames(result.getRoleNamesWithDirectMembership())
      .addAllDirectGroupNames(result.getGroupNamesWithDirectMembership())
      .log(log)
      .build();
  }

  private void logInputs() {
    if(logger != null) {    
      logger.debug("org/user_tree_inputs/{}/user_data \r\n{}", user.getUserName(), JsonObject.mapFrom(user).encodePrettily());
      logger.debug("org/user_tree_inputs/{}/role_data \r\n{}", user.getUserName(), new JsonArray(roleData).encodePrettily());
      logger.debug("org/user_tree_inputs/{}/group_data \r\n{}", user.getUserName(), new JsonArray(groupData).encodePrettily());
    }
  }
  
  private void initLogger() {
    final var pkgLevel = LoggerFactory.getLogger(LogConstants.SHOW_ORG_USER_TREE_CALC);
    if(pkgLevel.isDebugEnabled()) {
      this.logger = pkgLevel;
      return;
    }
    final var userLevel = LoggerFactory.getLogger(LogConstants.SHOW_ORG_USER_TREE_CALC + "_" + user.getUserName());
    if(userLevel.isDebugEnabled()) {
      this.logger = userLevel;
    }
  }
}
