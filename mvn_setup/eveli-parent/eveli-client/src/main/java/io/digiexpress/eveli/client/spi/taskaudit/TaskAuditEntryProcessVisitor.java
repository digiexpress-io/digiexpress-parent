package io.digiexpress.eveli.client.spi.taskaudit;

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

import java.util.Optional;

import io.digiexpress.eveli.client.api.ImmutableProcessInstance;
import io.digiexpress.eveli.client.api.ImmutableTaskAuditEntryProcess;
import io.digiexpress.eveli.client.api.TaskAuditClient.TaskAuditEntryProcess;
import io.digiexpress.eveli.client.api.TaskClient;
import io.digiexpress.eveli.client.spi.task.TaskMapper;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class TaskAuditEntryProcessVisitor {
  private final TaskClient taskClient;
  private final String taskId;
  
  public Uni<Optional<TaskAuditEntryProcess>> accept() {
    
    final var config = taskClient.unwrap().getConfig();
    final var grim = config.getClient().grim(config.getTenantName());
    return grim.find().missionProcsQuery()
      .findOneByMissionId(taskId)
      .onItem().transform(proc -> {
        if(proc.isEmpty()) {
          return Optional.<TaskAuditEntryProcess>empty();
        }
        return Optional.of(ImmutableTaskAuditEntryProcess.builder()
            .processFlowLog(Optional.ofNullable(proc.get().getFlowBody()).map(json -> new JsonObject(json)).orElse(null))
            .processFormLog(Optional.ofNullable(proc.get().getFormBody()).map(json -> new JsonObject(json)).orElse(null))
            .processInstance(ImmutableProcessInstance.builder()
                .from(TaskMapper.map(proc.get()))
                .build())
            .build());
      });
    
  }
}
