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
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.digiexpress.eveli.client.api.HealthClient;
import io.digiexpress.eveli.client.api.HealthClient.HealthEntry;
import io.digiexpress.eveli.client.api.HealthClient.ProcessHealth;
import io.digiexpress.eveli.client.api.HealthClient.ProcessHealthStatus;
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
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class HealthEntryVisitor {

  private final TaskClient taskClient;

  
  public Multi<HealthEntry> accept() {
    
    final var config = taskClient.unwrap().getConfig();
    final var grim = config.getClient().grim(config.getTenantName());
    
    return grim.find().missionProcsQuery().findAllOnOrAfter(OffsetDateTime.now().minusMonths(6)).collect().asList()
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
    
    final var procsByTaskId = new HashMap<String, GrimProcess>();
    for(final var proc : procs) {
      procsByTaskId.put(proc.getMissionId(), proc);
    }
    
    final var viewsByTask = views.stream().collect(Collectors.groupingBy(GrimCommitViewer::getMissionId));
    
    return Multi.createFrom().items(Stream.concat(
        procs.stream().filter(proc -> !tasksById.containsKey(proc.getMissionId())).map(proc -> {
      
        final HealthClient.ProcessHealthStatus errorStatus = 
            (proc.getMissionId() == null && proc.getFlowBody() != null) ? 
            HealthClient.ProcessHealthStatus.ERRORS : 
            HealthClient.ProcessHealthStatus.RUNNING;
        
        return diagnose(ImmutableProcessHealth.builder()
          .ageInDays(Duration.between(proc.getCreated(), OffsetDateTime.now()).toDays())
          .customerId(proc.getUserId())
          .taskRef(proc.getMissionRef())
          .status(proc.getMissionId() == null ? errorStatus : HealthClient.ProcessHealthStatus.COMPLETED)
          .flowBody(Optional.ofNullable(proc.getFlowBody()).orElse(null))
          .formBody(Optional.ofNullable(proc.getFormBody()).orElse(null))
          .flowName(proc.getFlowName())
          .formName(proc.getFormName())
          .name(proc.getWorkflowName())
          .createdAt(proc.getCreated())
          .id(proc.getId())
          .build());
      }),
        
      tasks.stream().map(task -> {
        final boolean viewed = !viewsByTask.getOrDefault(task.getId(), Collections.emptyList()).isEmpty();
        final var proc = Optional.ofNullable(procsByTaskId.get(task.getId()));
        return diagnose(ImmutableTaskHealth.builder()
          .id(task.getId())
          .ageInDays(Duration.between(task.getCreated(), OffsetDateTime.now()).toDays())
          .customerId(task.getClientIdentificator())
          .taskRef(task.getTaskRef())
          .flowBody(proc.map(GrimProcess::getFlowBody).orElse(null))
          .formBody(proc.map(GrimProcess::getFormBody).orElse(null))
          .flowName(proc.map(GrimProcess::getFlowName).orElse(null))
          .formName(proc.map(GrimProcess::getFormName).orElse(null))
          .name(proc.map(GrimProcess::getWorkflowName).orElse(null))
          .assignedRoles(task.getAssignedRoles())
          .taskStatus(task.getStatus().name())
          .processId(proc.map(GrimProcess::getId).orElse(null))
          .viewed(viewed)
          .createdAt(task.getCreated().toOffsetDateTime())
          .subject(task.getSubject())
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
    if(healthData.getStatus() == ProcessHealthStatus.ERRORS) {
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
