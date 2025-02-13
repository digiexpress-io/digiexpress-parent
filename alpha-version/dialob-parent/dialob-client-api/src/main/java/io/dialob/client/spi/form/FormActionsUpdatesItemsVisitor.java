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
package io.dialob.client.spi.form;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.annotation.Nonnull;

import io.dialob.api.proto.ActionItem;
import io.dialob.api.questionnaire.ImmutableError;
import io.dialob.compiler.Utils;
import io.dialob.executor.model.ErrorState;
import io.dialob.executor.model.IdUtils;
import io.dialob.executor.model.ItemState;
import io.dialob.executor.model.SessionObject;
import io.dialob.executor.model.ValueSetState;
import io.dialob.program.EvalContext;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FormActionsUpdatesItemsVisitor implements EvalContext.UpdatedItemsVisitor {

  private final FormActions formActions;
  private final Predicate<SessionObject> isVisiblePredicate;
  private final Function<ItemState, ActionItem> toActionItemFunction;

  @Override
  public Optional<UpdatedSessionStateVisitor> visitSession() {
    return Optional.of((original, updated) -> languageChanged(updated));
  }

  @Override
  public Optional<UpdatedErrorStateVisitor> visitUpdatedErrorStates() {
    return Optional.of((original, updated) -> {
      final boolean updatedVisible = updated != null && isVisiblePredicate.test(updated);
      final boolean originalVisible = original != null && isVisiblePredicate.test(original);
      if (originalVisible && !updatedVisible) {
        inactivated(original);
        return;
      }
      if (!originalVisible && updatedVisible) {
        activated(updated);
        return;
      }
      if (updatedVisible) {
        updated(updated);
      }
    });
  }

  @Override
  public Optional<UpdatedItemStateVisitor> visitUpdatedItems() {
    return Optional.of((original, updated) -> {
      final boolean updatedVisible = updated != null && isVisiblePredicate.test(updated);
      final boolean originalVisible = original != null && isVisiblePredicate.test(original);
      if (!originalVisible && !updatedVisible) {
        return;
      }
      if (originalVisible && !updatedVisible) {
        inactivated(updated != null ? updated : original);
        return;
      }
      if (!originalVisible) {
        activated(updated);
        return;
      }
      // here updatedVisible == true and originalVisible == true, so need to check change of disabled
      if (original.isDisabled() && !updated.isDisabled()) {
        enabled(updated);
        return;
      }
      if (!original.isDisabled() && (updated.isDisabled())) {
        disabled(updated);
        return;
      }
      if (!Objects.equals(original, updated)) {
        updated(updated);
      }
    });
  }

  @Override
  public Optional<UpdatedValueSetVisitor> visitUpdatedValueSets() {
    return Optional.of((original, updated) -> {
      if (updated != null) {
        updated(updated);
      }
    });
  }

  @Nonnull
  public FormActions getFormActions() {
    return formActions;
  }

  protected void updated(@Nonnull ErrorState updated) {
    // triggered when error label is updated
    formActions.addError(Utils.toError(updated));
  }

  protected void updated(@Nonnull ValueSetState updated) {
    formActions.newValueSet(Utils.toValueSet(updated));
  }

  protected void activated(@Nonnull ErrorState updated) {
    formActions.addError(Utils.toError(updated));
  }

  protected void inactivated(@Nonnull ErrorState updated) {
    formActions.removeError(ImmutableError.builder().id(IdUtils.toString(updated.getItemId())).code(updated.getCode()).build());
  }

  protected void disabled(@Nonnull ItemState updated) {
    updated(updated);
  }

  protected void enabled(@Nonnull ItemState updated) {
    updated(updated);
  }

  protected void activated(@Nonnull ItemState updated) {
    formActions.newQuestion(toActionItemFunction.apply(updated));
  }

  protected void inactivated(@Nonnull ItemState updated) {
    formActions.removeQuestion(IdUtils.toString(updated.getId()));
  }

  protected void updated(@Nonnull ItemState updated) {
    formActions.updateQuestion(toActionItemFunction.apply(updated));
  }

  protected void languageChanged(@Nonnull String language) {
    formActions.locale(new Locale(language));
  }

  @Override
  public void visitCompleted() {
    formActions.complete();
  }

}
