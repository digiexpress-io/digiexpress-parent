package io.resys.thena.api.entities;

import java.util.List;

import org.immutables.value.Value;

import jakarta.annotation.Nullable;


@Value.Immutable
public interface PageQuery<P> {
  int getPageNumber(); // page to query
  int getPageSize();   // size of the page 
  long getOffset();    // start the results after n-th object
  PageSorting<P> getSort();
  
  @Value.Immutable
  interface PageSorting<P> {
    List<PageSortingOrder<P>> getOrders();
  }
  
  @Value.Immutable
  interface PageSortingOrder<P> {
    PageSortDirection getDirection();
    P getProperty();
    @Nullable String getPropertyType();
  }
  
  enum PageSortDirection { ASC, DESC }
}
