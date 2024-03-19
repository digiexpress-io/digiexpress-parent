package io.resys.thena.docdb.models.org.userhierarchy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.resys.thena.docdb.api.models.ImmutableOrgUserGroupStatus;
import io.resys.thena.docdb.api.models.ImmutableOrgUserRoleStatus;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgMemberHierarchyEntry;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgRightFlattened;
import io.resys.thena.docdb.api.models.ThenaOrgObjects.OrgUserGroupStatus;
import io.resys.thena.docdb.api.models.ThenaOrgObjects.OrgUserRoleStatus;
import io.resys.thena.docdb.models.org.userhierarchy.UserContainer.UserContainerChildVisitor;
import io.resys.thena.docdb.models.org.userhierarchy.UserContainer.UserContainerVisitor;
import io.resys.thena.docdb.support.RepoAssert;


public class UserContainerVisitorImpl implements UserContainerVisitor<UserTreeContainer> {
  private final Map<String, OrgRightFlattened> roleData = new HashMap<>();
  private final Map<String, OrgUserRoleStatus> roleStatus = new HashMap<>();
  private final Map<String, OrgUserGroupStatus> groupStatus = new HashMap<>();
  private final List<UserTree> roots = new ArrayList<UserTree>();
  
  public UserContainerVisitorImpl(List<OrgRightFlattened> input) {
    super();
    input.forEach(role -> {
      RepoAssert.isTrue(!roleData.containsKey(role.getRightId()), () -> "There can't be overlapping errors for role with name: " + role.getRightName());
      roleData.put(role.getRightId(), role);
      
      if(!roleStatus.containsKey(role.getRightStatusId()) && role.getRightStatusId() != null) {
        roleStatus.put(role.getRightStatusId() , ImmutableOrgUserRoleStatus.builder()
          .roleId(role.getRightId())
          .status(role.getRightStatus())
          .statusId(role.getRightStatusId())
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
  
  private void visitChild(UserTree root, OrgMemberHierarchyEntry next) {
    if(!groupStatus.containsKey(next.getPartyStatusId()) && next.getPartyStatusId() != null) {
      groupStatus.put(next.getPartyStatusId(), ImmutableOrgUserGroupStatus.builder()
          .groupId(next.getPartyId())
          .status(next.getPartyStatus())
          .statusId(next.getPartyStatusId())
          .build());
    }
    
    if(next.getPartyParentId() == null && next.getPartyId().equals(root.getGroupId())) {
      root.addGroupValue(next, roleData.get(next.getRightId()));
      return;
    }
    
    // find the parent
    final UserTree parent = root.getNode(next.getPartyParentId());
    RepoAssert.notNull(parent, () -> "unknown parent id: " + next.getPartyParentId());
    
    // attach the child
    UserTree child = parent.getNode(next.getPartyId());
    if(child == null) {
      child = parent.addChild(next.getPartyId());
    }
    child.addGroupValue(next, roleData.get(next.getRightId()));
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
