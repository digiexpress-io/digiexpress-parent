package io.digiexpress.thena.mq.client.spi.persistence;

/*-
 * #%L
 * thena-mq-client
 * %%
 * Copyright (C) 2015 - 2025 Copyright 2022 ReSys OÜ
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
import io.digiexpress.thena.mq.client.api.entities.Delivery.DeliveryStatus;
import io.digiexpress.thena.mq.client.api.entities.QueueMessage;
import io.digiexpress.thena.mq.client.api.entities.QueueMessage.QueueMessageStatus;
import io.digiexpress.thena.mq.client.api.persistence.ThenaMqChannelState.InternalMessageQuery;
import io.digiexpress.thena.mq.client.api.persistence.ThenaMqDataSource;
import io.resys.thena.datasource.ThenaSqlDataSourceErrorHandler.SqlTupleFailed;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.RowSet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = ThenaMqLogConstants.SHOW_SQL)
@RequiredArgsConstructor
public class InternalMessageQueryImpl implements InternalMessageQuery {
 
  private final ThenaMqDataSource dataSource;
  
  @Override
  public Uni<List<QueueMessage>> findAllByStatus(QueueMessageStatus status, boolean lockForUpdate) {
    final var sql = dataSource.getRegistry().message().findAllByStatus(status, lockForUpdate);
    
    if(log.isDebugEnabled()) {
      log.debug("InternalMessageQueryImpl.findAllByRoutingStatus query, with props: {} \r\n{}", 
          sql.getProps().deepToString(), 
          sql.getValue());
    }
    
    return dataSource.getClient().preparedQuery(sql.getValue())
        .mapping(dataSource.getRegistry().message().defaultMapper())
        .execute(sql.getProps())
        .onItem()
        .transformToUni((RowSet<QueueMessage> rowset) -> Multi.createFrom().iterable(rowset).collect().asList())
        .onFailure().invoke(e -> dataSource.getErrorHandler().deadEnd(new SqlTupleFailed("Can't find 'QUEUE_MESSAGE' by 'routing_status'!", sql, e)));
  }

  @Override
  public Uni<List<QueueMessage>> findAllByAppIdAndDeliveryStatus(String appId, DeliveryStatus status) {
    final var sql = dataSource.getRegistry().message().findAllByAppIdAndDeliveryStatus(appId, status);
    
    if(log.isDebugEnabled()) {
      log.debug("InternalMessageQueryImpl.findAllByAppIdAndDeliveryStatus query, with props: {} \r\n{}", 
          sql.getProps().deepToString(), 
          sql.getValue());
    }
    
    return dataSource.getClient().preparedQuery(sql.getValue())
        .mapping(dataSource.getRegistry().message().defaultMapper())
        .execute(sql.getProps())
        .onItem()
        .transformToUni((RowSet<QueueMessage> rowset) -> Multi.createFrom().iterable(rowset).collect().asList())
        .onFailure().invoke(e -> dataSource.getErrorHandler().deadEnd(new SqlTupleFailed("Can't find 'QUEUE_MESSAGE' by 'app_id' and 'delivery.status'!", sql, e)));
  }
  
  @Override
  public Uni<List<QueueMessage>> findLastNEntries(long entries) {
   final var sql = dataSource.getRegistry().message().findLastNEntries(entries);
    
    if(log.isDebugEnabled()) {
      log.debug("InternalMessageQueryImpl.findLastNEntries query, with props: {} \r\n{}", 
          sql.getProps().deepToString(), 
          sql.getValue());
    }
    
    return dataSource.getClient().preparedQuery(sql.getValue())
        .mapping(dataSource.getRegistry().message().defaultMapper())
        .execute(sql.getProps())
        .onItem()
        .transformToUni((RowSet<QueueMessage> rowset) -> Multi.createFrom().iterable(rowset).collect().asList())
        .onFailure().invoke(e -> dataSource.getErrorHandler().deadEnd(new SqlTupleFailed("Can't find last-N 'QUEUE_MESSAGE'!", sql, e)));

  }

  @Override
  public Multi<QueueMessage> findAllByBodyId(String bodyId) {
   final var sql = dataSource.getRegistry().message().findAllByBodyId(bodyId);
    
    if(log.isDebugEnabled()) {
      log.debug("InternalMessageQueryImpl.findAllByBodyId query, with props: {} \r\n{}", 
          sql.getProps().deepToString(), 
          sql.getValue());
    }
    
    return dataSource.getClient().preparedQuery(sql.getValue())
        .mapping(dataSource.getRegistry().message().defaultMapper())
        .execute(sql.getProps())
        .onItem()
        .transformToMulti((RowSet<QueueMessage> rowset) -> Multi.createFrom().iterable(rowset))
        .onFailure().invoke(e -> dataSource.getErrorHandler().deadEnd(new SqlTupleFailed("Can't find last-N 'QUEUE_MESSAGE'!", sql, e)));
  }
}
