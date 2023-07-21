package io.resys.thena.tasks.client.api.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import javax.annotation.Nullable;

import org.immutables.value.Value;

/*-
 * #%L
 * thena-tasks-client
 * %%
 * Copyright (C) 2021 - 2023 Copyright 2021 ReSys OÜ
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


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Value.Immutable @JsonSerialize(as = ImmutableExport.class) @JsonDeserialize(as = ImmutableExport.class)
public interface Export {

  String getId();
  String getHash();
  LocalDateTime getCreated();
  LocalDate getStartDate();
  LocalDate getEndDate();
  String getName();
  List<ExportEvent> getEvents();

  
  @Value.Immutable @JsonSerialize(as = ImmutableExportEvent.class) @JsonDeserialize(as = ImmutableExportEvent.class)
  interface ExportEvent {
    String getId();
    String getTaskId();
    ExportEventType getEventType();
    LocalDate getEventDate(); // when the event happened
    
    @Nullable
    LocalDate getEndDate(); // only when a date period is used in combination with the event date
    @Nullable
    Task.Status getTaskStatus(); // only used for Task Status events
  }
  
  enum ExportEventType {
    TASK_UPDATED,
    TASK_STATUS_SET
  }
}
