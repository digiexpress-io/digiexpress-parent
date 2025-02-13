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
package io.dialob.executor;

import io.dialob.api.proto.Action;
import io.dialob.compiler.DebugUtil;
import io.dialob.executor.command.Command;
import io.dialob.executor.command.UpdateCommand;
import io.dialob.executor.command.event.Event;
import io.dialob.executor.model.DialobSession;
import io.dialob.executor.model.IdUtils;
import io.dialob.executor.model.ItemId;
import io.dialob.program.DialobProgram;
import io.dialob.program.DialobSessionEvalContext;
import io.dialob.program.DialobSessionEvalContextFactory;
import io.dialob.program.EvalContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import static io.dialob.executor.command.CommandFactory.*;
import static java.util.Objects.requireNonNull;

@Slf4j
public class ActiveDialobSessionUpdater implements DialobSessionUpdater {

  private final DialobSessionEvalContextFactory sessionContextFactory;

  private final DialobSession dialobSession;

  private final DialobProgram dialobProgram;

  private final Set<Command<?>> updated = new HashSet<>();

  protected final List<Command<?>> evalQueue = new LinkedList<>();

  public ActiveDialobSessionUpdater(@Nonnull DialobSessionEvalContextFactory sessionContextFactory,
                                    @Nonnull DialobProgram dialobProgram,
                                    @Nonnull DialobSession dialobSession) {
    this.sessionContextFactory = requireNonNull(sessionContextFactory);
    this.dialobProgram = requireNonNull(dialobProgram);
    this.dialobSession = requireNonNull(dialobSession);
  }

  @Override
  public Consumer<EvalContext.UpdatedItemsVisitor> dispatchActions(@Nonnull Iterable<Action> actions) {
    return dispatchActions(actions, false);
  }

  @Override
  public Consumer<EvalContext.UpdatedItemsVisitor> dispatchActions(@Nonnull Iterable<Action> actions, boolean activating) {
    DialobSessionEvalContext evalContext = sessionContextFactory.createDialobSessionEvalContext(dialobSession, this::queueUpdate, activating);
    applyUpdates(actions);
    while(!evalQueue.isEmpty()) {
      ListIterator<Command<?>> iterator = evalQueue.listIterator();
      Command<?> command = iterator.next();
      iterator.remove();
      matchPartialCommands(command).forEach(action -> {
        updated.add(action);
        evalContext.applyAction(action);
      });
    }
    updated.clear();
    log.debug("Update completed.");
    return evalContext::accept;
  }

  protected void applyUpdates(@Nonnull Iterable<Action> actions) {
    actions.forEach(action -> {
      ItemId itemId = IdUtils.toIdNullable(action.getId());
      switch(action.getType()) {
        case ANSWER:
          queueCommand(setAnswer(requireNonNull(itemId), action.getAnswer()));
          break;
        case SET_VALUE:
          queueCommand(setVariableValue(requireNonNull(itemId), action.getValue()));
          break;
        case SET_FAILED:
          queueCommand(setVariableFailed(requireNonNull(itemId)));
          break;
        case NEXT:
          queueCommand(nextPage());
          break;
        case PREVIOUS:
          queueCommand(prevPage());
          break;
        case GOTO:
          queueCommand(gotoPage(requireNonNull(itemId)));
          break;
        case COMPLETE:
          queueCommand(complete());
          break;
        case ADD_ROW:
          queueCommand(addRow(requireNonNull(itemId)));
          break;
        case DELETE_ROW:
          queueCommand(deleteRow(requireNonNull(itemId)));
          break;
        case SET_LOCALE:
          if (action.getValue() instanceof String) {
            queueCommand(setLocale((String) action.getValue()));
          }
          break;
        default:
          log.debug("Action \"{}\" ignored.", action);
      }
    });
  }

  private void queueUpdate(@Nonnull Event event) {
    if (log.isDebugEnabled()) {
      log.debug(" -> event({})", event);
    }
    dialobProgram
      .findDependencies(event)
      .forEach(this::queueCommand);
    if (log.isDebugEnabled()) {
      log.debug("  = {}", StringUtils.join(evalQueue.stream().map(DebugUtil::commandToString).toArray(), " ,"));
    }
  }

  private Stream<Command<?>> matchPartialCommands(Command<?> command) {
    if (command instanceof UpdateCommand) {
      UpdateCommand updateCommand = (UpdateCommand) command;
      final ItemId targetId = updateCommand.getTargetId();
      if (targetId.isPartial()) {
        return dialobSession.findMatchingItemIds(targetId).map((Function<ItemId, Command<?>>) updateCommand::withTargetId);
      }
    }
    return Stream.of(command);
  }

  protected void queueCommand(@Nonnull final Command<?> updateCommand) {
    Collection<Command<?>> mustBeBefore = dialobProgram.getCommandsToCommands(updateCommand);
    if (mustBeBefore.isEmpty()) {
      evalQueue.add(updateCommand);
      return;
    }
    // push command to be queue to correct location in queue.
    ListIterator<Command<?>> i = evalQueue.listIterator();
    while (i.hasNext()) {
      Command<?> command = i.next();
      if (updateCommand.equals(command)) {
        if (log.isDebugEnabled()) {
          log.debug("  - skip {} (on queue already)", DebugUtil.commandToString(updateCommand));
        }
        return;
      }
      if (mustBeBefore.contains(command)) {
        i.previous();
        break;
      }
    }
    i.add(updateCommand);
    if (log.isDebugEnabled()) {
      log.debug("  + queued {}", DebugUtil.commandToString(updateCommand));
    }
    if (log.isWarnEnabled() && updated.contains(updateCommand)) {
      log.warn("Target {} already executed. Cyclic dependency?", DebugUtil.commandToString(updateCommand));
    }
  }
}
