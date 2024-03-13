package io.resys.thena.docdb.models.org.anytree;

import io.resys.thena.docdb.api.models.ThenaOrgObjects.OrgGroupHierarchy;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GroupHierarchyContainer implements AnyTreeContainer<OrgGroupHierarchy> {
  private final AnyTreeContainerContext ctx;
  
  @Override
  public OrgGroupHierarchy accept(AnyTreeContainerVisitor<OrgGroupHierarchy> visitor) {
    visitor.start(ctx);
    return visitor.close();
  }
}
