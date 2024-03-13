package io.resys.thena.docdb.models.org.userhierarchy;

import java.util.List;

import org.immutables.value.Value;

import io.resys.thena.docdb.api.models.ThenaOrgObjects.OrgUserGroupStatus;
import io.resys.thena.docdb.api.models.ThenaOrgObjects.OrgUserRoleStatus;

@Value.Immutable
public interface UserTreeContainer {
  List<UserTree> getRoots();
  List<OrgUserRoleStatus> getRoleStatus();
  List<OrgUserGroupStatus> getGroupStatus();
  
  default <T> T accept(final TopDownVisitor<T> visitor) {
    getRoots().stream().sorted((a, b) -> a.getGroupName().compareTo(b.getGroupName()))
    .forEach(root -> {
      visitor.visitTop(root);
      acceptChildren(root, visitor);
    });
    return visitor.close();
  }
  default void acceptChildren(UserTree parent, final TopDownVisitor<?> visitor) {
    parent.getChildren().values().stream().sorted((a, b) -> a.getGroupName().compareTo(b.getGroupName()))
    .forEach(child -> {
      visitor.visitChild(parent, child);
      acceptChildren(child, visitor);
    });
  }
  
  default <T> T accept(final BottomUpVisitor<T> visitor) {
    getRoots().stream().forEach(e -> {
      for(final UserTree bottom : e.getLastNodes()) {
        visitor.visitBottom(bottom);
        
        UserTree next = bottom.getParent();
        while(next != null) {
          visitor.visitParent(next);
          next = next.getParent();
        }
      }
    });
    return visitor.close();
  }

  
  interface TopDownVisitor<T> {
    void visitTop(UserTree top);
    void visitChild(UserTree parent, UserTree child);
    T close();
  }
  
  interface BottomUpVisitor<T> {
    void visitBottom(UserTree bottom);
    void visitParent(UserTree parent);
    T close();
  }
}
