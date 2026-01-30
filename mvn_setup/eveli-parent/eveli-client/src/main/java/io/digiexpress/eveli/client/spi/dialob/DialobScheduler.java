package io.digiexpress.eveli.client.spi.dialob;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

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

import java.util.concurrent.TimeUnit;

import org.springframework.scheduling.annotation.Scheduled;

import io.digiexpress.eveli.client.api.TaskClient;
import io.digiexpress.eveli.client.api.TaskClient.ProcessStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@RequiredArgsConstructor
public class DialobScheduler {
  
  private final TaskClient taskClient;
  private final SyncDialobAndProcess syncDialobAndProcess;
  
  @Scheduled(fixedRate = 24, timeUnit = TimeUnit.HOURS)
  public CompletableFuture<?> executeFlow() {
    return taskClient.queryTaskProcesess()
      .findAllAnsweredFrom(OffsetDateTime.now().minusMonths(6))
      .onItem().transformToUni(instance -> syncDialobAndProcess.executeFlowForInstance(instance, Optional.empty()))
      .concatenate()
      .collect().asList()
      .subscribeAsCompletionStage()
      .toCompletableFuture();
  }
  
  @Scheduled(fixedRate = 24, timeUnit = TimeUnit.HOURS)
  public CompletableFuture<?> rejectProcessesWithDeadline() {
    return taskClient.queryTaskProcesess().findAllExpired()
        .onItem().transformToUni(proc -> {
          log.warn("Expiry for process instance: {}, expiresAt: {}!", proc.getId(), proc.getExpiresAt());
          return taskClient.modifyProcess()
            .commitAuthor(DialobScheduler.class.getSimpleName())
            .commitMessage("Rejecting expired")
            .id(proc.getId().toString())
            .merge((currentState, merge) -> merge.status(ProcessStatus.EXPIRED).build())
            .build().onFailure().recoverWithNull();
        })
        
        .concatenate()
        .collect().asList()
        .subscribeAsCompletionStage()
        .toCompletableFuture();
  }
}



