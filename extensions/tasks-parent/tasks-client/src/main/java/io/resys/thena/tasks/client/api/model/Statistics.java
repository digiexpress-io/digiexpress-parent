package io.resys.thena.tasks.client.api.model;

/*-
 * #%L
 * thena-tasks-api
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

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Value.Immutable @JsonSerialize(as = ImmutableStatistics.class) @JsonDeserialize(as = ImmutableStatistics.class)
public interface Statistics extends Serializable {
  

  @Value.Immutable @JsonSerialize(as = ImmutableByPriority.class) @JsonDeserialize(as = ImmutableByPriority.class)
  interface ByPriority extends Statistics {
    Map<String, Long> getCountByPriority();
  }
  
  @Value.Immutable @JsonSerialize(as = ImmutableByStatus.class) @JsonDeserialize(as = ImmutableByStatus.class)
  interface ByStatus extends Statistics {
    Map<String, Long> getCountByStatus(); 
  }
  
  @Value.Immutable @JsonSerialize(as = ImmutableByStatusChange.class) @JsonDeserialize(as = ImmutableByStatusChange.class)
  interface ByStatusChange extends Statistics {
    List<StatusOnDate> getValues();
  }
  
  @Value.Immutable @JsonSerialize(as = ImmutableStatusOnDate.class) @JsonDeserialize(as = ImmutableStatusOnDate.class)
  interface StatusOnDate {
    LocalDate getTargetDate();
    Map<String, Long> getCountByStatus();
  }
  
  @Value.Immutable @JsonSerialize(as = ImmutableStatisticsSummary.class) @JsonDeserialize(as = ImmutableStatisticsSummary.class)
  interface StatisticsSummary {
    String getCommitId();
    ByPriority getByPriority();
    ByPriority getByStatus();
    ByStatusChange getByStatusChange();
  }
}
