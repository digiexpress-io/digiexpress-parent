package io.digiexpress.thena.batch.client.spi.createoneruntimeinstance;

import io.digiexpress.thena.batch.client.api.persistence.BatchDbBuilder.BatchTransactionEntries;

public class CreateOneRuntimeException extends RuntimeException {
  private static final long serialVersionUID = -6202574733069488724L;
  private final BatchTransactionEntries batch;
  public CreateOneRuntimeException(String message, BatchTransactionEntries batch) {
    super(message + System.lineSeparator() + " " +
        String.join(System.lineSeparator() + " ", batch.getMessages().stream().map(e -> e.getText()).toList()));
    
    batch.getMessages().stream().filter(e -> e.getException() != null).forEach(e -> addSuppressed(e.getException()));
    this.batch = batch;
  }
  public BatchTransactionEntries getBatch() {
    return batch;
  }
}