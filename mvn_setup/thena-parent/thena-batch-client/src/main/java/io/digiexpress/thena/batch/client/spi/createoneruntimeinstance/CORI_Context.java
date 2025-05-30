package io.digiexpress.thena.batch.client.spi.createoneruntimeinstance;

import java.time.OffsetDateTime;

import io.digiexpress.thena.batch.client.api.entities.Envelope.OperationStatus;
import io.digiexpress.thena.batch.client.api.persistence.ImmutableBatchTransactionEntries;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor @Data @Builder
public class CORI_Context {

  private final OffsetDateTime now;
  private final String userId;
  private final String appId;
  private final String tenantId;

  private final CORI_Logger logger;
  
  

  public ImmutableBatchTransactionEntries.Builder createPersistContainer() {
    return ImmutableBatchTransactionEntries.builder()
        .tenantId(tenantId)
        .status(OperationStatus.OK)
        .log("");
  }
}
