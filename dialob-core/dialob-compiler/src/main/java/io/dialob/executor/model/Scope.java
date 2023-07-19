/*
 * Copyright © 2015 - 2021 ReSys (info@dialob.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.dialob.executor.model;

import java.io.Serializable;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nonnull;

import org.immutables.value.Value;

@Value.Immutable
public interface Scope extends Serializable {

  @Nonnull
  @Value.Parameter(order = -1000)
  ItemId getScopeId();

  @Value.Parameter
  Set<ItemId> getScopeItems();

  /**
   * Maps item id to current evaluation scope. This is used to map plain row group item to current row.
   *
   * @param itemId plain item id
   * @param ignoreScopeItems if mapped item id is not found from scope,
   * @return item id from this scope
   */
  default ItemId mapTo(final ItemId itemId, final boolean ignoreScopeItems) {
    return getScopeId().getParent().map(scopeParent -> {
      ItemId scopedId = itemId;
      if (!itemId.getParent().isPresent()){
        scopedId = ImmutableItemRef.of(itemId.getValue(), Optional.of(getScopeId()));
      } else {
        if (IdUtils.matches(itemId, getScopeId())) {
          scopedId = getScopeId();
        } else if (itemId.isPartial() && itemId.getParent().isPresent()) {
          ItemId parentId = itemId.getParent().get();
          if (IdUtils.matches(parentId, getScopeId())) {
            scopedId = itemId.withParent(getScopeId());
          }
        }
      }
      if (!ignoreScopeItems && !getScopeItems().contains(scopedId)) {
        scopedId = itemId;
      }
      return scopedId;
    }).orElse(itemId);
  }

}
