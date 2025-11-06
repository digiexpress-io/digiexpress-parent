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

import org.apache.commons.lang3.exception.ExceptionUtils;

import io.digiexpress.thena.batch.client.api.entities.ImmutableRuntimeInstance;
import io.digiexpress.thena.batch.client.api.entities.ImmutableRuntimeLog;
import io.digiexpress.thena.batch.client.api.entities.RuntimeInstance;
import io.digiexpress.thena.batch.client.api.entities.RuntimeInstance.RuntimeExecutionStatus;
import io.digiexpress.thena.batch.client.api.entities.RuntimeInstance.RuntimeStatus;
import io.digiexpress.thena.batch.client.api.entities.RuntimeLog.ExecutionLogLevel;
import io.digiexpress.thena.batch.client.api.executor.ExecutorContext;
import io.digiexpress.thena.batch.client.api.persistence.BatchDb;
import io.digiexpress.thena.batch.client.spi.batchenvir.BatchEnvirLogger;
import io.resys.thena.support.OidUtils;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;




@RequiredArgsConstructor
public class InstanceRunnerFail {

  private final ExecutorContext context;
  private final Throwable throwable;
  
  /**
   * Start the instance, move status to executing and log the relevant parts of technical configuration
   */
  public Uni<RuntimeInstance> accept() {
    return context.getDb().withTransaction(context.getScope(), db -> doInTx(db));
  }
  

  private Uni<RuntimeInstance> failInstance(RuntimeInstance instance, BatchDb tx) {
    
    BatchEnvirLogger.INSTANCE_ERROR.withContext(this.context)
    .cause(throwable)
    .append("Failed to run instance: {}!", throwable.getMessage());
    
    final var start = context.getInstance();
    final var log = ImmutableRuntimeLog.builder()
        .id(OidUtils.gen())
        .runtimeId(start.getId())
        .level(ExecutionLogLevel.ERROR)
        .createdAt(OffsetDateTime.now())
        .format("Failed to runtime execution")
        .formatType(BatchEnvirLogger.INSTANCE_ERROR.getName())
        .stack(ExceptionUtils.getStackTrace(throwable))
        .build();
    final RuntimeInstance updatedInstance = ImmutableRuntimeInstance.builder().from(start)
        .status(RuntimeStatus.COMPLETED)
        .executionStatus(RuntimeExecutionStatus.ERROR)
        .endedAt(OffsetDateTime.now())
        .build();
    return tx.builder().persist(context.createPersistContainer()
        .addRuntimeInstanceUpdates(updatedInstance)
        .addRuntimeLogInserts(log)
        .build()).onItem().transform(ignore -> updatedInstance);
  }
  
  private Uni<RuntimeInstance> doInTx(BatchDb db) {
    return db.query().queryInstances().getById(context.getInstance().getId(), true).onItem()
        .transformToUni(instance -> failInstance(instance, db)); 
  }

  
}
