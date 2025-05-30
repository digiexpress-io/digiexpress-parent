package io.digiexpress.thena.batch.client.spi.createbatchconfig;

import java.util.List;
import java.util.Optional;

import io.digiexpress.thena.batch.client.api.BatchClient.BatchBuilder;
import io.digiexpress.thena.batch.client.api.entities.Batch;
import io.digiexpress.thena.batch.client.api.entities.BatchStatus;
import io.digiexpress.thena.batch.client.api.entities.ImmutableBatch;
import io.digiexpress.thena.batch.client.api.persistence.BatchDbBuilder.BatchTransactionEntries;
import io.digiexpress.thena.batch.client.spi.createbatchconfig.CMB_Logger.CBE_LogEventType;
import io.resys.thena.support.OidUtils;
import io.resys.thena.support.RepoAssert;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;



@RequiredArgsConstructor
@Data @Accessors(fluent = true)
public class BatchBuilderImpl implements BatchBuilder {
  private final CMB_Context ctx;
  private final List<Batch> existing;
  
  private String batchName;
  private String comment;
  private String externalId;
  
  private Batch built;
  private BatchTransactionEntries persist;
  
  
  @Override
  public Batch build() {
    RepoAssert.isTrue(built == null, () -> "build is already called!");
    
    final var found = existing.stream()
      .filter(entry -> entry.getBatchName().equals(batchName))
      .findFirst();
    
    if(found.isEmpty()) {
      this.built = ImmutableBatch.builder()
          .id(OidUtils.gen())
          .appId(ctx.getAppId())
          .batchName(batchName)
          .comment(comment)
          .externalId(Optional.ofNullable(externalId))
          .createdBy(ctx.getUserId())
          .createdAt(ctx.getNow())
          .status(BatchStatus.ENABLED)
          .build();
    } else {
      this.built = ImmutableBatch.builder()
          .from(found.get())
          .updatedAt(ctx.getNow())
          .updatedBy(ctx.getUserId())
          .batchName(batchName)
          .build();
    }
    
    
    final var toBeSaved = ctx.createPersistContainer();
    if(found.isEmpty()) {
      ctx.getLogger().append(CBE_LogEventType.BATCH_CREATED, this.built);
      toBeSaved.addBatchInserts(this.built);
    } else {
      ctx.getLogger().append(CBE_LogEventType.BATCH_UPDATED, this.built);
      toBeSaved.addBatchUpdates(this.built);
    }
    persist = toBeSaved.build();
    return built;
  }
  
  public BatchTransactionEntries close() {
    RepoAssert.notNull(built, () -> {
      ctx.getLogger().append(CBE_LogEventType.BATCH_BUILDER_BUILD_METHOD_NOT_CALLED);
      return "build must be called!";
    });
    return persist;
  }
}
