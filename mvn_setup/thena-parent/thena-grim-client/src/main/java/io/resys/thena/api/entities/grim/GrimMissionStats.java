package io.resys.thena.api.entities.grim;

/*-
 * #%L
 * thena-db-client
 * %%
 * Copyright (C) 2015 - 2025 Copyright 2022 ReSys OÜ
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.time.LocalDate;

import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.resys.thena.api.envelope.ThenaContainer;
import jakarta.annotation.Nullable;

public interface GrimMissionStats {
  
  
  @JsonSerialize(as = ImmutableGrimMissionAttributeEvent.class)
  @JsonDeserialize(as = ImmutableGrimMissionAttributeEvent.class)
  @JsonIgnoreProperties(ignoreUnknown = true)
  @Value.Immutable
  interface GrimMissionAttributeEvent extends ThenaContainer {
    @Nullable LocalDate getEventDate(); // date for events
    long getEventCount();                         // number of events
    GrimMissionAttributeEventType getEventType(); // number of events for specific attribute
    String getAttributeValue();// attribute value
  }
  
  enum GrimMissionAttributeEventType {
    STATUS, PRIORITY, OVERDUE, STATUS_DATE
  }
}
