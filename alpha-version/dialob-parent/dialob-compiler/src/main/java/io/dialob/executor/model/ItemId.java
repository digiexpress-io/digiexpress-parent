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

import org.immutables.value.Value;

import java.io.Serializable;
import java.util.Optional;

public interface ItemId extends Serializable {

  @Value.Parameter(order = 1)
  Optional<ItemId> getParent();

  String getValue();

  default boolean isPartial() {
    return getParent().map(ItemId::isPartial).orElse(false);
  }

  default <I extends ItemId> ItemId withParent(I parent) {
    return this;
  }

  default ItemId withParent(Optional<? extends ItemId> parent) {
    return this;
  }

}
