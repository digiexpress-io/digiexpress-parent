package io.digiexpress.eveli.client.spi.health;

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

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.digiexpress.eveli.client.api.HealthClient;
import io.digiexpress.eveli.client.api.HealthClient.HealthEntry;
import io.digiexpress.eveli.client.api.HealthClient.ProcessHealth;
import io.digiexpress.eveli.client.api.HealthClient.ProcessStatus;
import io.digiexpress.eveli.client.api.HealthClient.TaskHealth;
import io.digiexpress.eveli.client.api.ImmutableProcessHealth;
import io.digiexpress.eveli.client.api.ImmutableTaskHealth;
import io.digiexpress.eveli.client.api.TaskClient;
import io.digiexpress.eveli.client.spi.task.TaskMapper;
import io.resys.thena.api.entities.grim.GrimCommitViewer;
import io.resys.thena.api.entities.grim.GrimProcess;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.tuples.Tuple3;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class HealthEntryVisitor {

  private final TaskClient taskClient;

  
  public Multi<HealthEntry> accept() {
    
    final var config = taskClient.unwrap().getConfig();
    final var grim = config.getClient().grim(config.getTenantName());
    
    return grim.find().missionProcsQuery().findOnOrAfter(OffsetDateTime.now().minusMonths(6)).collect().asList()
      .onItem().transformToUni(procs -> {
        final var taskIds = procs.stream().map(e -> e.getMissionId()).toList();
        return Uni.combine().all().unis(
            taskClient.queryTasks().findAll(taskIds),
            grim.find().commitViewersQuery().missionIds(taskIds).usedFor(TaskMapper.VIEWER_WORKER).findAll()
          ).asTuple()
          .onItem().transform(tuple -> Tuple3.of(tuple.getItem1(), tuple.getItem2().getObjects(), procs));
      })
      .onItem().transformToMulti(tuple -> diagnose(tuple.getItem1(), tuple.getItem2(), tuple.getItem3()));
  }
  
  
  private Multi<HealthEntry> diagnose(List<TaskClient.Task> tasks, List<GrimCommitViewer> views, List<GrimProcess> procs) {
    final var tasksById = tasks.stream().collect(Collectors.toMap(e -> e.getId(), e -> e));
    final var procsByTaskId = procs.stream().collect(Collectors.toMap(e -> e.getMissionId(), e -> e));
    
    final var viewsByTask = views.stream().collect(Collectors.groupingBy(GrimCommitViewer::getMissionId));
    
    return Multi.createFrom().items(Stream.concat(
        procs.stream().filter(proc -> !tasksById.containsKey(proc.getMissionId())).map(proc -> {
      
        final HealthClient.ProcessStatus errorStatus = 
            (proc.getMissionId() == null && proc.getFlowBody() != null) ? 
            HealthClient.ProcessStatus.ERRORS : 
            HealthClient.ProcessStatus.RUNNING;
        
        return diagnose(ImmutableProcessHealth.builder()
          .ageInDays(Duration.between(proc.getCreated(), OffsetDateTime.now()).toDays())
          .customerId(proc.getUserId())
          .taskRef(proc.getMissionRef())
          .status(proc.getMissionId() == null ? errorStatus : HealthClient.ProcessStatus.COMPLETED)
          .flowBody(Optional.ofNullable(proc.getFlowBody()).map(JsonObject::new).orElse(null))
          .formBody(Optional.ofNullable(proc.getFormBody()).map(JsonObject::new).orElse(null))
          .flowName(proc.getFlowName())
          .formName(proc.getFormName())
          .name(proc.getWorkflowName())
          .id(proc.getId())
          .build());
      }),
        
      tasks.stream().map(task -> {
        final boolean viewed = !viewsByTask.getOrDefault(task.getId(), Collections.emptyList()).isEmpty();
        final var proc = procsByTaskId.get(task.getId());
        return diagnose(ImmutableTaskHealth.builder()
          .ageInDays(Duration.between(task.getCreated(), OffsetDateTime.now()).toDays())
          .customerId(task.getClientIdentificator())
          .taskRef(proc.getMissionRef())
          .flowBody(Optional.ofNullable(proc.getFlowBody()).map(JsonObject::new).orElse(null))
          .formBody(Optional.ofNullable(proc.getFormBody()).map(JsonObject::new).orElse(null))
          .flowName(proc.getFlowName())
          .formName(proc.getFormName())
          .name(proc.getWorkflowName())
          .id(task.getId())
          .assignedRoles(task.getAssignedRoles())
          .taskStatus(task.getStatus().name())
          .processId(proc.getId())
          .viewed(viewed)
          .build());
      })
    ));
  }
  
  
  private TaskHealth diagnose(TaskHealth healthData) {
    if(healthData.getAssignedRoles().isEmpty()) {
      return ImmutableTaskHealth.builder().from(healthData)
          .diagnosis(HealthClient.DiagnosisType.ERROR)
          .diagnosisDescription("Roles are missing")
          .build();
    } 
    
    if(healthData.getAgeInDays() >= 3 && healthData.getViewed() != null && !healthData.getViewed()) {
      return ImmutableTaskHealth.builder().from(healthData)
          .diagnosis(HealthClient.DiagnosisType.WARNING)
          .diagnosisDescription("New task but nobody has seen it")
          .build();
    }
    return healthData;
  }
  
  private ProcessHealth diagnose(ProcessHealth healthData) {
    if(healthData.getStatus() == ProcessStatus.ERRORS) {
      return ImmutableProcessHealth.builder().from(healthData)
          .diagnosis(HealthClient.DiagnosisType.ERROR)
          .diagnosisDescription("Flow execution is broken")
          .build();
    } 
    
    if(healthData.getAgeInDays() >= 30 && healthData.getTaskRef() == null) {
      return ImmutableProcessHealth.builder().from(healthData)
          .diagnosis(HealthClient.DiagnosisType.WARNING)
          .diagnosisDescription("No activity for 30 days")
          .build();
    }
    return healthData;
  }
}
