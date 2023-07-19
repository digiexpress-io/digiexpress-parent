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

import com.google.common.collect.Lists;
import io.dialob.api.proto.Action;
import io.dialob.executor.ActiveDialobSessionUpdater;
import io.dialob.executor.CreateDialobSessionProgramVisitor;
import io.dialob.executor.command.*;
import io.dialob.executor.command.event.*;
import io.dialob.executor.model.DialobSession;
import io.dialob.executor.model.ErrorId;
import io.dialob.executor.model.ItemId;
import io.dialob.program.model.Item;
import io.dialob.program.model.Program;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;

@EqualsAndHashCode()
@Slf4j
public class DialobProgram implements Serializable {

  private static final long serialVersionUID = 2922819825920407874L;

  private final Program program;

  private final Map<EventMatcher,List<Command<?>>> inputUpdates;

  private final Map<ItemId,List<Command<?>>> itemCommands;

  private final Map<Command<?>, Set<Command<?>>> commandsToCommands;

  private DialobProgram(@Nonnull Program program,
                        @Nonnull Map<EventMatcher, List<Command<?>>> inputUpdates,
                        @Nonnull Map<ItemId, List<Command<?>>> itemCommands,
                        @Nonnull Map<Command<?>, Set<Command<?>>> commandsToCommands) {
    this.program = Objects.requireNonNull(program);
    this.inputUpdates = Objects.requireNonNull(inputUpdates);
    this.itemCommands = Objects.requireNonNull(itemCommands);
    this.commandsToCommands = Objects.requireNonNull(commandsToCommands);
  }

  @Nonnull
  public static DialobProgram createDialobProgram(@Nonnull Program program) {
    DependencyResolverVisitor visitor = new DependencyResolverVisitor();
    program.accept(visitor);
    return new DialobProgram(program,
      visitor.getInputUpdates(),
      visitor.getItemCommands(),
      visitor.getCommandsToCommands());
  }

  public Stream<Command<?>> findDependencies(@Nonnull Event event) {
    return inputUpdates
      .entrySet()
      .stream()
      .filter(entry -> entry.getKey().matches(event))
      .flatMap(entry -> entry.getValue().stream())
      .filter(Objects::nonNull)
      .flatMap(command -> mapTo(event, command));
  }

  private <T, C extends Command<T>> Stream<C> mapTo(Event event, C command) {
    if (command instanceof ErrorUpdateCommand) {
      final ErrorUpdateCommand updateCommand = (ErrorUpdateCommand) command;
      // TODO remove instanceof checks
      final ErrorId errorId = updateCommand.getTargetId();
      if (event instanceof ItemAddedEvent) {
        return Stream.of((C) updateCommand.withTargetId(errorId.withItemId(((ItemAddedEvent)event).getAddItemId())));
      }
      if (event instanceof ItemRemovedEvent) {
        return Stream.of((C) updateCommand.withTargetId(errorId.withItemId(((ItemRemovedEvent)event).getRemoveItemId())));
      }
      if (event instanceof RowGroupItemsInitEvent) {
        return Stream.of((C) updateCommand.withTargetId(errorId.withItemId(((RowGroupItemsInitEvent)event).getGroupId())));
      }
      if (errorId.isPartial() && event instanceof TargetEvent) {
        return Stream.of((C) updateCommand.withTargetId(errorId.withItemId(errorId.getItemId().withParent(((TargetEvent)event).getTargetId().getParent()))));
      }
    } else if (command instanceof UpdateCommand) {
      final UpdateCommand updateCommand = (UpdateCommand) command;
      if (event instanceof ItemAddedEvent) {
        return Stream.of((C) updateCommand.withTargetId(((ItemAddedEvent)event).getAddItemId()));
      }
      if (event instanceof ItemRemovedEvent) {
        return Stream.of((C) updateCommand.withTargetId(((ItemRemovedEvent)event).getRemoveItemId()));
      }
      if (event instanceof RowGroupItemsInitEvent) {
        return Stream.of((C) updateCommand.withTargetId(((RowGroupItemsInitEvent)event).getGroupId()));
      }
    }
    return Stream.of(command);
  }

  public Set<Event> allUpdates() {
    return this.inputUpdates.values()
      .stream()
      .flatMap(List::stream)
      .map(Command::getTriggers)
      .flatMap(List::stream)
      .map(Trigger::getAllEvents)
      .flatMap(List::stream)
      .collect(toSet());
  }


  /**
   * @deprecated Only used in unit tests
   */
  @Deprecated
  public DialobSession createSession(@Nonnull DialobSessionEvalContextFactory sessionContextFactory, String tenantId, final String sessionId, final String language, String activePage) {
    return this.createSession(sessionContextFactory, tenantId, sessionId, language, activePage, (itemId, item) -> Optional.empty(), valueSetId -> Collections.emptyList(), null, null, null);
  }

  public DialobSession createSession(@Nonnull DialobSessionEvalContextFactory sessionContextFactory,
                                     final String tenantId,
                                     final String sessionId,
                                     final String language,
                                     final String activePage,
                                     @Nonnull CreateDialobSessionProgramVisitor.InitialValueResolver initialValueResolver,
                                     CreateDialobSessionProgramVisitor.ProvidedValueSetEntriesResolver findProvidedValueSetEntries,
                                     Date completed,
                                     Date opened,
                                     Date lastAnswer)
  {
    final CreateDialobSessionProgramVisitor createDialobSessionProgramVisitor = new CreateDialobSessionProgramVisitor(tenantId, sessionId, language, activePage, initialValueResolver, findProvidedValueSetEntries, this.itemCommands, null, null, null);
    program.accept(createDialobSessionProgramVisitor);
    DialobSession dialobSession = createDialobSessionProgramVisitor.getDialobSession();
    new ActiveDialobSessionUpdater(sessionContextFactory, this, dialobSession) {
      @Override
      protected void applyUpdates(@Nonnull Iterable<Action> actions) {
        createDialobSessionProgramVisitor.getUpdates().forEach(this::queueCommand);
        // find first whenActiveUpdated page, if activePage is unset
        if (activePage == null) {
          evalQueue.add(CommandFactory.nextPage());
        }
      }
    }.dispatchActions(Collections.emptyList());

    return dialobSession;
  }

  public Program getProgram() {
    return program;
  }

  @Override
  public String toString() {
    return program.toString();
  }

  public Optional<Item> getItem(ItemId id) {
    return program.getItem(id);
  }

  public Set<Command<?>> getCommandsToCommands(Command<?> updateCommand) {
    return commandsToCommands.getOrDefault(updateCommand, Collections.emptySet());
  }

  private <T> List<T> merge(List<T> o, List<T> o1) {
    final ArrayList<T> list = Lists.newArrayList(o);
    list.addAll(o1);
    return list;
  }

}
