package io.digiexpress.thena.batch.client.spi.createbatchconfig;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.google.common.collect.ImmutableList;

import io.digiexpress.thena.batch.client.api.BatchClient.BatchBuilder;
import io.digiexpress.thena.batch.client.api.BatchClient.BatchConsumerBuilder;
import io.digiexpress.thena.batch.client.api.BatchClient.CreateBatchConfig;
import io.digiexpress.thena.batch.client.api.entities.Batch;
import io.digiexpress.thena.batch.client.api.entities.BatchConfig;
import io.digiexpress.thena.batch.client.api.entities.BatchConsumer;
import io.digiexpress.thena.batch.client.api.entities.Envelope;
import io.digiexpress.thena.batch.client.api.entities.Envelope.OperationStatus;
import io.digiexpress.thena.batch.client.api.entities.ImmutableEnvelope;
import io.digiexpress.thena.batch.client.api.entities.ImmutableEnvelopeLog;
import io.digiexpress.thena.batch.client.api.executor.Executor;
import io.digiexpress.thena.batch.client.api.persistence.BatchDb;
import io.digiexpress.thena.batch.client.api.persistence.BatchDbBuilder.BatchTransactionEntries;
import io.resys.thena.spi.ImmutableTxScope;
import io.resys.thena.support.RepoAssert;
import io.smallrye.mutiny.Uni;
import lombok.Data;
import lombok.experimental.Accessors;



@Data @Accessors(fluent = true)
public class CreateBatchConfigImpl implements CreateBatchConfig {
  private final BatchDb batchDb;
  private final CMB_Logger logger;
  
  private String appId;
  private String commitMessage;
  private String commitAuthor;
  
  private final List<Consumer<BatchBuilder>> batches = new ArrayList<>();
  private final List<Consumer<BatchConsumerBuilder>> consumers = new ArrayList<>();
  private final List<Executor<?, ?>> executors = new ArrayList<>();
  
  
  public CreateBatchConfigImpl(BatchDb batchDb) {
    super();
    this.batchDb = batchDb;
    this.logger = new CMB_Logger();
  }
  
  @Override
  public CreateBatchConfig appId(String appId) {
    this.appId = appId;
    return this;
  }
  @Override
  public CreateBatchConfig commitMessage(String commitMessage) {
    this.commitMessage = commitMessage;
    return this;
  }
  @Override
  public CreateBatchConfig commitAuthor(String commitAuthor) {
    this.commitAuthor = commitAuthor;
    return this;
  }
  @Override
  public CreateBatchConfig addBatch(Consumer<BatchBuilder> consumer) {
    batches.add(consumer);
    return this;
  }
  @Override
  public CreateBatchConfig addConsumer(Consumer<BatchConsumerBuilder> consumerBuilder) {
    consumers.add(consumerBuilder);
    return this;
  }
  @Override
  public Uni<Envelope<BatchConfig>> build() {
    RepoAssert.notBlank(appId, () -> "appId must be defined!");
    RepoAssert.notBlank(commitAuthor, () -> "commitAuthor must be defined!");
    RepoAssert.notBlank(commitMessage, () -> "commitMessage must be defined!");
    
    return batchDb.withTenant().onItem().transformToUni(db -> execute(db));
  }
  
  public Uni<Envelope<BatchConfig>> execute(BatchDb batchDb) {
    final var scope = ImmutableTxScope.builder()
        .tenantId(batchDb.getDataSource().getTenant().getId())
        .commitAuthor(commitAuthor)
        .commitMessage(commitMessage)
        .build();
    
    return batchDb
        .withTransaction(scope, db -> {
          return createRequest(db)
              .onItem().transformToUni(request -> db.builder().persist(request))
              .onItem().transform(persisted -> createResponse(persisted, db));
        })
        .onFailure().recoverWithItem(throwable -> {
          logger.fail(throwable);
          return ImmutableEnvelope.<BatchConfig>builder()
            .tenantId(scope.getTenantId())
            .addOperationLogs(ImmutableEnvelopeLog.builder()
                .text(new StringBuilder()
                  .append("Batch commit to: '").append(scope.getTenantId()).append("'").append(" is rejected.")
                  .append(System.lineSeparator())
                  .append("Message: ").append(throwable.getMessage())
                  .toString())
                .exception(throwable)
                .build())
            .operationStatus(OperationStatus.ERROR)
          .build();
        })
        .invoke(envelope -> {
          logger.close();
        });
  }

  
  private Envelope<BatchConfig> createResponse(BatchTransactionEntries rsp, BatchDb db) {
    if(rsp.getStatus() == OperationStatus.CONFLICT || rsp.getStatus() == OperationStatus.ERROR) {
      throw new CreateBatchConfigException("Failed to create batch envir!", rsp);
    }
    
    final Envelope<BatchConfig> result = ImmutableEnvelope.<BatchConfig>builder()
        .tenantId(db.getDataSource().getTenant().getId())
        .operationStatus(rsp.getStatus())
        .object(new BatchConfigImpl(
            appId,
            ImmutableList.<Batch>builder()
              .addAll(rsp.getBatchInserts())
              .addAll(rsp.getBatchUpdates())
              .build(),
            ImmutableList.<BatchConsumer>builder()
              .addAll(rsp.getBatchConsumerInserts())
              .addAll(rsp.getBatchConsumerUpdates())
              .build(),
            ImmutableList.<Executor<?, ?>>builder()
              .addAll(executors)
              .build()
        ))
        .build();
    return result;
  }
  
  private Uni<BatchTransactionEntries> createRequest(BatchDb db) {
    return Uni.combine().all().unis(
        db.query().queryBatches().findAllByAppId(appId, true),
        db.query().queryBatchConsumers().findAllByAppId(appId, true))
    .asTuple().onItem().transform(tuple -> {
      
      
      final var ctx = CMB_Context.builder()
          .appId(appId)
          .logger(logger)
          .userId(commitAuthor)
          .tenantId(db.getDataSource().getTenant().getId())
          .now(OffsetDateTime.now())
          .build();
      
      final var txEntries = ctx.createPersistContainer()
          .addCommitMessages(commitMessage)
          .addCommitAuthors(commitAuthor);

        
      batches.stream()
        .map(c -> {
          final var builder = new BatchBuilderImpl(ctx, tuple.getItem1());
          c.accept(builder);
          return builder.close();
        })
        .forEach(c -> c.merge(txEntries));
     
      consumers.stream()
        .map(c -> {
          final var builder = new BatchConsumerBuilderImpl(ctx, tuple.getItem2());
          c.accept(builder);
          this.executors.add(builder.worker());
          return builder.close();
        })
        .forEach(c -> c.merge(txEntries));
      
      
      return txEntries.build();
    });
  }
}
