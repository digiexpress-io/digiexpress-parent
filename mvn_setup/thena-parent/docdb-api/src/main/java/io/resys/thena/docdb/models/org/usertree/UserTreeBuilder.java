package io.resys.thena.docdb.models.org.usertree;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.barfuin.texttree.api.DefaultNode;
import org.barfuin.texttree.api.TextTree;
import org.barfuin.texttree.api.TreeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.resys.thena.docdb.api.LogConstants;
import io.resys.thena.docdb.api.models.ImmutableOrgUserGroupsAndRolesWithLog;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgActorStatusType;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgGroupAndRoleFlattened;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgRoleFlattened;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgUserFlattened;
import io.resys.thena.docdb.api.models.ThenaOrgObjects.OrgUserGroupsAndRolesWithLog;
import io.resys.thena.docdb.models.org.usertree.UserTreeContainer.TopDownVisitor;
import io.resys.thena.docdb.support.RepoAssert;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class UserTreeBuilder {

  private Logger logger;
  
  private OrgUserFlattened user;
  private List<OrgGroupAndRoleFlattened> groupData;
  private List<OrgRoleFlattened> roleData;
  
  public UserTreeBuilder user(OrgUserFlattened user) {                         this.user = RepoAssert.notNull(user,            () -> "user can't be empty!"); return this; }
  public UserTreeBuilder groupData(List<OrgGroupAndRoleFlattened> groupData) { this.groupData = RepoAssert.notNull(groupData,  () -> "groupData can't be empty!"); return this; }
  public UserTreeBuilder roleData(List<OrgRoleFlattened> roleData) {           this.roleData = RepoAssert.notNull(roleData,    () -> "roleData can't be null!"); return this; }
  
  
  public OrgUserGroupsAndRolesWithLog build() {
    RepoAssert.notNull(user,      () -> "user can't be empty!");
    RepoAssert.notNull(groupData, () -> "groupData can't be empty!");
    RepoAssert.notNull(roleData,  () -> "roleData can't be null!");
    initLogger();
    logInputs();
    
    // format data
    final var visitor = new UserContainerVisitorImpl(roleData);
    final UserTreeContainer container = new UserContainer(groupData).accept(visitor);

    // create end result
    final ImmutableUserGroupsData result = container.accept(new CreateGroups(logger, user, roleData));
    final String log = container.accept(new CreateLog(logger, user));
    
    
    return ImmutableOrgUserGroupsAndRolesWithLog.builder()
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


  private static class CreateLog implements TopDownVisitor<String> {
    private final Logger logger;
    private final OrgUserFlattened user;
    private final DefaultNode userNode;
    private final DefaultNode userRoles = new DefaultNode("roles");
    private final DefaultNode groups = new DefaultNode("groups");

    private final DefaultNode greyRoles = new DefaultNode("grey-roles");
    private final DefaultNode greyGroups = new DefaultNode("grey-groups");

    private final Map<String, DefaultNode> groupNodes = new HashMap<>();
    
    public CreateLog(Logger logger, OrgUserFlattened user) {
      this.logger = logger;
      this.user = user;

      this.userNode = new DefaultNode(user.getUserName());
      this.userNode.addChild(userRoles);
      this.userNode.addChild(groups);
      this.userNode.addChild(greyRoles);
      this.userNode.addChild(greyGroups);

    }
    
    @Override
    public void visitTop(UserTree top) {
      final var groupNode = new DefaultNode(top.getGroupName());
      groupNodes.put(top.getGroupId(), groupNode);
      visitRoles(top);
      
      if(top.isGreyGroup()) {
        greyGroups.addChild(groupNode);
      } else {
        groups.addChild(groupNode);        
      }

    }
    @Override
    public void visitChild(UserTree parent, UserTree child) {
      final var groupNode = new DefaultNode(child.getGroupName());
      groupNodes.put(child.getGroupId(), groupNode);
      visitRoles(child);
      
      if(child.isGreyGroup()) {
        greyGroups.addChild(groupNode); 
      } else {
        groupNodes.get(parent.getGroupId()).addChild(groupNode);
      }
    }
    
    private void visitRoles(UserTree tree) {
      final var roles = new DefaultNode("roles"); 
      groupNodes.get(tree.getGroupId()).addChild(roles);
      
      for(final var role : tree.getRoleValues().values().stream()
          .sorted((a, b) -> a.getRoleName().compareTo(b.getRoleName()))
          .toList()) {
        
        if(role.getRoleStatus() == OrgActorStatusType.IN_FORCE || role.getRoleStatus() == null) {
          roles.addChild(new DefaultNode(role.getRoleName()));
        } else {
          roles.addChild(new DefaultNode(role.getRoleName() + " - " + role.getRoleStatus()));
        } 
      }
    }
    
    @Override
    public String close() {
      final var options = new TreeOptions();
      final var tree = TextTree.newInstance(options).render(userNode);
      return tree;
    }
  }
}
