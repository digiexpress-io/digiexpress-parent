package io.resys.thena.docdb.models.org.userhierarchy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgUserHierarchyEntry;

public class UserContainer {
  private final Map<String, List<OrgUserHierarchyEntry>> groupsById = new HashMap<>();
  private final Map<String, List<OrgUserHierarchyEntry>> groupsByParentId = new HashMap<>();
  private final Set<String> roots = new LinkedHashSet<>();
  

  public interface UserContainerVisitor<T> {
    UserContainerChildVisitor visitRoot(String rootGroupId);
    T close();
  }

  @FunctionalInterface
  public interface UserContainerChildVisitor {
    void visitChild(OrgUserHierarchyEntry entry);
  }
  
  public <T> T accept(UserContainerVisitor<T> visitor) {
    for(final var root : this.roots.stream()
        .sorted((a, b) -> groupsById.get(a).get(0).getGroupName().compareTo(groupsById.get(b).get(0).getGroupName()))
        .toList()) {
      
      final var next = visitor.visitRoot(root);
      groupsById.get(root).forEach(next::visitChild);
      visitChildren(root, next);
    }
    return visitor.close(); 
  }
  
  public UserContainer(List<OrgUserHierarchyEntry> init) {
    for(final var entry : init) {
      if(entry.getGroupParentId() == null) {
        roots.add(entry.getGroupId());
      }
      if(!groupsById.containsKey(entry.getGroupId())) {
        groupsById.put(entry.getGroupId(), new ArrayList<>());
      }
      groupsById.get(entry.getGroupId()).add(entry);
      
      if(!groupsByParentId.containsKey(entry.getGroupParentId())) {
        groupsByParentId.put(entry.getGroupParentId(), new ArrayList<>());
      }
      groupsByParentId.get(entry.getGroupParentId()).add(entry);
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
      visitChildren(child.getGroupId(), visitor);
    }
  }
 
  private static String getSortableId(OrgUserHierarchyEntry entry) {
    return entry.getGroupName() + entry.getRoleName() + entry.getGroupStatus() + entry.getRoleStatus();
  }
}