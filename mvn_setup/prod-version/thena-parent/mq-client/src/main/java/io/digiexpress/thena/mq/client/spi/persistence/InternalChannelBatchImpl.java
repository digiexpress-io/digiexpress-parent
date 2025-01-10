package io.digiexpress.thena.mq.client.spi.persistence;

/*-
 * #%L
 * thena-docdb-api
 * %%
 * Copyright (C) 2015 - 2024 Copyright 2022 ReSys OÜ
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

import io.digiexpress.thena.mq.client.api.ThenaMqLogConstants;
import io.digiexpress.thena.mq.client.api.entities.ImmutableLog;
import io.digiexpress.thena.mq.client.api.entities.ThenaMqEnvelope.OperationStatus;
import io.digiexpress.thena.mq.client.api.persistence.ImmutableChannelBatch;
import io.digiexpress.thena.mq.client.api.persistence.ThenaMqChannelState.ChannelBatch;
import io.digiexpress.thena.mq.client.api.persistence.ThenaMqDataSource;
import io.digiexpress.thena.mq.client.api.persistence.ThenaMqTableRegistry;
import io.resys.thena.datasource.ThenaSqlClient.SqlTupleList;
import io.resys.thena.storesql.support.Execute;
import io.resys.thena.support.RepoAssert;
import io.smallrye.mutiny.Uni;
import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = ThenaMqLogConstants.SHOW_SQL)
public class InternalChannelBatchImpl {
  private final ThenaMqDataSource wrapper;
  private final ThenaMqTableRegistry registry;
  
  public InternalChannelBatchImpl(ThenaMqDataSource dataSource) {
    this.wrapper = dataSource;
    this.registry = dataSource.getRegistry();
  }

  public Uni<ChannelBatch> execute(ChannelBatch inputBatch) {
    RepoAssert.isTrue(this.wrapper.getTx().isPresent(), () -> "Transaction must be started!");
    final var tx = wrapper.getClient();

    /* DELETE OPERATIONS
    final var del_assignements = registry.channel().deleteAll(inputBatch.getDeleteAssignments());
    final Uni<GrimBatchMissions> del_assignements_uni = Execute.apply(tx, del_assignements).onItem()
        .transform(row -> successOutput(inputBatch, "Assignments deleted, number of deleted entries: " + + (row == null ? 0 : row.rowCount())))
        .onFailure().transform(e -> failOutput(inputBatch, "Failed to delete assignments \r\n" + inputBatch.getDeleteAssignments(), e));
    */

    
    
    // UPDATE OPERATIONS
    final var update_msgs = registry.message().updateMany(inputBatch.getUpdatePublishedMessages());
    final var update_deliveries = registry.delivery().updateMany(inputBatch.getUpdateDeliveries());
    final var update_delivery_attempts = registry.deliveryAttempt().updateMany(inputBatch.getUpdateDeliveryAttempts());
    final var update_queue_consumer = registry.queueConsumer().updateMany(inputBatch.getUpdateQueueConsumer());

    final Uni<ChannelBatch> update_msgs_uni = Execute.apply(tx, update_msgs).onItem()
        .transform(row -> successOutput(inputBatch, "Queue messages updated, number of updated entries: " + + (row == null ? 0 : row.rowCount())))
        .onFailure().transform(e -> failOutput(inputBatch, "Failed to update queue messages \r\n" + inputBatch.getUpdateDeliveries(), e));

    final Uni<ChannelBatch> upd_delivery_uni = Execute.apply(tx, update_deliveries).onItem()
        .transform(row -> successOutput(inputBatch, "Queue deliveries updated, number of updated entries: " + + (row == null ? 0 : row.rowCount())))
        .onFailure().transform(e -> failOutput(inputBatch, "Failed to update queue deliveries \r\n" + inputBatch.getUpdateDeliveries(), e));

    final Uni<ChannelBatch> upd_delivery_attempt_uni = Execute.apply(tx, update_delivery_attempts).onItem()
        .transform(row -> successOutput(inputBatch, "Queue delivery attempts updated, number of updated entries: " + + (row == null ? 0 : row.rowCount())))
        .onFailure().transform(e -> failOutput(inputBatch, "Failed to update queue delivery attempts \r\n" + inputBatch.getUpdateDeliveryAttempts(), e));

    final Uni<ChannelBatch> upd_consumer_uni = Execute.apply(tx, update_queue_consumer).onItem()
        .transform(row -> successOutput(inputBatch, "Queue consumers updated, number of updated entries: " + + (row == null ? 0 : row.rowCount())))
        .onFailure().transform(e -> failOutput(inputBatch, "Failed to update queue consumers \r\n" + inputBatch.getUpdateQueueConsumer(), e));

    
    // INSERT OPERATIONS
    final var ins_deliveries = registry.delivery().insertMany(inputBatch.getNewDeliveries());
    final var ins_delivery_attempts = registry.deliveryAttempt().insertMany(inputBatch.getNewDeliveryAttempts());
    final var ins_published_messages = registry.message().insertMany(inputBatch.getNewPublishedMessages());
    final var ins_queue_consumer = registry.queueConsumer().insertMany(inputBatch.getNewQueueConsumer());
    final var ins_queues = registry.queue().insertMany(inputBatch.getNewQueues());
    final var ins_binding = registry.binding().insertMany(inputBatch.getNewBindings());
    
    
    final Uni<ChannelBatch> ins_delivery_uni = Execute.apply(tx, ins_deliveries).onItem()
        .transform(row -> successOutput(inputBatch, "Queue deliveries inserted, number of inserted entries: " + + (row == null ? 0 : row.rowCount())))
        .onFailure().transform(e -> failOutput(inputBatch, "Failed to insert queue deliveries \r\n" + inputBatch.getNewDeliveries(), e));

    final Uni<ChannelBatch> ins_delivery_attempts_uni = Execute.apply(tx, ins_delivery_attempts).onItem()
        .transform(row -> successOutput(inputBatch, "Queue delivery attempt inserted, number of inserted entries: " + + (row == null ? 0 : row.rowCount())))
        .onFailure().transform(e -> failOutput(inputBatch, "Failed to insert queue delivery attempts \r\n" + inputBatch.getNewDeliveryAttempts(), e));
    
    final Uni<ChannelBatch> ins_messages_uni = Execute.apply(tx, ins_published_messages).onItem()
        .transform(row -> successOutput(inputBatch, "Queue messages inserted, number of inserted entries: " + + (row == null ? 0 : row.rowCount())))
        .onFailure().transform(e -> failOutput(inputBatch, "Failed to insert queue messages \r\n" + inputBatch.getNewPublishedMessages(), e));
    
    final Uni<ChannelBatch> ins_consumers_uni = Execute.apply(tx, ins_queue_consumer).onItem()
        .transform(row -> successOutput(inputBatch, "Queue consumers inserted, number of inserted entries: inserted, number of inserted entries: " + + (row == null ? 0 : row.rowCount())))
        .onFailure().transform(e -> failOutput(inputBatch, "Failed to insert queue consumers \r\n" + inputBatch.getNewQueueConsumer(), e));

    final Uni<ChannelBatch> ins_queues_uni = Execute.apply(tx, ins_queues).onItem()
        .transform(row -> successOutput(inputBatch, "Queues inserted, number of inserted entries: " + + (row == null ? 0 : row.rowCount())))
        .onFailure().transform(e -> failOutput(inputBatch, "Failed to insert queues \r\n" + inputBatch.getNewQueues(), e));

    final Uni<ChannelBatch> ins_binding_uni = Execute.apply(tx, ins_binding).onItem()
        .transform(row -> successOutput(inputBatch, "Queues bindings inserted, number of inserted entries: " + + (row == null ? 0 : row.rowCount())))
        .onFailure().transform(e -> failOutput(inputBatch, "Failed to insert queue bindings \r\n" + inputBatch.getNewQueues(), e));

    
    if(log.isDebugEnabled()) {

      log.debug(new StringBuilder()
        .append(System.lineSeparator())
        .append("Thena MQ batch TX").append(System.lineSeparator())
        .append(toLog(ins_queues))
      
        .append(toLog(ins_published_messages))
        .append(toLog(ins_queue_consumer))
        .append(toLog(ins_binding))
        
        .append(toLog(ins_deliveries))
        .append(toLog(ins_delivery_attempts))
  
        
        .append(toLog(update_msgs))        
        .append(toLog(update_queue_consumer))
        .append(toLog(update_deliveries))
        .append(toLog(update_delivery_attempts)).toString()
      );
    }

    return Uni.combine().all()
    		.unis(
          ins_queues_uni,
  		    ins_messages_uni,
  		    ins_consumers_uni,
  		    ins_binding_uni,
          ins_delivery_uni,
          ins_delivery_attempts_uni,
  		    
  		    upd_consumer_uni,
  		    upd_delivery_uni,
          upd_delivery_attempt_uni,
          update_msgs_uni
    		 )
    		.with(ChannelBatch.class, (List<ChannelBatch> items) -> merge(inputBatch, items))
    		.onFailure(ChannelBatchException.class)
    		.recoverWithUni((ex) -> {
    		  final var batchError = (ChannelBatchException) ex;
    		  return tx.rollback().onItem().transform(junk -> batchError.getBatch());
    		})
    		;
  }

  
  private String toLog(SqlTupleList data) {
    if(data.getProps().isEmpty()) {
      return "";
    }
    return data.getValue() + data.getPropsDeepString() + System.lineSeparator();
  }
  
  private ChannelBatch merge(ChannelBatch start, List<ChannelBatch> current) {
    final var builder = ImmutableChannelBatch.builder().from(start);
    final var log = new StringBuilder(start.getLog());
    var status = start.getBatchStatus();
    for(final var value : current) {
      if(value == null) {
        continue;
      }
      
      if(status != OperationStatus.ERROR) {
        status = value.getBatchStatus();
      }
      log.append("\r\n\r\n").append(value.getLog());
      builder.addAllLogs(value.getLogs());
    }
    
    return builder.batchStatus(status).build();
  }
  private ChannelBatch successOutput(ChannelBatch current, String msg) {
    return ImmutableChannelBatch.builder()
      .from(current)
      .batchStatus(OperationStatus.OK)
      .addLogs(ImmutableLog.builder().text(msg).build())
      .build();
  }
  
  private ChannelBatchException failOutput(ChannelBatch current, String msg, Throwable t) {
    return new ChannelBatchException(ImmutableChannelBatch.builder()
        .from(current)
        .batchStatus(OperationStatus.ERROR)
        .addLogs(ImmutableLog.builder().text(msg).exception(t).build())
        .addLogs(ImmutableLog.builder().text(t.getMessage()).build())
        .build()); 
  }
  
  public static class ChannelBatchException extends RuntimeException {
    private static final long serialVersionUID = -7251738425609399151L;
    private final ChannelBatch batch;
    
    public ChannelBatchException(ChannelBatch batch) {
      this.batch = batch;
    }
    public ChannelBatch getBatch() {
      return batch;
    }
  }
  
}
