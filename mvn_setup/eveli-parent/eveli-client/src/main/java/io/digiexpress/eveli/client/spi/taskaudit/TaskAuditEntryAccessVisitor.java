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

import io.digiexpress.eveli.client.api.ImmutableTaskAuditEntryAccess;
import io.digiexpress.eveli.client.api.TaskAuditClient.TaskAuditEntryAccess;
import io.digiexpress.eveli.client.api.TaskClient;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TaskAuditEntryAccessVisitor {
  private final TaskClient taskClient;
  private final String taskId;
  
  
  public Uni<TaskAuditEntryAccess> accept() {
    final var config = taskClient.unwrap().getConfig();
    final var grim = config.getClient().grim(config.getTenantName());
    
    return Uni.combine().all().unis(
        grim.find().commitViewersQuery().missionId(taskId).findAll(),
        grim.find().commitQuery().findAllCommitsByMissionId(taskId)       
    ).asTuple()
    .onItem().transform(tuple -> {
      return ImmutableTaskAuditEntryAccess.builder()
          .addAllValue(tuple.getItem1().getObjects())
          .putAllCommits(tuple.getItem2().getObjects().getCommits())
          .putAllCommitTrees(tuple.getItem2().getObjects().getCommitTrees())
          .build();
      
    });
  }
}
