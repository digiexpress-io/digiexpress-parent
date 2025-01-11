package io.digiexpress.thena.mq.client.spi.visitors;

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
import java.util.Optional;

import io.digiexpress.thena.mq.client.api.ThenaMqLogConstants;
import io.digiexpress.thena.mq.client.api.entities.Binding;
import io.digiexpress.thena.mq.client.api.entities.Channel;
import io.digiexpress.thena.mq.client.api.entities.Delivery;
import io.digiexpress.thena.mq.client.api.entities.Delivery.DeliveryAttempt;
import io.digiexpress.thena.mq.client.api.entities.ImmutableLog;
import io.digiexpress.thena.mq.client.api.entities.Queue;
import io.digiexpress.thena.mq.client.api.entities.QueueConsumer;
import io.digiexpress.thena.mq.client.api.entities.QueueMessage;
import io.digiexpress.thena.mq.client.api.entities.ThenaMqEnvelope.OperationStatus;
import io.digiexpress.thena.mq.client.api.persistence.ImmutableChannelBatch;
import io.digiexpress.thena.mq.client.api.persistence.ThenaMqChannelState.ChannelBatch;
import io.digiexpress.thena.mq.client.api.persistence.ThenaMqChannelState.ChannelBatchException;
import io.digiexpress.thena.mq.client.api.persistence.ThenaMqDataSource;
import io.digiexpress.thena.mq.client.api.persistence.ThenaMqTableRegistry;
import io.resys.thena.datasource.ThenaSqlClient;
import io.resys.thena.datasource.ThenaSqlClient.SqlTupleList;
import io.resys.thena.storesql.support.Execute;
import io.resys.thena.support.RepoAssert;
import io.smallrye.mutiny.Uni;
import lombok.extern.slf4j.Slf4j;



@Slf4j(topic = ThenaMqLogConstants.SHOW_SQL)
public class ChannelBatchVisitor {
  private final ThenaMqDataSource wrapper;
  private final ThenaMqTableRegistry registry;
  private final ThenaSqlClient tx;
  private final StringBuilder txLog = new StringBuilder();
  
  public ChannelBatchVisitor(ThenaMqDataSource dataSource) {
    this.wrapper = dataSource;
    this.registry = dataSource.getRegistry();
    this.tx = wrapper.getClient();
    RepoAssert.isTrue(this.wrapper.getTx().isPresent(), () -> "Transaction must be started!");
  }

  public Uni<ChannelBatch> execute(ChannelBatch inputBatch) {
    return Uni.combine().all()
    		.unis(
    		  visitCreateChannel(inputBatch.getNewChannel()),
    		  visitInsertChannel(inputBatch.getNewChannel()),
    		    
  		    visitInsertQueues(inputBatch),
  		    visitInsertMessages(inputBatch),
  		    visitInsertConsumers(inputBatch),
  		    visitInsertBinding(inputBatch),
  		    visitInsertDelivery(inputBatch),
  		    visitInsertDeliveryAttempts(inputBatch),
  		    
  		    visitModifyConsumer(inputBatch),
  		    visitModifyDelivery(inputBatch),
  		    visitModifyDeliveryAttempt(inputBatch),
  		    visitModifyMsgs(inputBatch)
    		)
    		.with(ChannelBatch.class, (List<ChannelBatch> items) -> visitSuccess(inputBatch, items))
    		.onFailure(ChannelBatchException.class)
    		.recoverWithUni(this::visitError);
  }
  
  private ChannelBatch visitSuccess(ChannelBatch inputBatch, List<ChannelBatch> items) {
    final var msg = System.lineSeparator() + "--- TX LOG" + System.lineSeparator() + txLog;
    if(log.isDebugEnabled()) {
      log.debug(msg);
    }
    
    return ImmutableChannelBatch.builder()
        .from(inputBatch.merge(items))
        .log(msg)
        .build();
  }
  
  private Uni<ChannelBatch> visitError(Throwable ex) {
    final var msg = System.lineSeparator() + "--- TX LOG" + System.lineSeparator() + txLog;
    final var batchError = (ChannelBatchException) ex;
    log.error("Failed to save transaction because of: {},\r\n{}", ex.getMessage(), msg, ex);
    
    return tx.rollback().onItem().transform(junk -> 
      ImmutableChannelBatch.builder()
        .from(batchError.getBatch())
        .log(msg)
        .build()
    );
  }
  
  
  private Uni<ChannelBatch> visitInsertQueues(ChannelBatch inputBatch) {
    final var data = inputBatch.getNewQueues();
    final var sql = registry.queue().insertMany(data);  
    return visitExecution(sql, Queue.class);
  }
  
  private Uni<ChannelBatch> visitInsertMessages(ChannelBatch inputBatch) {
    final var data = inputBatch.getNewPublishedMessages();
    final var sql = registry.message().insertMany(data);  
    return visitExecution(sql, QueueMessage.class);
  }
  
  private Uni<ChannelBatch> visitInsertConsumers(ChannelBatch inputBatch) {
    final var data = inputBatch.getNewQueueConsumer();
    final var sql = registry.queueConsumer().insertMany(data);
    return visitExecution(sql, QueueConsumer.class);
  }
  
  private Uni<ChannelBatch> visitInsertBinding(ChannelBatch inputBatch) {
    final var data = inputBatch.getNewBindings();
    final var sql = registry.binding().insertMany(data);
    return visitExecution(sql, Binding.class);
  }
  
  private Uni<ChannelBatch> visitInsertDelivery(ChannelBatch inputBatch) {
    final var data = inputBatch.getNewDeliveries();
    final var sql = registry.delivery().insertMany(data);
    return visitExecution(sql, Delivery.class);
  }
  
  private Uni<ChannelBatch> visitInsertDeliveryAttempts(ChannelBatch inputBatch) {
    final var data = inputBatch.getNewDeliveryAttempts();
    final var sql = registry.deliveryAttempt().insertMany(data);
    return visitExecution(sql, DeliveryAttempt.class);
  }
  
  private Uni<ChannelBatch> visitModifyConsumer(ChannelBatch inputBatch) {
    final var data = inputBatch.getUpdateQueueConsumer();
    final var sql = registry.queueConsumer().updateMany(data);
    return visitExecution(sql, QueueConsumer.class);
  }
  
  private Uni<ChannelBatch> visitModifyDelivery(ChannelBatch inputBatch) {
    final var data = inputBatch.getUpdateDeliveries();
    final var sql = registry.delivery().updateMany(data);
    return visitExecution(sql, Delivery.class);
  }
  
  private Uni<ChannelBatch> visitModifyDeliveryAttempt(ChannelBatch inputBatch) {
    final var data = inputBatch.getUpdateDeliveryAttempts();
    final var sql = registry.deliveryAttempt().updateMany(data);
    return visitExecution(sql, DeliveryAttempt.class);
  }
  
  private Uni<ChannelBatch> visitModifyMsgs(ChannelBatch inputBatch) {
    final var data = inputBatch.getUpdatePublishedMessages();
    final var sql = registry.message().updateMany(data);
    return visitExecution(sql, QueueMessage.class);
  }

  private Uni<ChannelBatch> visitInsertChannel(Optional<Channel> channel) {
    if(channel.isEmpty()) {
      return Uni.createFrom().nothing();
    }
    
    final var tx = wrapper.getClient();
    final var sql = registry.channel().insertOne(channel.get());
    
    final var batch = ImmutableChannelBatch.builder()
        .channelId(wrapper.getChannel().getId())
        .batchStatus(OperationStatus.OK)
        .log("");
    
    final var type = Channel.class;
    
    return tx.preparedQuery(sql.getValue()).execute(sql.getProps()).onItem()
      .transform(row -> {
        final var text = "Inserted " + (row == null ? 0 : row.rowCount()) + " "  + type.getSimpleName() + " entries";
        final ChannelBatch result = batch.addLogs(ImmutableLog.builder().text(text).build()).build();
        return result;
      })
      .onFailure().transform(t -> {
        final var text = "Failed to insert " + sql.getProps().size() + " "  + type.getSimpleName() + " entries";
        return new ChannelBatchException(batch.build(), text, t);
      });
      
  }
  
  private Uni<ChannelBatch> visitCreateChannel(Optional<Channel> channel) {
    if(channel.isEmpty()) {
      return Uni.createFrom().nothing();
    }
    
    final var tx = wrapper.getClient();
    final var tablesCreate = new StringBuilder()
      .append(registry.channel().createTable().getValue())
      .append(registry.queue().createTable().getValue())
      .append(registry.delivery().createTable().getValue())
      .append(registry.deliveryAttempt().createTable().getValue())
      .append(registry.message().createTable().getValue())
      .append(registry.queueConsumer().createTable().getValue())
      .append(registry.binding().createTable().getValue())
      
      .append(registry.channel().createConstraints().getValue())
      .append(registry.queue().createConstraints().getValue())
      .append(registry.delivery().createConstraints().getValue())
      .append(registry.deliveryAttempt().createConstraints().getValue())
      .append(registry.message().createConstraints().getValue())
      .append(registry.queueConsumer().createConstraints().getValue())
      .append(registry.binding().createConstraints().getValue())
      .toString();
    
    final var batch = ImmutableChannelBatch.builder()
        .channelId(wrapper.getChannel().getId())
        .batchStatus(OperationStatus.OK)
        .log("");

    return tx.query(tablesCreate.toString()).execute().onItem().transform(row -> {
      final var text = "Created " + (row == null ? 0 : row.rowCount()) + " tables";
      final ChannelBatch result = batch.addLogs(ImmutableLog.builder().text(text).build()).build();
      return result;
    })
    .onFailure().transform(t -> {
      final var text = "Failed to create tables";
      return new ChannelBatchException(batch.build(), text, t);
    });

  }

  
  private Uni<ChannelBatch> visitExecution(SqlTupleList sql, Class<?> type) {
    visitTxLog(sql, type);
    
    final var batch = ImmutableChannelBatch.builder()
        .channelId(wrapper.getChannel().getId())
        .batchStatus(OperationStatus.OK)
        .log("");
    
    return Execute.apply(tx, sql).onItem().transform(row -> {
        final var text = "Inserted " + (row == null ? 0 : row.rowCount()) + " "  + type.getSimpleName() + " entries";
        final ChannelBatch result = batch.addLogs(ImmutableLog.builder().text(text).build()).build();
        return result;
      })
      .onFailure().transform(t -> {
        final var text = "Failed to insert " + sql.getProps().size() + " "  + type.getSimpleName() + " entries";
        return new ChannelBatchException(batch.build(), text, t);
      });
  }
  
  
  private void visitTxLog(SqlTupleList sql, Class<?> type) {
    if(sql.getProps().isEmpty()) {
      return;
    }
    
    this.txLog
      .append(System.lineSeparator())
      .append("--- processing ").append(sql.getProps().size()).append(" entries of type: '").append(type.getSimpleName()).append("'")
      .append(System.lineSeparator())
      .append(sql.getPropsDeepString()).append(System.lineSeparator())
      .append(sql.getValue()).append(System.lineSeparator());
  }
}
