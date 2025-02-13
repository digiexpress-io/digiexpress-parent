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

import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import com.google.protobuf.CodedInputStream;
import com.google.protobuf.CodedOutputStream;
import io.dialob.compiler.DebugUtil;
import io.dialob.executor.command.*;
import io.dialob.program.EvalContext;
import io.dialob.spi.Constants;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.Serializable;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import static io.dialob.compiler.Utils.*;

@EqualsAndHashCode
@ToString
@Slf4j
public class DialobSession implements ItemStates, Serializable {

  private static final long serialVersionUID = 1180110179877247767L;

  public static final ImmutableItemRef QUESTIONNAIRE_REF = (ImmutableItemRef) IdUtils.toId(Constants.QUESTIONNAIRE);

  private final String tenantId;

  private final String id;

  private int asyncUpdateCount;

  private String revision;

  @Nonnull
  private Date lastUpdate = new Date();

  private Date completed;

  private Date opened;

  private String language;

  private Map<ItemId,ItemState> itemStates = new HashMap<>();

  // TODO move this to DialobProgram
  private Map<ItemId,ItemState> itemPrototypes = new HashMap<>();

  private Map<ValueSetId,ValueSetState> valueSetStates = new HashMap<>();

  private Map<ErrorId,ErrorState> errorStates = new HashMap<>();

  // TODO move this to DialobProgram
  private Map<ErrorId,ErrorState> errorPrototypes = new HashMap<>();

  public void writeTo(CodedOutputStream output) throws IOException {
    writeNullableString(output, tenantId);
    writeNullableString(output, id);
    output.writeStringNoTag(revision);
    output.writeStringNoTag(language);
    output.writeInt64NoTag(lastUpdate.getTime());
    writeNullableDate(output, completed);
    writeNullableDate(output, opened);
    output.writeInt32NoTag(asyncUpdateCount);

    output.writeInt32NoTag(itemStates.size());
    for (ItemState itemState : itemStates.values()) {
      itemState.writeTo(output);
    }

    output.writeInt32NoTag(itemPrototypes.size());
    for (ItemState itemState : itemPrototypes.values()) {
      itemState.writeTo(output);
    }

    output.writeInt32NoTag(valueSetStates.size());
    for (ValueSetState valueSetState : valueSetStates.values()) {
      valueSetState.writeTo(output);
    }

    output.writeInt32NoTag(errorStates.size());
    for (ErrorState errorState : errorStates.values()) {
      errorState.writeTo(output);
    }

    output.writeInt32NoTag(errorPrototypes.size());
    for (ErrorState state : errorPrototypes.values()) {
      state.writeTo(output);
    }

  }

  public static DialobSession readFrom(CodedInputStream input) throws IOException {
    String tenantId = readNullableString(input);
    String id = readNullableString(input);
    DialobSession session = new DialobSession(tenantId, id);
    session.revision = input.readString();
    session.language = input.readString();
    session.lastUpdate = new Date(input.readInt64());
    session.completed = readNullableDate(input);
    session.opened = readNullableDate(input);
    session.asyncUpdateCount = input.readInt32();

    int count = input.readInt32();
    for (int i = 0; i < count; ++i) {
      final ItemState state = ItemState.readFrom(input);
      session.itemStates.put(state.getId(), state);
    }
    count = input.readInt32();
    for (int i = 0; i < count; ++i) {
      final ItemState state = ItemState.readFrom(input);
      session.itemPrototypes.put(state.getId(), state);
    }
    count = input.readInt32();
    for (int i = 0; i < count; ++i) {
      final ValueSetState state = ValueSetState.readFrom(input);
      session.valueSetStates.put(state.getId(), state);
    }
    count = input.readInt32();
    for (int i = 0; i < count; ++i) {
      final ErrorState state = ErrorState.readFrom(input);
      session.errorStates.put(state.getId(), state);
    }
    count = input.readInt32();
    for (int i = 0; i < count; ++i) {
      final ErrorState state = ErrorState.readFrom(input);
      session.errorPrototypes.put(state.getId(), state);
    }

    return session;
  }


  private DialobSession(String tenantId, @Nullable final String id) {
    this.tenantId = tenantId;
    this.id = id;
  }

  public DialobSession(
    String tenantId,
    String id,
    String revision,
    String language,
    List<ItemState> items,
    List<ItemState> prototypes,
    List<ValueSetState> valueSets,
    List<ErrorState> errors,
    List<ErrorState> errorPrototypes,
    Date completed,
    Date opened,
    Date lastAnswer)
  {
    this(tenantId, id);
    this.revision = revision;
    this.language = language;
    if (completed != null) {
      this.completed = new Date(completed.getTime());
    }
    if (opened != null) {
      this.opened = new Date(opened.getTime());
    }
    if (items != null) {
      items.forEach(item -> itemStates.put(item.getId(), item));
    }
    if (valueSets != null) {
      valueSets.forEach(item -> this.valueSetStates.put(item.getId(), item));
    }
    if (errors != null) {
      errors.forEach(item -> this.errorStates.put(ImmutableErrorId.of(item.getItemId(),item.getCode()), item));
    }
    if (errorPrototypes != null) {
      errorPrototypes.forEach(item -> this.errorPrototypes.put(ImmutableErrorId.of(item.getItemId(),item.getCode()), item));
    }
    if (prototypes != null) {
      prototypes.forEach(prototype -> this.itemPrototypes.put(prototype.getId(), prototype));
    }
    updated();
    if (lastAnswer != null) {
      // updated() updates lastUpdate. To retain persistent value
      this.lastUpdate = new Date(lastAnswer.getTime());
    }
  }

  private DialobSession(String id, DialobSession dialobSession) {
    this.id = id;
    this.tenantId = dialobSession.tenantId;
    this.revision = dialobSession.revision;
    this.lastUpdate = new Date(dialobSession.lastUpdate.getTime());
    this.opened = dialobSession.opened != null ? new Date(dialobSession.opened.getTime()) : null;
    this.completed = dialobSession.completed != null ? new Date(dialobSession.completed.getTime()) : null;
    this.language = dialobSession.language;
    this.itemStates = new HashMap<>(dialobSession.itemStates);
    this.itemPrototypes = new HashMap<>(dialobSession.itemPrototypes);
    this.valueSetStates = new HashMap<>(dialobSession.valueSetStates);
    this.errorStates = new HashMap<>(dialobSession.errorStates);
    this.errorPrototypes = new HashMap<>(dialobSession.errorPrototypes);
  }

  public DialobSession withId(String id) {
    if (Objects.equals(this.id, id)) {
      return this;
    }
    return new DialobSession(id, this);
  }

  public String getId() {
    return id;
  }

  public String getTenantId() {
    if (tenantId == null) {
      return Constants.DEFAULT_TENANT;
    }
    return tenantId;
  }

  @Nonnull
  public ItemState getRootItem() {
    return getItemState(QUESTIONNAIRE_REF)
      .orElseThrow(() -> new IllegalStateException("Could not find questionnaire from " + getId()));
  }

  public Optional<ItemState> getItemState(@Nonnull ItemId id) {
    return Optional.ofNullable(itemStates.get(id));
  }

  public void accept(DialobSessionVisitor visitor) {
    visitor.start();

    // --
    visitor.visitItemStates().ifPresent(itemVisitor -> {
      itemStates.values().forEach(itemVisitor::visitItemState);
      itemVisitor.end();
    });

    // --
    visitor.visitValueSetStates().ifPresent(valueSetVisitor -> {
      valueSetStates.values().forEach(valueSetVisitor::visitValueSetState);
      valueSetVisitor.end();

    });

    // --
    visitor.visitErrorStates().ifPresent(errorVisitor -> {
      getErrorStates().values().forEach(errorVisitor::visitErrorState);
      errorVisitor.end();
    });

    visitor.end();
  }

  /**
   *
   * @param evalContext execution context
   * @param command object to execute within context
   */
  public void applyUpdate(@Nonnull EvalContext evalContext, @Nonnull Command<?> command) {
    if (log.isDebugEnabled()) {
      log.debug("applyUpdate({})", DebugUtil.commandToString(command));
    }

    if (command instanceof ItemUpdateCommand) {
      final ItemUpdateCommand itemUpdateCommand = (ItemUpdateCommand) command;
      ItemId itemId = itemUpdateCommand.getTargetId();
      // TODO scope?
      EvalContext context = createScopedEvalContext(evalContext, itemId);

      applyItemUpdateCommand(context, itemUpdateCommand);
      updated();
    } else if (command instanceof ErrorUpdateCommand) {
      final ErrorUpdateCommand errorUpdateCommand = (ErrorUpdateCommand) command;
      EvalContext context = createScopedEvalContext(evalContext, errorUpdateCommand.getTargetId().getItemId());
      applyErrorUpdateCommand(context, errorUpdateCommand);
      updated();
    } else if (command instanceof UpdateValueSetCommand) {
      final UpdateValueSetCommand valueSetCommand = (UpdateValueSetCommand) command;
      applyUpdateValueSetCommand(evalContext, valueSetCommand);
      updated();
    } else if (command instanceof SessionUpdateCommand) {
      applySessionUpdateCommand(evalContext, (SessionUpdateCommand) command);
      updated();
    } else {
      log.warn("Do not know how to apply command: {}", command);
    }
  }

  public EvalContext createScopedEvalContext(@Nonnull EvalContext evalContext, ItemId itemId) {
    return itemId instanceof ItemIndex ?
      createScope(evalContext, itemId) :
      itemId.getParent().map(parentId -> {
        if (parentId instanceof ItemIndex) {
          return createScope(evalContext, parentId);
        }
        return evalContext;
      }).orElse(evalContext);
  }

  public EvalContext createScope(@Nonnull EvalContext evalContext, ItemId itemId) {
    var scopeItems = new ArrayList<>(evalContext
      .getItemState(itemId)
      .map(ItemState::getItems).orElseGet(Collections::emptyList));
    scopeItems.add(itemId);
    return evalContext.withScope(ImmutableScope.of(itemId, scopeItems));
  }

  private void applySessionUpdateCommand(EvalContext evalContext, SessionUpdateCommand command) {
    final ItemStates newStates = command.update(evalContext, this);
    command.getTriggers().stream()
      .flatMap(trigger -> trigger.apply(this, newStates))
      .forEach(event -> evalContext.getEventsConsumer().accept(event));

    MapDifference<ValueSetId,ValueSetState> valueSetDiffs = Maps.difference(newStates.getValueSetStates(), this.valueSetStates);
    MapDifference<ErrorId,ErrorState> errorDiffs = Maps.difference(newStates.getErrorStates(), this.errorStates);
    MapDifference<ItemId,ItemState> itemStatesDiffs = Maps.difference(newStates.getItemStates(), this.itemStates);

    // Removed
    itemStatesDiffs.entriesOnlyOnRight().forEach((itemId, itemState) -> {
      evalContext.registerUpdate(null, itemState);
      itemStates.remove(itemState.getId());
    });
    errorDiffs.entriesOnlyOnRight().forEach(((errorId, errorState) -> {
      evalContext.registerUpdate(null, errorState);
      errorStates.remove(errorId);
    }));
    // New ones
    itemStatesDiffs.entriesOnlyOnLeft().forEach((itemId, itemState) -> {
      evalContext.registerUpdate(itemState, null);
      itemStates.put(itemState.getId(), itemState);
    });
    errorDiffs.entriesOnlyOnLeft().forEach(((errorId, errorState) -> {
      evalContext.registerUpdate(errorState, null);
      errorStates.put(errorState.getId(), errorState);
    }));
    // Updated
    itemStatesDiffs.entriesDiffering().forEach((itemId, itemStateDiff) -> {
      evalContext.registerUpdate(itemStateDiff.leftValue(), itemStateDiff.rightValue());
      itemStates.put(itemStateDiff.leftValue().getId(), itemStateDiff.leftValue());
    });
    errorDiffs.entriesDiffering().forEach(((errorId, errorState) -> {
      evalContext.registerUpdate(errorState.leftValue(), errorState.rightValue());
      errorStates.put(errorState.leftValue().getId(), errorState.leftValue());
    }));
  }

  private void applyUpdateValueSetCommand(EvalContext evalContext, UpdateValueSetCommand updateCommand) {
    // alias 'answer' to error's target item.
    // TODO should be bound to command in more generic way
    valueSetStates.computeIfPresent(updateCommand.getTargetId(), (key,state) -> {
      ValueSetState updatedState = updateCommand.update(evalContext, state);
      updateCommand.getTriggers().stream()
        .flatMap(trigger -> trigger.apply(state, updatedState))
        .forEach(event -> evalContext.getEventsConsumer().accept(event));
      evalContext.registerUpdate(updatedState, state);
      return updatedState;
    });
  }

  private void applyErrorUpdateCommand(EvalContext evalContext, ErrorUpdateCommand updateCommand) {
    // alias 'answer' to error's target item.
    // TODO should be bound to command in more generic way
    errorStates.computeIfPresent(updateCommand.getTargetId(), (key,state) -> {
      ErrorState updatedState = updateCommand.update(evalContext, state);
      updateCommand.getTriggers().stream()
        .flatMap(trigger -> trigger.apply(state, updatedState))
        .forEach(event -> evalContext.getEventsConsumer().accept(event));
      evalContext.registerUpdate(updatedState, state);
      return updatedState;
    });
  }

  private void applyItemUpdateCommand(EvalContext evalContext, ItemUpdateCommand updateCommand) {
    itemStates.computeIfPresent(updateCommand.getTargetId(), (key,state) -> {
//      log.debug("Execute command: {}", updateCommand);
      final ItemState updatedState = updateCommand.update(evalContext, state);
      updateCommand.getTriggers().stream()
        .flatMap(trigger -> trigger.apply(state, updatedState))
        .forEach(event -> evalContext.getEventsConsumer().accept(event));

      if (state.isDisplayItem()) {
        // If update command is SetAnswer, skip update feedback to ui.
        if (!(updateCommand instanceof SetAnswer)) {
          evalContext.registerUpdate(updatedState, state);
        }
      }
      return updatedState;
    });
  }

  protected void updated() {
    lastUpdate = new Date();
    if (opened == null) {
      opened = lastUpdate;
    }
    revision = Integer.toString(ThreadLocalRandom.current().nextInt());
    log.trace("{} updated to rev {}", getId(), revision);
  }

  public ErrorState findErrorState(ErrorId id) {
    return errorStates.get(id);
  }

  public Optional<ErrorState> getErrorState(ItemId itemId, String code) {
    return Optional.ofNullable(errorStates.get(ImmutableErrorId.of(itemId, code)));
  }

  public String getRevision() {
    return revision;
  }

  @Nonnull
  public Instant getLastUpdate() {
    return lastUpdate.toInstant();
  }

  @Nullable
  public Instant getCompleted() {
    if (completed == null) {
      return null;
    }
    return completed.toInstant();
  }

  @Nullable
  public Instant getOpened() {
    if (opened == null) {
      return null;
    }
    return opened.toInstant();
  }


  @Nullable
  public Instant getLastAnswer() {
    if (lastUpdate == null) {
      return null;
    }
    return lastUpdate.toInstant();
  }

  public boolean isCompleted() {
    return completed != null;
  }

  public boolean complete() {
    if (this.completed == null) {
      this.completed = new Date();
    }
    return isCompleted();
  }

  public String getLanguage() {
    return language;
  }

  public void setLanguage(String language) {
    this.language = language;
  }

  @Nonnull
  @Override
  public Map<ItemId, ItemState> getItemStates() {
    return Collections.unmodifiableMap(itemStates);
  }

  @Nonnull
  @Override
  public Map<ValueSetId, ValueSetState> getValueSetStates() {
    return Collections.unmodifiableMap(valueSetStates);
  }

  @Nonnull
  @Override
  public Map<ErrorId, ErrorState> getErrorStates() {
    return Collections.unmodifiableMap(errorStates);
  }

  @Nonnull
  public Optional<ValueSetState> getValueSetState(ValueSetId id) {
    return Optional.of(valueSetStates.get(id));
  }

  public Optional<ItemState> findHoistingGroup(ItemId id) {
    return itemStates.values().stream().filter(itemState -> itemState.getItems().contains(id)).findFirst();
  }

  public Optional<ItemState> findPrototype(ItemId itemId) {
    if (itemId.isPartial()) {
      return Optional.ofNullable(itemPrototypes.get(itemId));
    }
    return itemPrototypes.values().stream()
      .filter(itemState -> IdUtils.matches(itemState.getId(), itemId))
      .findFirst();
  }

  @Nonnull
  private Stream<Map.Entry<ItemId,ItemState>> findMatchingItemsEntries(ItemId partialItemId) {
    return itemStates
      .entrySet()
      .stream()
      .filter(item -> IdUtils.matches(partialItemId, item.getKey()));
  }

  @Nonnull
  private Stream<Map.Entry<ErrorId,ErrorState>> findMatchingErrorEntries(ErrorId partialErrorId) {
    return errorStates
      .entrySet()
      .stream()
      .filter(item -> IdUtils.matches(partialErrorId, item.getKey()));
  }

  @Nonnull
  public Stream<ItemId> findMatchingItemIds(ItemId partialItemId) {
    final UnaryOperator<Map.Entry<? extends ItemId, ?>> logger = log.isDebugEnabled() ?
      itemEntry -> {
        log.debug("Matched {} -> {}", partialItemId, itemEntry.getKey());
        return itemEntry;
      } :
      UnaryOperator.identity();

    if (partialItemId instanceof ErrorId) {
      return findMatchingErrorEntries((ErrorId) partialItemId)
        .map(logger)
        .map(Map.Entry::getKey);
    }
    return findMatchingItemsEntries(partialItemId)
      .map(logger)
      .map(Map.Entry::getKey);
  }

  @Nonnull
  public Stream<ErrorState> findErrorPrototypes(ItemId itemId) {
    if (itemId.isPartial()) {
      return errorPrototypes.values().stream().filter(errorPrototype -> errorPrototype.getItemId().equals(itemId));
    }
    return errorPrototypes.values().stream().filter(errorPrototype -> IdUtils.matches(errorPrototype.getItemId(), itemId));
  }

  public String generateUpdateId() {
    return Integer.toString(asyncUpdateCount++);
  }
}
