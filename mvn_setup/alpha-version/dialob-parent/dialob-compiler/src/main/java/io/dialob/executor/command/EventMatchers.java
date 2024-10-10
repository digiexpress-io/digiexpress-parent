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

import javax.annotation.Nonnull;

import org.immutables.value.Value;

import io.dialob.executor.command.event.ActivePageUpdatedEvent;
import io.dialob.executor.command.event.ActiveUpdatedEvent;
import io.dialob.executor.command.event.AnsweredUpdatedEvent;
import io.dialob.executor.command.event.AnyInvalidAnswersUpdatedEvent;
import io.dialob.executor.command.event.AttributeEvent;
import io.dialob.executor.command.event.AvailableItemsUpdatedEvent;
import io.dialob.executor.command.event.DisabledUpdatedEvent;
import io.dialob.executor.command.event.ErrorActiveUpdatedEvent;
import io.dialob.executor.command.event.ErrorEvent;
import io.dialob.executor.command.event.Event;
import io.dialob.executor.command.event.ItemAddedEvent;
import io.dialob.executor.command.event.ItemRemovedEvent;
import io.dialob.executor.command.event.ItemsChangedEvent;
import io.dialob.executor.command.event.RequiredUpdatedEvent;
import io.dialob.executor.command.event.RowCanBeRemovedUpdatedEvent;
import io.dialob.executor.command.event.RowGroupItemsInitEvent;
import io.dialob.executor.command.event.RowsCanBeAddedUpdatedEvent;
import io.dialob.executor.command.event.SessionLocaleUpdatedEvent;
import io.dialob.executor.command.event.TargetEvent;
import io.dialob.executor.command.event.ValidUpdatedEvent;
import io.dialob.executor.model.ErrorId;
import io.dialob.executor.model.IdUtils;
import io.dialob.executor.model.ItemId;

@Value.Enclosing
public final class EventMatchers {

  private static final ImmutableEventMatchers.ActivePageEventMatcher ACTIVE_PAGE_EVENT_MATCHER = ImmutableEventMatchers.ActivePageEventMatcher.builder().build();

  private static final ImmutableEventMatchers.AvailableItemsEventMatcher AVAILABLE_ITEMS_EVENT_MATCHER = ImmutableEventMatchers.AvailableItemsEventMatcher.builder().build();

  private static final ImmutableEventMatchers.AnyErrorEventMatcher ANY_ERROR_EVENT_MATCHER = ImmutableEventMatchers.AnyErrorEventMatcher.builder().build();

  private EventMatchers() {
  }

  public static ErrorEventMatcher anyError() {
    return ANY_ERROR_EVENT_MATCHER;
  }

  public static ErrorEventMatcher error(@Nonnull ErrorId errorId) {
    return ImmutableEventMatchers.ErrorIdEventMatcher.of(errorId);
  }

  public static ErrorEventMatcher targetError(@Nonnull ItemId itemId) {
    return ImmutableEventMatchers.TargetErrorEventMatcher.of(itemId);
  }

  public static EventMatcher whenActiveUpdated(@Nonnull ItemId itemId) {
    return ImmutableEventMatchers.IsActiveTargetEventMatcher.of(itemId);
  }

  public static EventMatcher whenValueUpdated(@Nonnull ItemId itemId) {
    return ImmutableEventMatchers.TargetIdEventMatcher.of(itemId);
  }

  public static EventMatcher whenRequiredUpdated(@Nonnull ItemId itemId) {
    return ImmutableEventMatchers.IsRequiredTargetEventMatcher.of(itemId);
  }

  public static EventMatcher whenDisabledUpdatedEvent(@Nonnull ItemId itemId) {
    return ImmutableEventMatchers.IsDisabledTargetEventMatcher.of(itemId);
  }

  public static EventMatcher whenRowsCanBeAddedUpdatedEvent(@Nonnull ItemId itemId) {
    return ImmutableEventMatchers.RowsCanBeAddedUpdatedEventMatcher.builder().build();
  }

  public static EventMatcher whenRowCanBeRemovedUpdatedEvent(@Nonnull ItemId itemId) {
    return ImmutableEventMatchers.RowCanBeRemovedUpdatedEventMatcher.builder().build();
  }

  public static EventMatcher whenValidUpdated(@Nonnull ItemId itemId) {
    return ImmutableEventMatchers.IsValidTargetEventMatcher.of(itemId);
  }

  public static EventMatcher whenAnyInvalidAnswersUpdated() {
    return ImmutableEventMatchers.AnyInvalidAnswersUpdatedEventMatcher.builder().build();
  }

  public static EventMatcher whenActivePageUpdated() {
    return ACTIVE_PAGE_EVENT_MATCHER;
  }

  public static EventMatcher whenAvailableItemsUpdated() {
    return AVAILABLE_ITEMS_EVENT_MATCHER;
  }

  public static EventMatcher whenAnsweredUpdated(@Nonnull ItemId itemId) {
    return ImmutableEventMatchers.AnsweredTargetEventMatcher.of(itemId);
  }

  public static EventMatcher whenItemsChanged(@Nonnull ItemId groupId) {
    return ImmutableEventMatchers.ItemsChangedEventMatcher.of(groupId);
  }

  public static EventMatcher whenItemAdded(@Nonnull ItemId prototypeId) {
    return ImmutableEventMatchers.ItemAddedEventMatcher.of(prototypeId);
  }
  public static EventMatcher whenItemRemoved(@Nonnull ItemId prototypeId) {
    return ImmutableEventMatchers.ItemRemovedEventMatcher.of(prototypeId);
  }
  public static EventMatcher whenRowGroupItemsInit(@Nonnull ItemId prototypeId) {
    return ImmutableEventMatchers.RowGroupItemsInitEventMatcher.of(prototypeId);
  }

  public static EventMatcher errorActivity(@Nonnull ErrorEventMatcher errorEventMatcher) {
    return ImmutableEventMatchers.ErrorActivityEventMatcher.of(errorEventMatcher);
  }

  public static EventMatcher whenSessionLocaleUpdated() {
    return event -> event instanceof SessionLocaleUpdatedEvent;
  }

    @Value.Immutable
  public interface TargetIdEventMatcher extends EventMatcher {
    @Value.Parameter
    ItemId getTargetId();

    @Override
    default boolean matches(Event event) {
      if (event instanceof TargetEvent) {
        TargetEvent targetEvent = (TargetEvent) event;
        return IdUtils.matches(getTargetId(), targetEvent.getTargetId());
      }
      return false;
    }
  }

  interface QuestionnaireEventMatcher extends EventMatcher {
  }

  interface AttributeEventMatcher<E extends AttributeEvent> extends EventMatcher {

    @Value.Parameter
    ItemId getTargetMatcher();

    boolean eventTypeMatches(Event event);

    @Override
    default boolean matches(Event event) {
      if (eventTypeMatches(event)) {
        E attributeEvent = (E) event;
        TargetEvent targetEvent = attributeEvent.getTarget();
        return IdUtils.matches(getTargetMatcher(), targetEvent.getTargetId());
      }
      return false;
    }
  }

  @Value.Immutable
  interface IsActiveTargetEventMatcher extends AttributeEventMatcher<ActiveUpdatedEvent> {
    @Override
    default boolean eventTypeMatches(Event event) {
      return event instanceof ActiveUpdatedEvent;
    }
  }

  @Value.Immutable
  interface IsRequiredTargetEventMatcher extends AttributeEventMatcher<ActiveUpdatedEvent> {
    @Override
    default boolean eventTypeMatches(Event event) {
      return event instanceof RequiredUpdatedEvent;
    }
  }

  @Value.Immutable
  interface IsDisabledTargetEventMatcher extends AttributeEventMatcher<DisabledUpdatedEvent> {
    @Override
    default boolean eventTypeMatches(Event event) {
      return event instanceof DisabledUpdatedEvent;
    }
  }

  @Value.Immutable
  interface IsValidTargetEventMatcher extends AttributeEventMatcher<ValidUpdatedEvent> {
    @Override
    default boolean eventTypeMatches(Event event) {
      return event instanceof ValidUpdatedEvent;
    }
  }

  @Value.Immutable
  interface AnyInvalidAnswersUpdatedEventMatcher extends EventMatcher {
    @Override
    default boolean matches(Event event) {
      return event instanceof AnyInvalidAnswersUpdatedEvent;
    }
  }

  @Value.Immutable
  interface ItemAddedEventMatcher extends EventMatcher {

    @Value.Parameter
    ItemId getPrototypeId();

    @Override
    default boolean matches(Event event) {
      return event instanceof ItemAddedEvent && ((ItemAddedEvent)event).getPrototypeId().equals(getPrototypeId());
    }
  }

  @Value.Immutable
  interface ItemRemovedEventMatcher extends EventMatcher {

    @Value.Parameter
    ItemId getPrototypeId();

    @Override
    default boolean matches(Event event) {
      return event instanceof ItemRemovedEvent && IdUtils.matches(getPrototypeId(), ((ItemRemovedEvent)event).getRemoveItemId());
    }
  }

  @Value.Immutable
  interface RowGroupItemsInitEventMatcher extends EventMatcher {

    @Value.Parameter
    ItemId getPrototypeId();

    @Override
    default boolean matches(Event event) {
      return event instanceof RowGroupItemsInitEvent && ((RowGroupItemsInitEvent)event).getPrototypeId().equals(getPrototypeId());
    }
  }

  @Value.Immutable
  interface RowsCanBeAddedUpdatedEventMatcher extends EventMatcher {

    @Override
    default boolean matches(Event event) {
      return event instanceof RowsCanBeAddedUpdatedEvent;
    }
  }

  @Value.Immutable
  interface RowCanBeRemovedUpdatedEventMatcher extends EventMatcher {

    @Override
    default boolean matches(Event event) {
      return event instanceof RowCanBeRemovedUpdatedEvent;
    }
  }

  @Value.Immutable
  interface ItemsChangedEventMatcher extends AttributeEventMatcher<ItemsChangedEvent> {
    @Override
    default boolean eventTypeMatches(Event event) {
      return event instanceof ItemsChangedEvent;
    }
  }

  @Value.Immutable
  interface AnsweredTargetEventMatcher extends AttributeEventMatcher<AnsweredUpdatedEvent> {
    @Override
    default boolean eventTypeMatches(Event event) {
      return event instanceof AnsweredUpdatedEvent;
    }
  }

  @Value.Immutable(prehash = true)
  interface ActivePageEventMatcher extends QuestionnaireEventMatcher {
    @Override
    default boolean matches(Event event) {
      return event instanceof ActivePageUpdatedEvent;
    }
  }

  @Value.Immutable
  interface AvailableItemsEventMatcher extends QuestionnaireEventMatcher {
    @Override
    default boolean matches(Event event) {
      return event instanceof AvailableItemsUpdatedEvent;
    }
  }

  interface ErrorEventMatcher extends EventMatcher {
  }

  @Value.Immutable
  interface AnyErrorEventMatcher extends ErrorEventMatcher {
    @Override
    default boolean matches(Event event) {
      return event instanceof ErrorEvent;
    }
  }

  @Value.Immutable
  interface TargetErrorEventMatcher extends ErrorEventMatcher {

    @Value.Parameter
    ItemId getTargetId();

    @Override
    default boolean matches(Event event) {
      if (event instanceof ErrorEvent) {
        ErrorEvent errorEvent = (ErrorEvent) event;
        return getTargetId().equals(errorEvent.getErrorId().getItemId());
      }
      return false;
    }
  }

  @Value.Immutable
  interface ErrorIdEventMatcher extends ErrorEventMatcher {
    @Value.Parameter
    ErrorId getErrorId();

    @Override
    default boolean matches(Event event) {
      if (event instanceof ErrorEvent) {
        ErrorEvent targetEvent = (ErrorEvent) event;
        return getErrorId().equals(targetEvent.getErrorId());
      }
      return false;
    }
  }

  @Value.Immutable
  interface ErrorActivityEventMatcher extends ErrorEventMatcher {
    @Value.Parameter
    ErrorEventMatcher getErrorEventMatcher();

    @Override
    default boolean matches(Event event) {
      if (event instanceof ErrorEvent) {
        ErrorActiveUpdatedEvent targetEvent = (ErrorActiveUpdatedEvent) event;
        return getErrorEventMatcher().matches(targetEvent);
      }
      return false;
    }

  }
}
