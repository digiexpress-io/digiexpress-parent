package io.digiexpress.thena.batch.client.api.persistence;

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

import java.util.List;

import org.immutables.value.Value;

import io.digiexpress.thena.batch.client.api.entities.Batch;
import io.digiexpress.thena.batch.client.api.entities.BatchConsumer;
import io.digiexpress.thena.batch.client.api.entities.Envelope.EnvelopeLog;
import io.digiexpress.thena.batch.client.api.entities.Envelope.OperationStatus;
import io.digiexpress.thena.batch.client.api.entities.RuntimeInstance;
import io.digiexpress.thena.batch.client.api.entities.RuntimeLog;
import io.digiexpress.thena.batch.client.api.entities.RuntimeMetric;
import io.digiexpress.thena.batch.client.api.entities.RuntimeParams;
import io.digiexpress.thena.batch.client.api.entities.RuntimeStep;
import io.digiexpress.thena.batch.client.api.entities.RuntimeStepRow;
import io.smallrye.mutiny.Uni;

public interface BatchDbBuilder {
  
  
  Uni<BatchTransactionEntries> persist(BatchTransactionEntries entries);
  
  
  @Value.Immutable
  interface BatchTransactionEntries {
    
    List<String> getCommitMessages();
    List<String> getCommitAuthors();
    
    
    List<Batch> getBatchUpdates();
    List<Batch> getBatchInserts();
    
    List<BatchConsumer> getBatchConsumerUpdates();
    List<BatchConsumer> getBatchConsumerInserts();
    
    List<RuntimeInstance> getRuntimeInstanceInserts();
    List<RuntimeInstance> getRuntimeInstanceUpdates();
    
    List<RuntimeLog> getRuntimeLogInserts();    
    List<RuntimeParams> getRuntimeParamInserts();
    
    List<RuntimeStep> getRuntimeStepInserts();
    List<RuntimeStep> getRuntimeStepUpdates();
    
    List<RuntimeStepRow> getRuntimeStepRowInserts();
    
    List<RuntimeMetric> getRuntimeMetricInserts();
    List<RuntimeMetric> getRuntimeMetricUpdates();
    
    // meta
    String getTenantId();    
    OperationStatus getStatus();
    String getLog();
    List<EnvelopeLog> getMessages();
    
    
    
    default BatchTransactionEntries merge(List<BatchTransactionEntries> src) {
      final var builder = ImmutableBatchTransactionEntries.builder().from(this);
      src.forEach(entry -> entry.merge(builder));
      return builder.build();
    }
    
    
    default BatchTransactionEntries merge(BatchTransactionEntries src) {
      return merge(ImmutableBatchTransactionEntries.builder().from(src)).build();
    }
    
    // merge this to builder
    default ImmutableBatchTransactionEntries.Builder merge(ImmutableBatchTransactionEntries.Builder target) {
      return target
          .addAllBatchConsumerInserts(this.getBatchConsumerInserts())
          .addAllBatchConsumerUpdates(this.getBatchConsumerUpdates())
          
          .addAllBatchInserts(this.getBatchInserts())
          .addAllBatchUpdates(this.getBatchUpdates())
          
          .addAllRuntimeParamInserts(this.getRuntimeParamInserts())
          .addAllRuntimeInstanceInserts(this.getRuntimeInstanceInserts())
          
          .addAllRuntimeInstanceUpdates(this.getRuntimeInstanceInserts())
          .addAllRuntimeLogInserts(this.getRuntimeLogInserts())
          
          .addAllRuntimeMetricUpdates(this.getRuntimeMetricUpdates())
          .addAllRuntimeMetricInserts(this.getRuntimeMetricInserts())
          
          
          .addAllRuntimeStepUpdates(this.getRuntimeStepUpdates())
          .addAllRuntimeStepInserts(this.getRuntimeStepInserts())
          
          .addAllRuntimeStepRowInserts(this.getRuntimeStepRowInserts())
          
          .addAllCommitMessages(this.getCommitMessages())
          .addAllCommitAuthors(this.getCommitAuthors());
    }
  }
}
