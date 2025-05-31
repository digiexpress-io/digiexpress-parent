package io.digiexpress.thena.batch.client.spi.persistence.sql;

import java.util.List;

import io.digiexpress.thena.batch.client.api.BatchLogConstants;
import io.digiexpress.thena.batch.client.api.entities.Batch;
import io.digiexpress.thena.batch.client.api.entities.BatchConsumer;
import io.digiexpress.thena.batch.client.api.entities.Envelope.OperationStatus;
import io.digiexpress.thena.batch.client.api.entities.ImmutableEnvelopeLog;
import io.digiexpress.thena.batch.client.api.entities.RuntimeInstance;
import io.digiexpress.thena.batch.client.api.entities.RuntimeLog;
import io.digiexpress.thena.batch.client.api.entities.RuntimeParams;
import io.digiexpress.thena.batch.client.api.entities.RuntimeStep;
import io.digiexpress.thena.batch.client.api.entities.RuntimeStepRow;
import io.digiexpress.thena.batch.client.api.persistence.BatchDbBuilder;
import io.digiexpress.thena.batch.client.api.persistence.ImmutableBatchTransactionEntries;
import io.digiexpress.thena.batch.client.spi.persistence.BatchTenantRegistry;
import io.resys.thena.datasource.ThenaSqlClient;
import io.resys.thena.datasource.ThenaSqlClient.SqlTupleList;
import io.resys.thena.datasource.ThenaSqlDataSource;
import io.resys.thena.storesql.support.Execute;
import io.resys.thena.support.RepoAssert;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = BatchLogConstants.SHOW_SQL)
@RequiredArgsConstructor
public class BatchDbBuilderImpl implements BatchDbBuilder {

  private final ThenaSqlClient tx;
  private final ThenaSqlDataSource dataSource;
  private final BatchTenantRegistry registry;
  private final StringBuilder txLog = new StringBuilder();
  
  public BatchDbBuilderImpl(ThenaSqlDataSource dataSource) {
    super();
    
    RepoAssert.isTrue(dataSource.getTx().isPresent(), () -> "Transaction must be started!");
    this.dataSource = dataSource;
    this.tx = dataSource.getClient();
    this.registry = new BatchTenantRegistryImpl(dataSource.getRegistry());
  }
  
  @Override
  public Uni<BatchTransactionEntries> persist(BatchTransactionEntries entries) {

    return Uni.combine().all()
        .unis(
          visitInsertBatches(entries),
          visitInsertBatcheConsumers(entries),
          
          visitModifyBatches(entries),
          visitModifyBatcheConsumers(entries),
          
          visitInsertRuntimeInstances(entries),
          visitModifyRuntimeInstances(entries),
          
          visitInsertRuntimeParams(entries),
          visitInsertRuntimeLogs(entries),
          
          visitInsertRuntimeSteps(entries),
          visitModifyRuntimeSteps(entries),
          
          visitInsertRuntimeStepRows(entries)
          
        )
        .with(BatchTransactionEntries.class, (List<BatchTransactionEntries> items) -> visitSuccess(entries, items))
        .onFailure(BatchTransactionException.class)
        .recoverWithUni(this::visitError);
  }
  
  private Uni<BatchTransactionEntries> visitInsertRuntimeStepRows(BatchTransactionEntries inputBatch) {
    final var data = inputBatch.getRuntimeStepRowInserts();
    final var sql = registry.getRuntimeStepRows().insertMany(data);
    return visitExecution(sql, RuntimeStepRow.class);
  }  
  
  private Uni<BatchTransactionEntries> visitModifyRuntimeInstances(BatchTransactionEntries inputBatch) {
    final var data = inputBatch.getRuntimeInstanceUpdates();
    final var sql = registry.getRuntimeInstances().updateMany(data);
    return visitExecution(sql, RuntimeParams.class);
  }  
  
  private Uni<BatchTransactionEntries> visitInsertRuntimeLogs(BatchTransactionEntries inputBatch) {
    final var data = inputBatch.getRuntimeLogInserts();
    final var sql = registry.getRuntimeLogs().insertMany(data);
    return visitExecution(sql, RuntimeLog.class);
  }  
  
  private Uni<BatchTransactionEntries> visitInsertRuntimeSteps(BatchTransactionEntries inputBatch) {
    final var data = inputBatch.getRuntimeStepInserts();
    final var sql = registry.getRuntimeSteps().insertMany(data);
    return visitExecution(sql, RuntimeStep.class);
  }  
  
  private Uni<BatchTransactionEntries> visitModifyRuntimeSteps(BatchTransactionEntries inputBatch) {
    final var data = inputBatch.getRuntimeStepUpdates();
    final var sql = registry.getRuntimeSteps().updateMany(data);
    return visitExecution(sql, RuntimeStep.class);
  }  
  private Uni<BatchTransactionEntries> visitInsertRuntimeParams(BatchTransactionEntries inputBatch) {
    final var data = inputBatch.getRuntimeParamInserts();
    final var sql = registry.getRuntimeParams().insertMany(data);
    return visitExecution(sql, RuntimeParams.class);
  }  
  private Uni<BatchTransactionEntries> visitInsertRuntimeInstances(BatchTransactionEntries inputBatch) {
    final var data = inputBatch.getRuntimeInstanceInserts();
    final var sql = registry.getRuntimeInstances().insertMany(data);
    return visitExecution(sql, RuntimeInstance.class);
  }

  private Uni<BatchTransactionEntries> visitInsertBatches(BatchTransactionEntries inputBatch) {
    final var data = inputBatch.getBatchInserts();
    final var sql = registry.getBatches().insertMany(data);
    return visitExecution(sql, Batch.class);
  }
  
  private Uni<BatchTransactionEntries> visitModifyBatches(BatchTransactionEntries inputBatch) {
    final var data = inputBatch.getBatchUpdates();
    final var sql = registry.getBatches().updateMany(data);
    return visitExecution(sql, Batch.class);
  }
  
  
  private Uni<BatchTransactionEntries> visitInsertBatcheConsumers(BatchTransactionEntries inputBatch) {
    final var data = inputBatch.getBatchConsumerInserts();
    final var sql = registry.getBatchConsumers().insertMany(data);
    return visitExecution(sql, BatchConsumer.class);
  }
  
  private Uni<BatchTransactionEntries> visitModifyBatcheConsumers(BatchTransactionEntries inputBatch) {
    final var data = inputBatch.getBatchConsumerUpdates();
    final var sql = registry.getBatchConsumers().updateMany(data);
    return visitExecution(sql, BatchConsumer.class);
  }
  
  private Uni<BatchTransactionEntries> visitExecution(SqlTupleList sql, Class<?> type) {
    visitTxLog(sql, type);
    
    final var batch = ImmutableBatchTransactionEntries.builder()
        .tenantId(this.dataSource.getTenant().getId())
        .status(OperationStatus.OK)
        .log("");
    
    return Execute.apply(tx, sql).onItem().transform(row -> {
        final var text = "Inserted " + (row == null ? 0 : row.rowCount()) + " "  + type.getSimpleName() + " entries";
        final BatchTransactionEntries result = batch.addMessages(ImmutableEnvelopeLog.builder().text(text).build()).build();
        return result;
      })
      .onFailure().transform(t -> {
        final var text = "Failed to insert " + sql.getProps().size() + " "  + type.getSimpleName() + " entries";
        return new BatchTransactionException(batch.build(), text, t);
      });
  }
  
  
  private void visitTxLog(SqlTupleList sql, Class<?> type) {
    if(sql.getProps().isEmpty()) {
      return;
    }
    
    this.txLog
      .append(System.lineSeparator())
      .append("--- processing ").append(sql.getProps().size()).append(" entries of type: '").append(type.getSimpleName()).append("'")
      .append(sql.getPropsDeepString()).append(System.lineSeparator())
      .append(sql.getValue()).append(System.lineSeparator());
  }
  
  
  private BatchTransactionEntries visitSuccess(BatchTransactionEntries inputBatch, List<BatchTransactionEntries> items) {
    final var msg = System.lineSeparator() + "--- TX LOG" + System.lineSeparator() + txLog;
    if(log.isDebugEnabled()) {
      log.debug(msg);
    }
    return ImmutableBatchTransactionEntries.builder()
        .from(inputBatch.merge(items))
        .log(msg)
        .build();
  }
  
  private Uni<BatchTransactionEntries> visitError(Throwable ex) {
    final var msg = System.lineSeparator() + "--- TX LOG" + System.lineSeparator() + txLog;
    final var batchError = (BatchTransactionException) ex;
    log.error("Failed to save transaction because of: {},\r\n{}", ex.getMessage(), msg, ex);
    
    return tx.rollback().onItem().transform(junk -> 
      ImmutableBatchTransactionEntries.builder()
        .from(batchError.getBatch())
        .log(msg)
        .build()
    );
  }
  
  

}
