package io.resys.thena.api.entities;

/*-
 * #%L
 * thena-docdb-api
 * %%
 * Copyright (C) 2015 - 2024 Copyright 2022 ReSys OÜ
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

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
