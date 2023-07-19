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

import static io.dialob.executor.command.CommandFactory.ErrorStateMatcher.ERROR_ACTIVITY_CHANGED;
import static io.dialob.executor.command.CommandFactory.ItemStatePredicates.ALWAYS;
import static io.dialob.executor.command.CommandFactory.ItemStatePredicates.GROUP_ITEMS_CHANGED;
import static io.dialob.executor.command.CommandFactory.ItemStatePredicates.ITEM_ACTIVITY_CHANGED;
import static io.dialob.executor.command.CommandFactory.ItemStatePredicates.ITEM_ANSWERED_STATE_CHANGED;
import static io.dialob.executor.command.CommandFactory.ItemStatePredicates.ITEM_DESCRIPTION_CHANGED;
import static io.dialob.executor.command.CommandFactory.ItemStatePredicates.ITEM_INVALIDITY_CHANGED;
import static io.dialob.executor.command.CommandFactory.ItemStatePredicates.ITEM_INVALID_ANSWERS_CHANGED;
import static io.dialob.executor.command.CommandFactory.ItemStatePredicates.ITEM_LABEL_CHANGED;
import static io.dialob.executor.command.CommandFactory.ItemStatePredicates.ITEM_REQUIRED_CHANGED;
import static io.dialob.executor.command.CommandFactory.ItemStatePredicates.ITEM_STATE_CHANGED;
import static io.dialob.executor.command.CommandFactory.ItemStatePredicates.ITEM_STATUS_CHANGED;
import static io.dialob.executor.command.Triggers.itemsChangedEvent;
import static io.dialob.executor.command.Triggers.onTarget;
import static io.dialob.executor.command.Triggers.sessionLocaleUpdatedEvent;
import static io.dialob.executor.command.Triggers.stateChangedEvent;
import static io.dialob.executor.command.Triggers.trigger;
import static java.util.Collections.emptyList;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableList;

import io.dialob.executor.command.event.ImmutableProtoTypeItemsAddedEventsProvider;
import io.dialob.executor.command.event.ImmutableRowItemsAddedEventsProvider;
import io.dialob.executor.command.event.ImmutableRowItemsRemovedEventsProvider;
import io.dialob.executor.model.ErrorId;
import io.dialob.executor.model.ErrorState;
import io.dialob.executor.model.ItemId;
import io.dialob.executor.model.ItemIdPartial;
import io.dialob.executor.model.ItemState;
import io.dialob.executor.model.ItemStates;
import io.dialob.executor.model.ValueSetId;
import io.dialob.program.model.Expression;
import io.dialob.program.model.Value;
import io.dialob.program.model.ValueSet;

public final class CommandFactory {

  private static boolean isNew(Object itemState, Object updateState) {
    return itemState == null && updateState != null;
  }
  private static boolean isRemoved(Object itemState, Object updateState) {
    return itemState != null && updateState == null;
  }

  private static boolean isNewOrRemoved(Object itemState, Object updateState) {
    return itemState != updateState && (itemState == null || updateState == null);
  }
  private static boolean notSame(Object itemState, Object updateState) {
    return itemState != updateState;
  }

  private static boolean notRemoved(Object itemState, Object updateState) {
    return itemState == null || updateState != null;
  }

  public enum ItemStatePredicates implements BiPredicate<ItemState, ItemState> {
    ALWAYS {
      @Override
      public boolean test(ItemState itemState, ItemState updateState) {
        return true;
      }
    },
    ITEM_STATE_CHANGED {
      @Override
      public boolean test(ItemState itemState, ItemState updateState) {
        return notSame(itemState, updateState) && (isNew(itemState, updateState) || updateState != itemState) && notRemoved(itemState, updateState);
      }
    },
    GROUP_ITEMS_CHANGED {
      @Override
      public boolean test(ItemState itemState, ItemState updateState) {
        return notSame(itemState, updateState) && (isNewOrRemoved(itemState, updateState) || !itemState.getItems().equals(updateState.getItems()));
      }
    },
    ITEM_ACTIVITY_CHANGED {
      @Override
      public boolean test(ItemState itemState, ItemState updateState) {
        return notSame(itemState, updateState) && (isNewOrRemoved(itemState, updateState) || updateState.isActive() != itemState.isActive());
      }
    },
    ITEM_LABEL_CHANGED {
      @Override
      public boolean test(ItemState itemState, ItemState updateState) {
        return notSame(itemState, updateState) && (isNewOrRemoved(itemState, updateState) || !Objects.equals(updateState.getLabel(), itemState.getLabel()));
      }
    },

    ITEM_DESCRIPTION_CHANGED {
      @Override
      public boolean test(ItemState itemState, ItemState updateState) {
        return notSame(itemState, updateState) && (isNewOrRemoved(itemState, updateState) || !Objects.equals(updateState.getDescription(), itemState.getDescription()));
      }
    },

    ITEM_REQUIRED_CHANGED {
      @Override
      public boolean test(ItemState itemState, ItemState updateState) {
        return notSame(itemState, updateState) && (isNewOrRemoved(itemState, updateState) || updateState.isRequired() != itemState.isRequired());
      }
    },
    ITEM_STATUS_CHANGED {
      @Override
      public boolean test(ItemState itemState, ItemState updateState) {
        return notSame(itemState, updateState) && (isNewOrRemoved(itemState, updateState) || updateState.getStatus() != itemState.getStatus());
      }
    },
    ITEM_INVALIDITY_CHANGED {
      @Override
      public boolean test(ItemState itemState, ItemState updateState) {
        return notSame(itemState, updateState) && (isNewOrRemoved(itemState, updateState) || updateState.isInvalid() != itemState.isInvalid());
      }
    },
    ITEM_INVALID_ANSWERS_CHANGED {
      @Override
      public boolean test(ItemState itemState, ItemState updateState) {
        return notSame(itemState, updateState) && (isNewOrRemoved(itemState, updateState) || updateState.isInvalidAnswers() != itemState.isInvalidAnswers());
      }
    },
    ITEM_ANSWERED_STATE_CHANGED {
      @Override
      public boolean test(ItemState itemState, ItemState updateState) {
        return notSame(itemState, updateState) && (isNewOrRemoved(itemState, updateState) || updateState.isAnswered() != itemState.isAnswered());
      }
    }
  }

  enum ItemStatesPredicates implements BiPredicate<ItemStates, ItemStates> {
    ITEM_STATES_CHANGED {
      @Override
      public boolean test(ItemStates itemState, ItemStates itemState2) {
        return itemState.getItemStates() != itemState2.getItemStates();
      }
    }
  }

  enum ErrorStateMatcher implements BiPredicate<ErrorState, ErrorState> {
    ERROR_STATE_CHANGED {
      @Override
      public boolean test(ErrorState itemState, ErrorState updateState) {
        return updateState != itemState;
      }
    },
    ERROR_ACTIVITY_CHANGED {
      @Override
      public boolean test(ErrorState itemState, ErrorState updateState) {
        return isNewOrRemoved(itemState, updateState) || updateState.isActive() != itemState.isActive();
      }
    }
  }

  private static final ImmutableList<Trigger<ItemState>> ACTIVE_PAGE_TRIGGERS = ImmutableList.of(Triggers.<ItemState>trigger(Triggers.activePageUpdatedEvent()).when(ITEM_STATE_CHANGED));

  private static final NextPage NEXT_PAGE = ImmutableNextPage.of(ACTIVE_PAGE_TRIGGERS);

  private static final PrevPage PREV_PAGE = ImmutablePrevPage.of(ACTIVE_PAGE_TRIGGERS);

  private static final Complete COMPLETE = ImmutableComplete.builder().build();

  private CommandFactory() {
  }

  public static NextPage nextPage() {
    return NEXT_PAGE;
  }

  public static PrevPage prevPage() {
    return PREV_PAGE;
  }

  public static Complete complete() {
    return COMPLETE;
  }

  public static GotoPage gotoPage(@Nonnull ItemId page) {
    return ImmutableGotoPage.of(page, ACTIVE_PAGE_TRIGGERS);
  }

  public static SetAnswer setAnswer(@Nonnull ItemId questionId, Object answer) {
    return ImmutableSetAnswer.of(questionId, answer, Arrays.asList(
      Triggers.<ItemState>trigger(stateChangedEvent(questionId)).when(ITEM_STATE_CHANGED),
      Triggers.<ItemState>trigger(Triggers.validityUpdatedEvent(onTarget(questionId))).when(ITEM_INVALIDITY_CHANGED),
      Triggers.<ItemState>trigger(Triggers.answeredUpdatedEvent(onTarget(questionId))).when(ITEM_ANSWERED_STATE_CHANGED)
    ));
  }

  public static SetLocale setLocale(@Nonnull String locale) {
    return ImmutableSetLocale.of(locale, Arrays.asList(
      Triggers.<ItemState>trigger(sessionLocaleUpdatedEvent()).when(ALWAYS)
    ));
  }

  public static SetVariableValue setVariableValue(@Nonnull ItemId id, Object value) {
    return ImmutableSetVariableValue.of(id, value, Arrays.asList(
      Triggers.<ItemState>trigger(stateChangedEvent(id)).when(ITEM_STATE_CHANGED),
      Triggers.<ItemState>trigger(Triggers.statusUpdatedEvent(onTarget(id))).when(ITEM_STATUS_CHANGED)
    ));
  }

  public static SetVariableFailed setVariableFailed(@Nonnull ItemId id) {
    return ImmutableSetVariableFailed.of(id, ImmutableList.of(
      Triggers.<ItemState>trigger(Triggers.statusUpdatedEvent(onTarget(id))).when(ITEM_STATUS_CHANGED)
    ));
  }

  public static ItemUpdateCommand deleteRow(@Nonnull ItemId toBeRemoved) {
    return toBeRemoved.getParent().map(parent -> (ItemUpdateCommand) ImmutableDeleteRow.of(parent, toBeRemoved, ImmutableList.of(Triggers.<ItemState>trigger(stateChangedEvent(parent)).when(ITEM_STATE_CHANGED)))).orElseGet(() -> ImmutableNopCommand.of(toBeRemoved, emptyList()));
  }

  public static SetRows addRows(@Nonnull ItemId id, @Nonnull List<Integer> ids) {
    return ImmutableSetRows.of(id, ids, ImmutableList.of(Triggers.<ItemState>trigger(stateChangedEvent(id)).when(ITEM_STATE_CHANGED)));
  }

  public static AddRow addRow(@Nonnull ItemId targetId) {
    return ImmutableAddRow.of(targetId, ImmutableList.of(Triggers.<ItemState>trigger(stateChangedEvent(targetId)).when(ITEM_STATE_CHANGED)));
  }

  public static InitRowGroupItemsCommand initRowGroupItemsCommand(@Nonnull ItemId targetId) {
    return ImmutableInitRowGroupItemsCommand.of(targetId, ImmutableList.of(Triggers.<ItemState>trigger(itemsChangedEvent(onTarget(targetId))).when(GROUP_ITEMS_CHANGED)));
  }

  public static UpdateActivityCommand activityUpdate(ItemId targetId, Expression expression) {
    return ImmutableUpdateActivityCommand.of(targetId, expression, ImmutableList.of(
      Triggers.<ItemState>trigger(stateChangedEvent(targetId)).when(ITEM_ACTIVITY_CHANGED),
      Triggers.<ItemState>trigger(Triggers.activityUpdatedEvent(onTarget(targetId))).when(ITEM_ACTIVITY_CHANGED),
      Triggers.<ItemState>trigger(Triggers.validityUpdatedEvent(onTarget(targetId))).when(ITEM_ACTIVITY_CHANGED),
      Triggers.<ItemState>trigger(Triggers.answeredUpdatedEvent(onTarget(targetId))).when(ITEM_ACTIVITY_CHANGED)
    ));
  }

  public static UpdateRequiredCommand requiredUpdate(ItemId targetId, Expression expression) {
    return ImmutableUpdateRequiredCommand.of(targetId, expression, ImmutableList.of(Triggers.<ItemState>trigger(Triggers.requiredUpdatedEvent(onTarget(targetId))).when(ITEM_REQUIRED_CHANGED)));
  }

  public static UpdateClassNames updateClassNames(ItemId targetId, Expression expression) {
    return ImmutableUpdateClassNames.of(targetId, expression, emptyList());
  }

  public static UpdateLabelCommand labelUpdate(ItemId targetId, Expression expression) {
    return ImmutableUpdateLabelCommand.of(targetId, expression, ImmutableList.of(
      Triggers.<ItemState>trigger(Triggers.labelUpdatedEvent(onTarget(targetId))).when(ITEM_LABEL_CHANGED)
    ));
  }

  public static UpdateDescriptionCommand descriptionUpdate(ItemId targetId, Expression expression) {
    return ImmutableUpdateDescriptionCommand.of(targetId, expression, ImmutableList.of(Triggers.<ItemState>trigger(Triggers.descriptionUpdatedEvent(onTarget(targetId))).when(ITEM_DESCRIPTION_CHANGED)));
  }

  public static UpdateAllowedActionsCommand allowedActionsUpdate(ItemId targetId, Expression expression) {
    return ImmutableUpdateAllowedActionsCommand.of(targetId, expression, emptyList());
  }

  public static UpdateIsInvalidAnswersCommand updateIsInvalidAnswers(ItemId targetId, Expression expression) {
    return ImmutableUpdateIsInvalidAnswersCommand.of(targetId, expression, ImmutableList.of(Triggers.<ItemState>trigger(Triggers.anyInvalidAnswersUpdatedEvent())
      .when(ITEM_INVALID_ANSWERS_CHANGED)));
  }

  public static UpdateAvailableItemsCommand availableItemsUpdate(ItemId targetId, Expression expression) {
    return ImmutableUpdateAvailableItemsCommand.of(targetId, expression, ImmutableList.of(Triggers.<ItemState>trigger(Triggers.availableItemsUpdatedEvent())
      .when(ITEM_STATE_CHANGED)));
  }

  public static UpdateDisabledCommand updateDisabled(ItemId targetId, Expression expression) {
    return ImmutableUpdateDisabledCommand.of(targetId, expression, ImmutableList.of(Triggers.<ItemState>trigger(Triggers.disabledUpdatedEvent(onTarget(targetId)))
      .when(ITEM_STATE_CHANGED)));
  }

  public static ItemUpdateCommand updateGroupItems(ItemId targetId, Expression expression) {
    if (targetId instanceof ItemIdPartial) {
      return ImmutableInitGroupItems.of(targetId, expression, ImmutableList.of(
        // TODO Triggered event do not match correctly on command
        Triggers.trigger(Triggers.groupItemsUpdatedEvent(targetId)).when(ITEM_STATE_CHANGED),
        Triggers.trigger(Triggers.rowGroupItemsInitEvent(targetId)).when(ITEM_STATE_CHANGED)
      ));
    }
    return ImmutableUpdateGroupItems.of(targetId, expression, ImmutableList.of(
      Triggers.trigger(Triggers.groupItemsUpdatedEvent(onTarget(targetId))).when(ITEM_STATE_CHANGED)
    ));
  }

  public static ValidationDisabledUpdateCommand validationDisabledUpdate(ErrorId errorId, Expression expression) {
    return ImmutableValidationDisabledUpdateCommand.of(errorId, expression, emptyList());
  }

  public static UpdateValidationCommand updateValidationCommand(ErrorId errorId, Expression expression) {
    return ImmutableUpdateValidationCommand.of(errorId, expression, ImmutableList.of(
      Triggers.<ErrorState>trigger(Triggers.validityUpdatedEvent(onTarget(errorId.getItemId()))).when(ERROR_ACTIVITY_CHANGED),
      Triggers.<ErrorState>trigger(Triggers.errorActivityUpdatedEvent(errorId)).when(ERROR_ACTIVITY_CHANGED)
    ));
  }

  public static ErrorLabelUpdateCommand errorLabelUpdateCommand(ErrorId errorId, Expression expression) {
    return ImmutableErrorLabelUpdateCommand.of(errorId, expression, Collections.emptyList());
  }

  public static VariableUpdateCommand variableUpdateCommand(ItemId targetId, Expression expression) {
    return ImmutableVariableUpdateCommand.of(targetId, expression, ImmutableList.of(
      Triggers.<ItemState>trigger(stateChangedEvent(targetId)).when(ITEM_STATE_CHANGED)
    ));
  }

  public static UpdateValueSetCommand updateValueSet(ValueSetId targetId, List<Value<ValueSet.Entry>> entries) {
    return ImmutableUpdateValueSetCommand.of(targetId, entries, emptyList());
  }

  public static SessionUpdateCommand createRowGroupFromPrototypeCommand(ItemId rowProtoTypeId) {
    return ImmutableCreateRowGroupFromPrototypeCommand.of(rowProtoTypeId,
      ImmutableList.of(
        trigger(ImmutableRowItemsAddedEventsProvider.of(rowProtoTypeId))
          .when(ItemStatesPredicates.ITEM_STATES_CHANGED),
        trigger(ImmutableRowItemsRemovedEventsProvider.of(rowProtoTypeId))
          .when(ItemStatesPredicates.ITEM_STATES_CHANGED)
      )
    );
  }

  public static SessionUpdateCommand createRowGroupItemsFromPrototypeCommand(ItemId rowProtoTypeId, List<ItemId> itemPrototypeIds) {
    return ImmutableCreateRowGroupItemsFromPrototypeCommand.of(rowProtoTypeId, rowProtoTypeId, ImmutableList.of(
      trigger(ImmutableProtoTypeItemsAddedEventsProvider.of(itemPrototypeIds)).when(ItemStatesPredicates.ITEM_STATES_CHANGED)
    ));
  }
}
