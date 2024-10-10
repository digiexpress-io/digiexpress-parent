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
package io.dialob.executor.command;

import io.dialob.executor.model.DialobSession;
import io.dialob.executor.model.ErrorState;
import io.dialob.executor.model.ItemId;
import io.dialob.executor.model.ItemState;
import io.dialob.program.EvalContext;

import org.immutables.value.Value;

import javax.annotation.Nonnull;

interface AbstractPageCommand extends AbstractUpdateCommand<ItemId,ItemState>, ItemUpdateCommand {

  @Nonnull
  @Value.Default
  default ItemId getTargetId() {
    return DialobSession.QUESTIONNAIRE_REF;
  }

  default boolean pageIsInactive(EvalContext context, ItemId page) {
    if (page == null) {
      return true;
    }
    return !context.getItemState(page).map(ItemState::isActive).orElse(false);
  }

  default boolean anyErrors(EvalContext context) {
    return context.getErrorStates().stream().anyMatch(ErrorState::isActive);
  }

  default ItemState gotoPage(EvalContext context, ItemState itemState, ItemId page) {
    if (page != null) {
      return itemState.update()
        .setActivePage(page).get();
    }
    return itemState;
  }

}
