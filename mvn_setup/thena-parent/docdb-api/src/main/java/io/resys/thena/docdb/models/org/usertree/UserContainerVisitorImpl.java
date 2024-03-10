package io.resys.thena.docdb.models.org.usertree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.resys.thena.docdb.api.models.ImmutableOrgUserGroupStatus;
import io.resys.thena.docdb.api.models.ImmutableOrgUserRoleStatus;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgGroupAndRoleFlattened;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgRoleFlattened;
import io.resys.thena.docdb.api.models.ThenaOrgObjects.OrgUserGroupStatus;
import io.resys.thena.docdb.api.models.ThenaOrgObjects.OrgUserRoleStatus;
import io.resys.thena.docdb.models.org.usertree.UserContainer.UserContainerChildVisitor;
import io.resys.thena.docdb.models.org.usertree.UserContainer.UserContainerVisitor;
import io.resys.thena.docdb.support.RepoAssert;


public class UserContainerVisitorImpl implements UserContainerVisitor<UserTreeContainer> {
  private final Map<String, OrgRoleFlattened> roleData = new HashMap<>();
  private final Map<String, OrgUserRoleStatus> roleStatus = new HashMap<>();
  private final Map<String, OrgUserGroupStatus> groupStatus = new HashMap<>();
  private final List<UserTree> roots = new ArrayList<UserTree>();
  
  public UserContainerVisitorImpl(List<OrgRoleFlattened> input) {
    super();
    input.forEach(role -> {
      RepoAssert.isTrue(!roleData.containsKey(role.getRoleId()), () -> "There can't be overlapping errors for role with name: " + role.getRoleName());
      roleData.put(role.getRoleId(), role);
      
      if(!roleStatus.containsKey(role.getRoleStatusId()) && role.getRoleStatusId() != null) {
        roleStatus.put(role.getRoleStatusId() , ImmutableOrgUserRoleStatus.builder()
          .roleId(role.getRoleId())
          .status(role.getRoleStatus())
          .statusId(role.getRoleStatusId())
          .build());
      }
    });
  }

  @Override
  public UserContainerChildVisitor visitRoot(String rootGroupId) {
    final var root = new UserTree(rootGroupId);
    roots.add(root);
    return (entry) -> visitChild(root, entry);
  }
  
  private void visitChild(UserTree root, OrgGroupAndRoleFlattened next) {
    if(!groupStatus.containsKey(next.getGroupStatusId()) && next.getGroupStatusId() != null) {
      groupStatus.put(next.getGroupStatusId(), ImmutableOrgUserGroupStatus.builder()
          .groupId(next.getGroupId())
          .status(next.getGroupStatus())
          .statusId(next.getGroupStatusId())
          .build());
    }
    
    if(next.getGroupParentId() == null && next.getGroupId().equals(root.getGroupId())) {
      root.addGroupValue(next, roleData.get(next.getRoleId()));
      return;
    }
    
    // find the parent
    final UserTree parent = root.getNode(next.getGroupParentId());
    RepoAssert.notNull(parent, () -> "unknown parent id: " + next.getGroupParentId());
    
    // attach the child
    UserTree child = parent.getNode(next.getGroupId());
    if(child == null) {
      child = parent.addChild(next.getGroupId());
    }
    child.addGroupValue(next, roleData.get(next.getRoleId()));
  }

  @Override
  public UserTreeContainer close() {
    
    
    final var result = ImmutableUserTreeContainer.builder()
        .roots(roots)
        .roleStatus(roleStatus.values())
        .groupStatus(groupStatus.values())
        .build();
    
    return result;
  }
   
}
