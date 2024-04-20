package io.resys.thena.structures.org.memberhierarchy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.resys.thena.api.entities.org.ImmutableOrgMemberPartyStatus;
import io.resys.thena.api.entities.org.ImmutableOrgMemberRightStatus;
import io.resys.thena.api.entities.org.OrgMemberHierarchyEntry;
import io.resys.thena.api.entities.org.OrgRightFlattened;
import io.resys.thena.api.entities.org.ThenaOrgObjects.OrgMemberPartyStatus;
import io.resys.thena.api.entities.org.ThenaOrgObjects.OrgMemberRightStatus;
import io.resys.thena.structures.org.memberhierarchy.MemberContainer.UserContainerChildVisitor;
import io.resys.thena.structures.org.memberhierarchy.MemberContainer.UserContainerVisitor;
import io.resys.thena.support.RepoAssert;


public class MemberContainerVisitorImpl implements UserContainerVisitor<MemberTreeContainer> {
  private final Map<String, OrgRightFlattened> roleData = new HashMap<>();
  private final Map<String, OrgMemberRightStatus> roleStatus = new HashMap<>();
  private final Map<String, OrgMemberPartyStatus> groupStatus = new HashMap<>();
  private final List<MemberTree> roots = new ArrayList<MemberTree>();
  
  public MemberContainerVisitorImpl(List<OrgRightFlattened> input) {
    super();
    input.forEach(role -> {
      RepoAssert.isTrue(!roleData.containsKey(role.getRightId()), () -> 
        "rightName/" + role.getRightName() + "/" +
        "rightId/" + role.getRightId() + "/" +
        "There can't be overlapping rights!");
      
      roleData.put(role.getRightId(), role);
      
      if(!roleStatus.containsKey(role.getRightId()) && role.getRightId() != null) {
        roleStatus.put(role.getRightId() , ImmutableOrgMemberRightStatus.builder()
          .rightId(role.getRightId())
          .status(role.getRightStatus())
          .build());
      }
    });
  }

  @Override
  public UserContainerChildVisitor visitRoot(String rootGroupId) {
    final var root = new MemberTree(rootGroupId);
    roots.add(root);
    return (entry) -> visitChild(root, entry);
  }
  
  private void visitChild(MemberTree root, OrgMemberHierarchyEntry next) {
    if(!groupStatus.containsKey(next.getPartyId()) && next.getPartyId() != null) {
      groupStatus.put(next.getPartyId(), ImmutableOrgMemberPartyStatus.builder()
          .partyId(next.getPartyId())
          .status(next.getPartyStatus())
          .build());
    }
    
    if(next.getPartyParentId() == null && next.getPartyId().equals(root.getGroupId())) {
      root.addGroupValue(next, roleData.get(next.getRightId()));
      return;
    }
    
    // find the parent
    final MemberTree parent = root.getNode(next.getPartyParentId());
    RepoAssert.notNull(parent, () -> "unknown parent id: " + next.getPartyParentId());
    
    // attach the child
    MemberTree child = parent.getNode(next.getPartyId());
    if(child == null) {
      child = parent.addChild(next.getPartyId());
    }
    child.addGroupValue(next, roleData.get(next.getRightId()));
  }

  @Override
  public MemberTreeContainer close() {
    
    
    final var result = ImmutableMemberTreeContainer.builder()
        .roots(roots)
        .roleStatus(roleStatus.values())
        .groupStatus(groupStatus.values())
        .build();
    
    return result;
  }
   
}
