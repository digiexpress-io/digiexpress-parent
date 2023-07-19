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
package io.dialob.client.spi.event;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.dialob.client.spi.event.EventPublisher.Event;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXTERNAL_PROPERTY, property = "type")
@JsonSubTypes({
  @JsonSubTypes.Type(value = ImmutableFormUpdatedEvent.class, name = "FormUpdated"),
  @JsonSubTypes.Type(value = ImmutableFormDeletedEvent.class, name = "FormDeleted"),
  @JsonSubTypes.Type(value = ImmutableFormTaggedEvent.class, name = "FormTagged")
})
public interface DistributedEvent extends Event {
  String getSource();

  
  interface FormEvent extends Event {
    @Nonnull
    String getFormId();
  }
 
  
  @Value.Immutable
  @JsonSerialize(as = ImmutableFormDeletedEvent.class)
  @JsonDeserialize(as = ImmutableFormDeletedEvent.class)
  @JsonInclude(content = JsonInclude.Include.NON_NULL, value = JsonInclude.Include.NON_EMPTY)
  interface FormDeletedEvent extends FormEvent, DistributedEvent {}
  
  
  @Value.Immutable
  @JsonSerialize(as = ImmutableFormTaggedEvent.class)
  @JsonDeserialize(as = ImmutableFormTaggedEvent.class)
  @JsonInclude(content = JsonInclude.Include.NON_NULL, value = JsonInclude.Include.NON_EMPTY)
  interface FormTaggedEvent extends FormEvent, DistributedEvent {
    @Nonnull
    String getFormName();
    @Nonnull
    String getTagName();
    @Nullable
    String getRefName();
  }
  
  @Value.Immutable
  @JsonSerialize(as = ImmutableFormUpdatedEvent.class)
  @JsonDeserialize(as = ImmutableFormUpdatedEvent.class)
  @JsonInclude(content = JsonInclude.Include.NON_NULL, value = JsonInclude.Include.NON_EMPTY)
  interface FormUpdatedEvent extends FormEvent, DistributedEvent {
    String getRevision();
  }
}