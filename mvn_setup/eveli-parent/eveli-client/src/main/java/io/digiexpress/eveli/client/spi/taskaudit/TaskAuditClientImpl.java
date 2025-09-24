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

import io.digiexpress.eveli.client.api.ImmutableTaskAuditLog;
import io.digiexpress.eveli.client.api.TaskAuditClient;
import io.digiexpress.eveli.client.api.TaskClient;
import io.digiexpress.thena.mq.client.api.ThenaMqAppConfig;
import io.digiexpress.thena.mq.client.api.ThenaMqClient;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TaskAuditClientImpl implements TaskAuditClient {

  private final TaskClient taskClient;
  private final Optional<ThenaMqClient> mqClient;
  private final Optional<ThenaMqAppConfig> mqConfig;
  
  @Override
  public TaskAuditQuery createTaskAuditQuery() {
    return new TaskAuditQuery() {
      
      @Override
      public Uni<Optional<TaskAuditLog>> findOneTask(String taskId) {
        
        // join all the queries
        return Uni.combine().all()
        .unis(
            
          // optional MQ data
          mqClient.isPresent() ? 
              new TaskAuditEntryMqVisitor(mqClient.get(), mqConfig.get(), taskId).accept() : 
              Uni.createFrom().item(Optional.<TaskAuditEntryMq>empty()),
              
          // access and commits
          new TaskAuditEntryAccessVisitor(taskClient, taskId).accept(),
          
          
          // process and wrench flow
          new TaskAuditEntryProcessVisitor(taskClient, taskId).accept()
        ).asTuple().onItem().transform(tuple -> {
          

          final var audit = ImmutableTaskAuditLog.builder()
            .id(taskId)
            .access(tuple.getItem2())
            .mq(tuple.getItem1().orElse(null))
            .flow(tuple.getItem3().orElse(null))
            ;
          
          return Optional.of(audit.build());
        });
      }
    };
  }
}
