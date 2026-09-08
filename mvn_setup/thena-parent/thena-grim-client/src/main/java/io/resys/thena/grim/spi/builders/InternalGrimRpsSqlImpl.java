package io.resys.thena.grim.spi.builders;

/*-
 * #%L
 * thena-grim-client
 * %%
 * Copyright (C) 2015 - 2026 Copyright 2022 ReSys OÜ
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

import io.resys.thena.api.LogConstants;
import io.resys.thena.api.entities.grim.GrimRps;
import io.resys.thena.datasource.ThenaSqlDataSource;
import io.resys.thena.datasource.ThenaSqlDataSourceErrorHandler;
import io.resys.thena.grim.spi.GrimDataSource.InternalRpsQuery;
import io.resys.thena.grim.spi.datasource.GrimRegistry;
import io.resys.thena.grim.spi.datasource.GrimRegistrySqlImpl;
import io.smallrye.mutiny.Multi;
import io.vertx.mutiny.sqlclient.RowSet;
import lombok.extern.slf4j.Slf4j;


@Slf4j(topic = LogConstants.SHOW_SQL)
public class InternalGrimRpsSqlImpl implements InternalRpsQuery {

  private final ThenaSqlDataSource dataSource;
  private final GrimRegistry registry;
  private final ThenaSqlDataSourceErrorHandler errorHandler;
  
  public InternalGrimRpsSqlImpl(ThenaSqlDataSource dataSource) {
    super();
    this.dataSource = dataSource;
    this.registry = new GrimRegistrySqlImpl(dataSource.getRegistry());
    this.errorHandler = dataSource.getErrorHandler();
  }

  @Override
  public Multi<GrimRps> findAll() {
    final var sql = registry.rps().findAll();
    if(log.isDebugEnabled()) {
      log.debug("InternalGrimRpsSqlImpl:findAll query\r\n{}", 
          sql.getValue());
    }
    return dataSource.getClient().preparedQuery(sql.getValue())
        .mapping(registry.rps().defaultMapper())
        .execute()
        .onItem()
        .transformToMulti(RowSet::toMulti)
        .onFailure().invoke(e -> errorHandler.deadEnd(sql.failed(e, "Can't find rps!")));
  }
}
