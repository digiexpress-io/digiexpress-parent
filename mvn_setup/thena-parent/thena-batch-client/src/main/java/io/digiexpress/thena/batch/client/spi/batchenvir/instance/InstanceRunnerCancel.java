package io.digiexpress.thena.batch.client.spi.batchenvir.instance;

/*-
 * #%L
 * thena-batch-client
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
import java.util.Optional;

import io.digiexpress.thena.batch.client.api.entities.ImmutableRuntimeInstance;
import io.digiexpress.thena.batch.client.api.entities.ImmutableRuntimeLog;
import io.digiexpress.thena.batch.client.api.entities.RuntimeInstance;
import io.digiexpress.thena.batch.client.api.entities.RuntimeInstance.RuntimeExecutionStatus;
import io.digiexpress.thena.batch.client.api.entities.RuntimeLog.ExecutionLogLevel;
import io.digiexpress.thena.batch.client.api.executor.ExecutorContext;
import io.digiexpress.thena.batch.client.api.persistence.BatchDb;
import io.resys.thena.support.OidUtils;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;




@RequiredArgsConstructor
public class InstanceRunnerCancel {

  private final ExecutorContext context;
  
  /**
   * Start the instance, move status to executing and log the relevant parts of technical configuration
   */
  public Uni<RuntimeInstance> accept() {
    return context.getDb().withTransaction(context.getScope(), db -> doInTx(db));
  }
  
  private Uni<RuntimeInstance> doInTx(BatchDb tx) {
    return tx.query().queryInstances().getById(context.getInstance().getId(), true).onItem()
        .transformToUni(instance -> cancelInstance(instance, tx)); 
  }
  
  
  private Uni<RuntimeInstance> cancelInstance(RuntimeInstance instance, BatchDb tx) {
    // move the status to execution
    final var log = ImmutableRuntimeLog.builder()
        .id(OidUtils.gen())
        .runtimeId(instance.getId())
        .level(ExecutionLogLevel.INFO)
        .createdAt(OffsetDateTime.now())
        .format("Started runtime instance execution")
        .formatType("RUNTIME_CANCEL_EXECUTION")
        .extra(JsonObject.of(
            "commitMessage", context.getScope().getCommitMessage(),
            "commitAuthor", context.getScope().getCommitAuthor()
          ))
        .build();
    
    final RuntimeInstance updatedInstance = ImmutableRuntimeInstance.builder().from(instance)
        .status(RuntimeInstance.RuntimeStatus.CANCELLED)
        .executionStatus(RuntimeExecutionStatus.OK)
        .endedAt(Optional.empty())
        .build();
    
    final var toBeSaved = context.createPersistContainer()
        .addRuntimeInstanceUpdates(updatedInstance)
        .addRuntimeLogInserts(log)
        .build();
    
    return tx.builder().persist(toBeSaved).onItem().transform(ignore -> updatedInstance);
  }
  
  

}
