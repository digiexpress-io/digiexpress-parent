package io.resys.thena.structures.org.memberhierarchy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.resys.thena.api.entities.org.OrgMemberHierarchyEntry;

public class MemberContainer {
  private final Map<String, List<OrgMemberHierarchyEntry>> groupsById = new HashMap<>();
  private final Map<String, List<OrgMemberHierarchyEntry>> groupsByParentId = new HashMap<>();
  private final Set<String> roots = new LinkedHashSet<>();
  

  public interface UserContainerVisitor<T> {
    UserContainerChildVisitor visitRoot(String rootGroupId);
    T close();
  }

  @FunctionalInterface
  public interface UserContainerChildVisitor {
    void visitChild(OrgMemberHierarchyEntry entry);
  }
  
  public <T> T accept(UserContainerVisitor<T> visitor) {
    for(final var root : this.roots.stream()
        .sorted((a, b) -> groupsById.get(a).get(0).getPartyName().compareTo(groupsById.get(b).get(0).getPartyName()))
        .toList()) {
      
      final var next = visitor.visitRoot(root);
      groupsById.get(root).forEach(next::visitChild);
      visitChildren(root, next);
    }
    return visitor.close(); 
  }
  
  public MemberContainer(List<OrgMemberHierarchyEntry> init) {
    for(final var entry : init) {
      if(entry.getPartyParentId() == null) {
        roots.add(entry.getPartyId());
      }
      if(!groupsById.containsKey(entry.getPartyId())) {
        groupsById.put(entry.getPartyId(), new ArrayList<>());
      }
      groupsById.get(entry.getPartyId()).add(entry);
      
      if(!groupsByParentId.containsKey(entry.getPartyParentId())) {
        groupsByParentId.put(entry.getPartyParentId(), new ArrayList<>());
      }
      groupsByParentId.get(entry.getPartyParentId()).add(entry);
    }
  }
  
  
  private void visitChildren(String groupId, UserContainerChildVisitor visitor) {
    final var children = groupsByParentId.get(groupId);
    if(children == null) {
      // reached the end
      return;
    }
    for(final var child : children.stream()
        .sorted((a, b) -> getSortableId(a).compareTo(getSortableId(b)))
        .toList()) {
      visitor.visitChild(child);
      visitChildren(child.getPartyId(), visitor);
    }
  }
 
  private static String getSortableId(OrgMemberHierarchyEntry entry) {
    return entry.getPartyName() + entry.getRightName() + entry.getPartyStatus() + entry.getRightStatus();
  }
}