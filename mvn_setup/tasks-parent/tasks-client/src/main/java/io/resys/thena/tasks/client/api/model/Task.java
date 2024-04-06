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
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import javax.annotation.Nullable;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Value.Immutable @JsonSerialize(as = ImmutableTask.class) @JsonDeserialize(as = ImmutableTask.class)
public interface Task extends Document {

  Instant getCreated();
  Instant getUpdated();
  @Nullable Instant getArchived();
  @Nullable LocalDate getStartDate();
  @Nullable LocalDate getDueDate();
  
  @Nullable String getParentId(); //for task linking/grouping 
  List<TaskTransaction> getTransactions(); 
  List<String> getRoles();
  List<String> getAssigneeIds();
  String getReporterId();
  
  String getTitle();
  String getDescription();
  Priority getPriority();
  Status getStatus();
  List<String> getLabels();
  List<TaskExtension> getExtensions();
  List<TaskComment> getComments();
  
  List<Checklist> getChecklist();
  
  
  @Value.Default
  default DocumentType getDocumentType() {
    return DocumentType.TASK;
  }
  
  enum Status { CREATED, IN_PROGRESS, COMPLETED, REJECTED }
  enum Priority { LOW, MEDIUM, HIGH }  
  
  
  @Value.Immutable @JsonSerialize(as = ImmutableChecklist.class) @JsonDeserialize(as = ImmutableChecklist.class)
  interface Checklist extends Serializable, TaskItem {
    String getId();
    String getTitle();
    
    List<ChecklistItem> getItems();
  }
  
  @Value.Immutable @JsonSerialize(as = ImmutableChecklistItem.class) @JsonDeserialize(as = ImmutableChecklistItem.class)
  interface ChecklistItem extends Serializable, TaskItem {
    String getId();
    List<String> getAssigneeIds();
    @Nullable LocalDate getDueDate();
    Boolean getCompleted();
    String getTitle();
  }
  
  @Value.Immutable @JsonSerialize(as = ImmutableTaskTransaction.class) @JsonDeserialize(as = ImmutableTaskTransaction.class)
  interface TaskTransaction extends Serializable, TaskItem {
    String getId();
    List<TaskCommand> getCommands(); 
  }

  interface TaskItem {
    String getId();
  }
  
  @Value.Immutable @JsonSerialize(as = ImmutableTaskExtension.class) @JsonDeserialize(as = ImmutableTaskExtension.class)
  interface TaskExtension extends Serializable, TaskItem {
    String getType(); //DIALOB, CUSTOMER
    String getName();
    String getBody();
    Instant getCreated();
    Instant getUpdated();
  }
  enum TaskExtensionType {
    CUSTOMER, DIALOB
  }; 
  
  @Value.Immutable @JsonSerialize(as = ImmutableTaskComment.class) @JsonDeserialize(as = ImmutableTaskComment.class)
  interface TaskComment extends TaskItem {
    Instant getCreated();
    @Nullable String getReplyToId();
    String getCommentText();
    String getUsername();
  }  
}
