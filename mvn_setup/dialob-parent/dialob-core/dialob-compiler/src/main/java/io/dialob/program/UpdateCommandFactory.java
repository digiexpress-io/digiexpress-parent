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
package io.dialob.program;

import com.google.common.collect.Sets;

import io.dialob.compiler.DebugUtil;
import io.dialob.executor.command.*;
import io.dialob.executor.model.ErrorId;
import io.dialob.executor.model.ItemId;
import io.dialob.executor.model.ValueSetId;
import io.dialob.program.model.Expression;
import io.dialob.program.model.Value;
import io.dialob.program.model.ValueSet;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nonnull;

import static io.dialob.executor.command.CommandFactory.*;

import java.util.Collections;
import java.util.List;
import java.util.Set;

@Slf4j
class UpdateCommandFactory {

  private final Set<Command<?>> allCommands = Sets.newHashSet();
  private final Set<ErrorId> targetIds = Sets.newHashSet();

  private <C extends Command<?>> C add(C command) {
    assert !allCommands.contains(command);
    allCommands.add(command);
    if(LOGGER.isDebugEnabled()) {
      LOGGER.debug("Command '{}' created", DebugUtil.commandToString(command));
    }
    return command;
  }

  public Set<Command<?>> getAllCommands() {
    return Collections.unmodifiableSet(allCommands);
  }

  public VariableUpdateCommand createUpdateVariable(@Nonnull ItemId id, @Nonnull Expression expression) {
    return add(variableUpdateCommand(id, expression));
  }

  public UpdateClassNames createUpdateClass(@Nonnull ItemId id, @Nonnull Expression expression) {
    return add(updateClassNames(id, expression));
  }

  public UpdateLabelCommand createUpdateLabel(@Nonnull ItemId id, @Nonnull Expression expression) {
    return add(labelUpdate(id, expression));
  }

  public UpdateDescriptionCommand createUpdateDescription(@Nonnull ItemId id, @Nonnull Expression expression) {
    return add(descriptionUpdate(id, expression));
  }

  public UpdateDisabledCommand createUpdateDisabled(@Nonnull ItemId id, @Nonnull Expression expression) {
    return add(updateDisabled(id, expression));
  }

  public UpdateActivityCommand createUpdateActivity(@Nonnull ItemId id, @Nonnull Expression expression) {
    return add(activityUpdate(id, expression));
  }

  public UpdateRowsCanBeAddedCommand createUpdateRowsCanBeAddedCommand(@Nonnull ItemId id, @Nonnull Expression expression) {
    return add(rowsCanBeAddedUpdate(id, expression));
  }

  public UpdateRowCanBeRemovedCommand createUpdateRowCanBeRemovedCommand(@Nonnull ItemId id, @Nonnull Expression expression) {
    return add(rowCanBeRemovedUpdate(id, expression));
  }

  public UpdateRequiredCommand createUpdateRequired(@Nonnull ItemId id, @Nonnull Expression expression) {
    return add(requiredUpdate(id, expression));
  }

  public UpdateAllowedActionsCommand createUpdateAllowedActions(@Nonnull ItemId id, @Nonnull Expression expression) {
    return add(allowedActionsUpdate(id, expression));
  }

  public UpdateIsInvalidAnswersCommand createUpdateIsInvalidAnswersCommand(@Nonnull ItemId id, @Nonnull Expression expression) {
    return add(updateIsInvalidAnswers(id, expression));
  }

  public UpdateAvailableItemsCommand createUpdateAvailableItems(@Nonnull ItemId id, @Nonnull Expression expression) {
    return add(availableItemsUpdate(id, expression));
  }

  public ItemUpdateCommand createUpdateGroupItems(@Nonnull ItemId groupId, @Nonnull Expression expression) {
    return add(updateGroupItems(groupId, expression));
  }

  public ValidationDisabledUpdateCommand createUpdateValidationDisabled(@Nonnull ErrorId targetId, @Nonnull Expression expression) {
    return add(validationDisabledUpdate(targetId, expression));
  }

  public UpdateValidationCommand createUpdateValidationCommand(@Nonnull ErrorId targetId, @Nonnull Expression expression) {
    assert !targetIds.contains(targetId);
    targetIds.add(targetId);
    return add(updateValidationCommand(targetId, expression));
  }

  public ErrorLabelUpdateCommand createErrorLabelUpdateCommand(@Nonnull ErrorId targetId, @Nonnull Expression expression) {
    return add(errorLabelUpdateCommand(targetId, expression));
  }

  public SessionUpdateCommand createRowGroupFromPrototype(ItemId rowGroupPrototypeId) {
    return add(createRowGroupFromPrototypeCommand(rowGroupPrototypeId));
  }

  public SessionUpdateCommand createRowGroupItemsFromPrototype(ItemId rowGroupPrototypeId, List<ItemId> itemIds) {
    return add(createRowGroupItemsFromPrototypeCommand(rowGroupPrototypeId, itemIds));
  }

  public InitRowGroupItemsCommand initRowGroupItems(ItemId groupId) {
    return add(initRowGroupItemsCommand(groupId));
  }

  public UpdateValueSetCommand createUpdateValueSetCommand(ValueSetId valueSetId, List<Value<ValueSet.Entry>> entries) {
    return add(updateValueSet(valueSetId, entries));
  }

}
