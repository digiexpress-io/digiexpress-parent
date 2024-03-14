package io.resys.thena.docdb.models.org.anytree;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AnyTreeContainerImpl<T> implements AnyTreeContainer<T> {
  private final AnyTreeContainerContext ctx;
  
  @Override
  public T accept(AnyTreeContainerVisitor<T> visitor) {
    visitor.start(ctx);
    return visitor.close();
  }
}
