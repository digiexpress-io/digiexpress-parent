package io.digiexpress.eveli.client.api;

/*-
 * #%L
 * eveli-client
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

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.digiexpress.eveli.client.api.TaskClient.TaskDiff;
import io.digiexpress.thena.mq.client.api.entities.Binding;
import io.digiexpress.thena.mq.client.api.entities.Channel;
import io.digiexpress.thena.mq.client.api.entities.Delivery;
import io.digiexpress.thena.mq.client.api.entities.Delivery.DeliveryAttempt;
import io.digiexpress.thena.mq.client.api.entities.Queue;
import io.digiexpress.thena.mq.client.api.entities.QueueMessage;
import io.resys.thena.api.entities.grim.GrimCommitViewer;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import jakarta.annotation.Nullable;

public interface TaskAuditClient {
  
  
  TaskAuditQuery createTaskAuditQuery();
  
  
  interface TaskAuditQuery {
    Uni<Optional<TaskAuditLog>> findOneTask(String taskId);
  }
  

  
  @Value.Immutable @JsonSerialize(as = ImmutableTaskAuditLog.class) @JsonDeserialize(as = ImmutableTaskAuditLog.class)
  interface TaskAuditLog {
    String getId();
    
    @Nullable TaskAuditEntryProcess getFlow();
    @Nullable TaskAuditEntryMq getMq();
    
    TaskAuditEntryDiff getDiff();
    TaskAuditEntryAccess getAccess();
  }
  
  
  
  
  @Value.Immutable @JsonSerialize(as = ImmutableTaskAuditEntryProcess.class) @JsonDeserialize(as = ImmutableTaskAuditEntryProcess.class)
  interface TaskAuditEntryProcess {
    ProcessClient.ProcessInstance getProcessInstance();
    JsonObject getProcessFlowLog();
    
    default TaskAuditEntryType getType() {
      return TaskAuditEntryType.FLOW;
    }
  }
  
  
  @Value.Immutable @JsonSerialize(as = ImmutableTaskAuditEntryMq.class) @JsonDeserialize(as = ImmutableTaskAuditEntryMq.class)
  interface TaskAuditEntryMq {
    Map<String, Delivery> getDeliveries();    
    Map<String, DeliveryAttempt> getDeliveryAttempts();
    Map<String, Binding> getBindings();
    Map<String, QueueMessage> getPublishedMessages();
    Map<String, Queue> getQueues();
    Map<String, Channel> getChannels();
    
    default TaskAuditEntryType getType() {
      return TaskAuditEntryType.MQ;
    }
  }
  
  @Value.Immutable @JsonSerialize(as = ImmutableTaskAuditEntryDiff.class) @JsonDeserialize(as = ImmutableTaskAuditEntryDiff.class)
  interface TaskAuditEntryDiff {
    
    Map<String, TaskDiff> getValue();
    Map<String, TaskCommiter> getCommiters();
    
    default TaskAuditEntryType getType() {
      return TaskAuditEntryType.DIFF;
    }
  }
  
  
  @Value.Immutable @JsonSerialize(as = ImmutableTaskAuditEntryAccess.class) @JsonDeserialize(as = ImmutableTaskAuditEntryAccess.class)
  interface TaskAuditEntryAccess {
    List<GrimCommitViewer> getValue();
    
    default TaskAuditEntryType getType() {
      return TaskAuditEntryType.VIEWER;
    }
  }
  
  
  interface TaskCommiter {
    OffsetDateTime getCommitedAt();
    String getCommitId();
    String getUserId();
    String getMessage();
  }
  
  enum TaskAuditEntryType {
    DIFF, MQ, FLOW, VIEWER
  }
}
