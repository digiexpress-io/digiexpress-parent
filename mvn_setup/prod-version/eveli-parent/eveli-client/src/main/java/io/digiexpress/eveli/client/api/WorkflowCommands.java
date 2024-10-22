package io.digiexpress.eveli.client.api;

/*-
 * #%L
 * eveli-client
 * %%
 * Copyright (C) 2015 - 2024 Copyright 2022 ReSys OÜ
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
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import jakarta.annotation.Nullable;

public interface WorkflowCommands {
  Workflow create(WorkflowInit init);
  WorkflowQuery query();
  Optional<Workflow> update(String workflowId, Workflow workflow);
  void delete(String workflowId);
  WorkflowTagCommands release();
  
  interface WorkflowQuery {
    Optional<Workflow> get(String id);
    Optional<Workflow> getByName(String name);
    List<Workflow> findAll();
  }
  
  interface WorkflowTagCommands extends AssetTagCommands<WorkflowTag> {
  }

  @Value.Immutable
  @JsonSerialize(as = ImmutableWorkflowTag.class)
  @JsonDeserialize(as = ImmutableWorkflowTag.class)
  @JsonIgnoreProperties(ignoreUnknown = true)
  interface WorkflowTag extends AssetTagCommands.AssetTag {
    List<Workflow> getEntries();
  }

  @Value.Immutable
  @JsonSerialize(as = ImmutableWorkflowInit.class)
  @JsonDeserialize(as = ImmutableWorkflowInit.class)
  @JsonIgnoreProperties(ignoreUnknown = true)
  interface WorkflowInit {
    String getName();
    String getFormName();
    String getFormTag();
    String getFlowName();
    @Nullable
    LocalDate getStartDate();
    @Nullable
    LocalDate getEndDate();
  }
  
  @Value.Immutable
  @JsonSerialize(as = ImmutableWorkflow.class)
  @JsonDeserialize(as = ImmutableWorkflow.class)
  @JsonIgnoreProperties(ignoreUnknown = true)
  interface Workflow {
    Long getId();
    String getName();
    String getFormName();
    String getFormTag();
    String getFlowName();
    
    @Nullable
    LocalDate getStartDate();
    @Nullable
    LocalDate getEndDate();
    ZonedDateTime getUpdated();
  }
}
