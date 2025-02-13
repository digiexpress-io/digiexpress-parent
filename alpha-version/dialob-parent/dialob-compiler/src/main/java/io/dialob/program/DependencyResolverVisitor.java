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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.Streams;
import io.dialob.api.form.FormValidationError;
import io.dialob.api.form.ImmutableFormValidationError;
import io.dialob.compiler.DebugUtil;
import io.dialob.executor.command.Command;
import io.dialob.executor.command.EventMatcher;
import io.dialob.executor.command.Trigger;
import io.dialob.executor.command.UpdateCommand;
import io.dialob.executor.model.*;
import io.dialob.program.expr.arith.RowItemsExpression;
import io.dialob.program.model.*;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Objects.requireNonNull;

@Slf4j
public class DependencyResolverVisitor implements ProgramVisitor {

  private final Map<EventMatcher, List<Command<?>>> inputUpdates;

  private final Map<ItemId, List<Command<?>>> itemCommands;

  private final UpdateCommandFactory updateCommandFactory;

  private Map<Command<?>, Set<Command<?>>> commandsToCommands;

  public DependencyResolverVisitor() {
    this.inputUpdates = Maps.newHashMap();
    this.itemCommands = Maps.newHashMap();
    this.updateCommandFactory = new UpdateCommandFactory();
  }

  @Override
  public Optional<ItemVisitor> visitItems() {
    return Optional.of(new AbstractItemVisitor() {

      @Override
      public void visitGroup(@Nonnull Group group) {
        visitDisplayItem(group);
        ItemId groupId;
        if (isRowgroup(group.getType()) || isRow(group.getType())) {
          groupId = group.getId();
          if (group.isPrototype()) {
            final Expression itemsExpression = group.getItemsExpression();
            if (itemsExpression instanceof RowItemsExpression) {
              RowItemsExpression rowItemsExpression = (RowItemsExpression) itemsExpression;
              updateCommandFactory.createRowGroupItemsFromPrototype(groupId, rowItemsExpression.getItemIds());

            }
            updateCommandFactory.createUpdateGroupItems(groupId, itemsExpression);
            group.getCanRemoveRowWhenExpression().ifPresent(expression -> updateCommandFactory.createUpdateRowCanBeRemovedCommand(groupId, expression));
          } else {
            updateCommandFactory.initRowGroupItems(groupId);
            group.getCanAddRowWhenExpression().ifPresent(expression -> updateCommandFactory.createUpdateRowsCanBeAddedCommand(groupId, expression));
          }
        } else {
          groupId = group.getId();
          updateCommandFactory.createUpdateGroupItems(groupId, group.getItemsExpression());
        }
        group.getAvailableItemsExpression().ifPresent(expression -> updateCommandFactory.createUpdateAvailableItems(groupId, expression));
        group.getIsInvalidAnswersExpression().ifPresent(expression -> updateCommandFactory.createUpdateIsInvalidAnswersCommand(groupId, expression));
        group.getAllowedActionsExpression().ifPresent(expression -> updateCommandFactory.createUpdateAllowedActions(groupId, expression));
      }

      @Override
      public void visitDisplayItem(@Nonnull DisplayItem displayItem) {
        final ItemId itemId = displayItem.getId();
        displayItem.getActiveExpression().ifPresent(expression -> updateCommandFactory.createUpdateActivity(itemId, expression));
        displayItem.getRequiredExpression().ifPresent(expression -> updateCommandFactory.createUpdateRequired(itemId, expression));
        displayItem.getDisabledExpression().ifPresent(expression -> updateCommandFactory.createUpdateDisabled(itemId, expression));
        displayItem.getLabelExpression().ifPresent(expression -> updateCommandFactory.createUpdateLabel(itemId, expression));
        displayItem.getDescriptionExpression().ifPresent(expression -> updateCommandFactory.createUpdateDescription(itemId, expression));
        displayItem.getClassName().ifPresent(expression -> updateCommandFactory.createUpdateClass(itemId, expression));
        if (displayItem.isPrototype() && isRow(displayItem.getType())) {
          updateCommandFactory.createRowGroupFromPrototype(itemId);
        }
      }

      @Override
      public void visitVariableItem(@Nonnull VariableItem variableItem) {
        updateCommandFactory.createUpdateVariable(variableItem.getId(), variableItem.getValueExpression());
      }

    });
  }

  private boolean isRowgroup(@Nonnull String type) {
    return "rowgroup".equals(type);
  }

  private boolean isRow(@Nonnull String type) {
    return "row".equals(type);
  }

  @Override
  public Optional<ErrorVisitor> visitErrors() {
    return Optional.of(error -> {
      final ErrorId targetId = ImmutableErrorId.of(error.getItemId(), error.getCode());
      updateCommandFactory.createUpdateValidationCommand(targetId, error.getValidationExpression());
      error.getDisabledExpression().ifPresent(disabledExpression -> updateCommandFactory.createUpdateValidationDisabled(targetId, disabledExpression));
      if (error.getLabel() != null) {
        updateCommandFactory.createErrorLabelUpdateCommand(targetId, error.getLabel());
      }
    });
  }

  @Override
  public Optional<ValueSetVisitor> visitValueSets() {
    return Optional.of(valueSet -> updateCommandFactory.createUpdateValueSetCommand(ImmutableValueSetId.of(valueSet.getId()), valueSet.getEntries()));
  }

  @Override
  public void end() {
    // collect direct command dependencies
    updateCommandFactory.getAllCommands().stream()
      .filter(command -> command instanceof UpdateCommand)
      .map(command -> (UpdateCommand<?,?>) command)
      .forEach(updateCommand ->
        itemCommands.computeIfAbsent(updateCommand.getTargetId(),
        targetId -> newArrayList()).add(updateCommand));
    updateCommandFactory.getAllCommands().stream().forEach(updateCommand -> updateCommand.getEventMatchers().forEach(
      eventMatcher -> inputUpdates.computeIfAbsent(requireNonNull(eventMatcher),
        key -> newArrayList()).add(updateCommand)));


    commandsToCommands = updateCommandFactory.getAllCommands().stream()
      .collect(Collectors.toMap(
        command -> command,
        command -> findTriggers(command)
          .map(Trigger::getAllEvents)
          .flatMap(List::stream)
          .flatMap(event -> inputUpdates.entrySet().stream()
            .filter(entry -> entry.getKey().matches(event))
            .flatMap(entry -> entry.getValue().stream()))
          .collect(Collectors.toSet())
      ));

    loopScan();

    // Scan deeper command dependencies

    commandsToCommands.entrySet().stream().forEach((Map.Entry<Command<?>, Set<Command<?>>> entry) -> {
      Set<Command<?>> prevSet;
      Set<Command<?>> set;
      boolean same;
      do {
        prevSet = entry.getValue();
        set = prevSet.stream()
          .flatMap(updateCommand -> Streams.concat(Stream.of(updateCommand), commandsToCommands.get(updateCommand).stream()))
          .collect(Collectors.toSet());
        entry.setValue(set);
        if (set.contains(entry.getKey())) {
          // TODO Add custom exception
          throw new RuntimeException("Command loop! " + entry.getKey());
        }
        same = prevSet.equals(set);
      } while (!same);
    });

    if (log.isDebugEnabled()) {
      log.debug(">>> Command dependencies >>>");
      commandsToCommands.forEach((key, value) -> {
        log.debug("Command : {}", DebugUtil.commandToString(key));
        value.forEach(command -> log.debug("  <= {}", DebugUtil.commandToString(command)));
      });
      log.debug("<<< Command dependencies <<<");
    }
    // Uncomment this to get dot dump of form.
    // DebugUtil.dumpDotFile(getItemCommands());
  }

  private <T> Stream<Trigger<T>> findTriggers(Command<T> command) {
    return command.getTriggers().stream();
  }

  private void loopScan() {
    for (final Map.Entry<Command<?>, Set<Command<?>>> entry : commandsToCommands.entrySet()) {
      final Command<?> key = entry.getKey();
      final ArrayList<Command<?>> path = new ArrayList<>();
      path.add(key);
      loopScan(path, key);
    }
  }

  private void loopScan(List<Command<?>> path, Command<?> next) {
    for (Command<?> command : commandsToCommands.get(next)) {
      final boolean contains = path.contains(command);
      path.add(command);
      if (contains) {
        ItemId itemId = IdUtils.QUESTIONNAIRE_ID;
        if (command instanceof UpdateCommand) {
          itemId = ((UpdateCommand) command).getTargetId();
        }
        throw new DependencyLoopException("dependency loop", ImmutableList.of(ImmutableFormValidationError.builder()
          .type(FormValidationError.Type.GENERAL)
          .level(FormValidationError.Level.ERROR)
          .message("dependency loop")
          .itemId(IdUtils.toString(itemId))
          .build()));
      }
      loopScan(path, command);
      path.remove(command);
    }
  }

  @Nonnull
  public Map<EventMatcher, List<Command<?>>> getInputUpdates() {
    return inputUpdates;
  }

  @Nonnull
  public Map<ItemId, List<Command<?>>> getItemCommands() {
    return itemCommands;
  }

  @Nonnull
  public Map<Command<?>, Set<Command<?>>> getCommandsToCommands() {
    return commandsToCommands == null ?
      Collections.emptyMap() :
      Collections.unmodifiableMap(commandsToCommands);
  }
}
