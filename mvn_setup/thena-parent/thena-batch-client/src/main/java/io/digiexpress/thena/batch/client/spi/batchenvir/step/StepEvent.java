package io.digiexpress.thena.batch.client.spi.batchenvir.step;

import java.time.OffsetDateTime;
import java.util.Optional;

import io.digiexpress.thena.batch.client.api.executor.ExecutorEntity;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StepEvent {
  private final OffsetDateTime createdAt;
  private final Object entity;
  private final Object entityConfig;
  private final Optional<ExecutorEntity> processed;
  private final Optional<Throwable> throwable;
  private final Long entityNumber;
  
  
  public boolean isOk() {
    return processed.isPresent();
  }
}
