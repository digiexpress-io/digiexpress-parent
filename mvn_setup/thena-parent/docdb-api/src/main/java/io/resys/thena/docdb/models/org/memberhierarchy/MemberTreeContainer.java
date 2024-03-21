package io.resys.thena.docdb.models.org.memberhierarchy;

import java.util.List;

import org.immutables.value.Value;

import io.resys.thena.docdb.api.models.ThenaOrgObjects.OrgMemberPartyStatus;
import io.resys.thena.docdb.api.models.ThenaOrgObjects.OrgMemberRightStatus;

@Value.Immutable
public interface MemberTreeContainer {
  List<MemberTree> getRoots();
  List<OrgMemberRightStatus> getRoleStatus();
  List<OrgMemberPartyStatus> getGroupStatus();
  
  default <T> T accept(final TopDownVisitor<T> visitor) {
    getRoots().stream().sorted((a, b) -> a.getGroupName().compareTo(b.getGroupName()))
    .forEach(root -> {
      visitor.visitTop(root);
      acceptChildren(root, visitor);
    });
    return visitor.close();
  }
  default void acceptChildren(MemberTree parent, final TopDownVisitor<?> visitor) {
    parent.getChildren().values().stream().sorted((a, b) -> a.getGroupName().compareTo(b.getGroupName()))
    .forEach(child -> {
      visitor.visitChild(parent, child);
      acceptChildren(child, visitor);
    });
  }
  
  default <T> T accept(final BottomUpVisitor<T> visitor) {
    getRoots().stream().forEach(e -> {
      for(final MemberTree bottom : e.getLastNodes()) {
        visitor.visitBottom(bottom);
        
        MemberTree next = bottom.getParent();
        while(next != null) {
          visitor.visitParent(next);
          next = next.getParent();
        }
      }
    });
    return visitor.close();
  }

  
  interface TopDownVisitor<T> {
    void visitTop(MemberTree top);
    void visitChild(MemberTree parent, MemberTree child);
    T close();
  }
  
  interface BottomUpVisitor<T> {
    void visitBottom(MemberTree bottom);
    void visitParent(MemberTree parent);
    T close();
  }
}
