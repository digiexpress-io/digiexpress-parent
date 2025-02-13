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

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import io.digiexpress.thena.mq.client.api.ThenaMqLogConstants;
import io.digiexpress.thena.mq.client.api.entities.Queue;
import io.digiexpress.thena.mq.client.api.persistence.ThenaMqChannelState.InternalQueueQuery;
import io.digiexpress.thena.mq.client.api.persistence.ThenaMqDataSource;
import io.resys.thena.datasource.ThenaSqlDataSourceErrorHandler.SqlFailed;
import io.resys.thena.datasource.ThenaSqlDataSourceErrorHandler.SqlTupleFailed;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.RowSet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j(topic = ThenaMqLogConstants.SHOW_SQL)
public class InternalQueueQueryImpl implements InternalQueueQuery {
  private final ThenaMqDataSource dataSource;
  
  @Override
  public Uni<Optional<Queue>> findByQueueName(String queueName) {
    final var sql = dataSource.getRegistry().queue().getByIdOrName(queueName);
    
    if(log.isDebugEnabled()) {
      log.debug("InternalQueueQueryImpl.findByQueueName query, with props: {} \r\n{}", 
          sql.getProps().deepToString(), 
          sql.getValue());
    }
    
    return dataSource.getClient().preparedQuery(sql.getValue())
        .mapping(dataSource.getRegistry().queue().defaultMapper())
        .execute(sql.getProps())
        .onItem()
        .transform((RowSet<Queue> rowset) -> {
          final var it = rowset.iterator();
          if(it.hasNext()) {
            return Optional.ofNullable(it.next());
          }
          return Optional.<Queue>empty();
        })
        .onFailure().invoke(e -> dataSource.getErrorHandler().deadEnd(new SqlTupleFailed("Can't find 'QUEUE' by 'queue_name' or 'id'!", sql, e)));
  }

  @Override
  public Uni<List<Queue>> findAll() {
    
    final var sql = dataSource.getRegistry().queue().findAll();
    
    if(log.isDebugEnabled()) {
      log.debug("InternalQueueQueryImpl.findAll query, with props: {} \r\n{}", 
          "", 
          sql.getValue());
    }
    
    return dataSource.getClient().preparedQuery(sql.getValue())
        .mapping(dataSource.getRegistry().queue().defaultMapper())
        .execute()
        .onItem()
        .transformToUni((RowSet<Queue> rowset) -> Multi.createFrom().iterable(rowset).collect().asList())
        .onFailure(e -> dataSource.getErrorHandler().notFound(e)).recoverWithItem(Collections.emptyList())
        .onFailure().invoke(e -> dataSource.getErrorHandler()
            .deadEnd(new SqlFailed("Can't find all 'QUEUE'-s!", sql, e)));
  }

}
