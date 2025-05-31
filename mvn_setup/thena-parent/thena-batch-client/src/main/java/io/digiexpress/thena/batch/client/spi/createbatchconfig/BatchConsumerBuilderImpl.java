package io.digiexpress.thena.batch.client.spi.createbatchconfig;

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
import java.util.List;
import java.util.Optional;

import io.digiexpress.thena.batch.client.api.BatchClient.BatchConsumerBuilder;
import io.digiexpress.thena.batch.client.api.entities.BatchConsumer;
import io.digiexpress.thena.batch.client.api.entities.BatchStatus;
import io.digiexpress.thena.batch.client.api.entities.ImmutableBatchConsumer;
import io.digiexpress.thena.batch.client.api.executor.Executor;
import io.digiexpress.thena.batch.client.api.persistence.BatchDbBuilder.BatchTransactionEntries;
import io.digiexpress.thena.batch.client.spi.createbatchconfig.CMB_Logger.CBE_LogEventType;
import io.resys.thena.support.OidUtils;
import io.resys.thena.support.RepoAssert;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;


@RequiredArgsConstructor
@Data @Accessors(fluent = true)
public class BatchConsumerBuilderImpl implements BatchConsumerBuilder {
  private final CMB_Context ctx;
  private final List<BatchConsumer> existing;
  
  private String batchName;
  private String comment;
  private String consumerName;
  private ImmutableBatchConsumer built;
  private BatchTransactionEntries persist;
  private Executor<?, ?> worker;
  
  
  @Override
  public BatchConsumer build(Executor<?, ?> worker) {
    RepoAssert.isTrue(built == null, () -> "build is already called!");
    RepoAssert.notNull(worker, () -> "worker must be defined!");
    RepoAssert.notNull(batchName, () -> "batchName must be defined!");
    RepoAssert.notNull(comment, () -> "comment must be defined!");
    RepoAssert.notNull(worker, () -> "worker must be defined!");
    RepoAssert.notNull(consumerName, () -> "consumerName must be defined!");
    
    this.worker = worker;
    
    final var found = existing.stream()
      .filter(entry -> entry.getBatchName().equals(batchName))
      .filter(entry -> entry.getConsumerName().equals(consumerName))
      .findFirst();
    
    if(found.isEmpty()) {
      this.built = ImmutableBatchConsumer.builder()
          .id(OidUtils.gen())
          .appId(ctx.getAppId())
          .batchName(batchName)
          .consumerName(consumerName)
          .comment(comment)
          .createdAt(ctx.getNow())
          .createdBy(ctx.getUserId())
          .status(BatchStatus.ENABLED)
          .qualifiedJavaName(worker.getClass().getCanonicalName())
          .updatedAt(Optional.empty())
          .updatedBy(Optional.empty())
          .build();
    } else {
      this.built = ImmutableBatchConsumer.builder()
          .from(found.get())
          .qualifiedJavaName(worker.getClass().getCanonicalName())
          .updatedAt(OffsetDateTime.now())
          .updatedBy(ctx.getUserId())
          .build();
    }
    
    final var toBeSaved = ctx.createPersistContainer();
    if(found.isEmpty()) {
      ctx.getLogger().append(CBE_LogEventType.BATCH_CONSUMER_CREATED, this.built);
      toBeSaved.addBatchConsumerInserts(this.built);
    } else {
      ctx.getLogger().append(CBE_LogEventType.BATCH_CONSUMER_UPDATED, this.built);
      toBeSaved.addBatchConsumerUpdates(this.built);
    }
    persist = toBeSaved.build();
    return this.built;
  }
  
  public BatchTransactionEntries close() {
    RepoAssert.notNull(built, () -> {
      ctx.getLogger().append(CBE_LogEventType.BATCH_CONSUMER_BUILDER_BUILD_METHOD_NOT_CALLED);
      return "build must be called!";
    });
    return persist;
  }
}
