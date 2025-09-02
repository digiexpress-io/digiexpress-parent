package io.digiexpress.thena.mq.client.spi;

import java.util.Collections;

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

import io.digiexpress.thena.mq.client.api.ThenaMqClient.QueueQuery;
import io.digiexpress.thena.mq.client.api.ThenaMqLogConstants;
import io.digiexpress.thena.mq.client.api.entities.Queue;
import io.digiexpress.thena.mq.client.api.persistence.ThenaMqChannelState;
import io.resys.thena.datasource.ThenaSqlDataSourceErrorHandler.SqlTupleFailed;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.RowSet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j(topic = ThenaMqLogConstants.SHOW_SQL)
@RequiredArgsConstructor
public class QueueQueryImpl implements QueueQuery {
  private final ThenaMqChannelState state;

 
  @Override
  public Uni<List<Queue>> findAllById(List<String> id) {
    final var dataSource = state.getDataSource();
    
    final var sql = dataSource.getRegistry().queue().findAllByIdOrName(id);
    
    if(log.isDebugEnabled()) {
      log.debug("QueueQueryImpl.findAllById query, with props: {} \r\n{}", 
          id, 
          sql.getValue());
    }
    
    return dataSource.getClient().preparedQuery(sql.getValue())
        .mapping(dataSource.getRegistry().queue().defaultMapper())
        .execute(sql.getProps())
        .onItem()
        .transformToMulti((RowSet<Queue> rowset) -> Multi.createFrom().iterable(rowset)).collect().asList()
        .onFailure(e -> dataSource.getErrorHandler().notFound(e)).recoverWithItem(Collections.emptyList())
        .onFailure().invoke(e -> dataSource.getErrorHandler().deadEnd(new SqlTupleFailed("Can't find 'QUEUE'-s!", sql, e)));
  }


}
