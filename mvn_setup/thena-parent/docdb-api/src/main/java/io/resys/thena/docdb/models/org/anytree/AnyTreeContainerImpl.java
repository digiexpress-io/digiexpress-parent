package io.resys.thena.docdb.models.org.anytree;

import io.resys.thena.docdb.api.visitors.OrgTreeContainer;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AnyTreeContainerImpl implements OrgTreeContainer {
  private final OrgAnyTreeContainerContext ctx;
  
  @Override
  public <T> T accept(OrgAnyTreeContainerVisitor<T> visitor) {
    visitor.start(ctx);
    return visitor.close();
  }
}
