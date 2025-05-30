package io.digiexpress.thena.batch.client.api.persistence;

import java.util.List;

import org.immutables.value.Value;

import io.digiexpress.thena.batch.client.api.entities.Batch;
import io.digiexpress.thena.batch.client.api.entities.BatchConsumer;
import io.digiexpress.thena.batch.client.api.entities.Envelope.EnvelopeLog;
import io.digiexpress.thena.batch.client.api.entities.Envelope.OperationStatus;
import io.digiexpress.thena.batch.client.api.entities.RuntimeInstance;
import io.digiexpress.thena.batch.client.api.entities.RuntimeLog;
import io.digiexpress.thena.batch.client.api.entities.RuntimeParams;
import io.digiexpress.thena.batch.client.api.entities.RuntimeStep;
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
          
          
          .addAllRuntimeStepUpdates(this.getRuntimeStepUpdates())
          .addAllRuntimeStepInserts(this.getRuntimeStepInserts())
          
          .addAllCommitMessages(this.getCommitMessages())
          .addAllCommitAuthors(this.getCommitAuthors());
    }
  }
}
