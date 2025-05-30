package io.digiexpress.thena.batch.client.spi.persistence.sql;

import io.digiexpress.thena.batch.client.api.entities.ImmutableEnvelopeLog;
import io.digiexpress.thena.batch.client.api.entities.Envelope.OperationStatus;
import io.digiexpress.thena.batch.client.api.persistence.ImmutableBatchTransactionEntries;
import io.digiexpress.thena.batch.client.api.persistence.BatchDbBuilder.BatchTransactionEntries;


public class BatchTransactionException extends RuntimeException {
  private static final long serialVersionUID = -7251738425609399151L;
  private final BatchTransactionEntries batch;
  
  public BatchTransactionException(BatchTransactionEntries current, String msg, Throwable t) {
    this.batch = ImmutableBatchTransactionEntries.builder()
        .from(current)
        .status(OperationStatus.ERROR)
        .addMessages(ImmutableEnvelopeLog.builder().text(msg).exception(t).build())
        .addMessages(ImmutableEnvelopeLog.builder().text(t.getMessage()).build())
        .build(); 
  }
  
  public BatchTransactionException(BatchTransactionEntries batch) {
    this.batch = batch;
  }
  public BatchTransactionEntries getBatch() {
    return batch;
  }
}