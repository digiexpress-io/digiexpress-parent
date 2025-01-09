package io.resys.thena.storesql.builders;

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

import io.resys.thena.api.LogConstants;
import io.resys.thena.api.entities.doc.Doc.DocFilter;
import io.resys.thena.api.entities.doc.DocCommands;
import io.resys.thena.api.registry.DocRegistry;
import io.resys.thena.datasource.ThenaSqlDataSource;
import io.resys.thena.datasource.ThenaSqlDataSourceErrorHandler;
import io.resys.thena.datasource.ThenaSqlDataSourceErrorHandler.SqlFailed;
import io.resys.thena.datasource.ThenaSqlDataSourceErrorHandler.SqlTupleFailed;
import io.resys.thena.structures.doc.DocQueries.DocCommandsQuery;
import io.smallrye.mutiny.Multi;
import io.vertx.mutiny.sqlclient.RowSet;
import lombok.extern.slf4j.Slf4j;


@Slf4j(topic = LogConstants.SHOW_SQL)
public class DocCommandsQuerySqlPool implements DocCommandsQuery {
  private final ThenaSqlDataSource wrapper;
  private final DocRegistry registry;
  private final ThenaSqlDataSourceErrorHandler errorHandler;
  
  public DocCommandsQuerySqlPool(ThenaSqlDataSource dataSource) {
    this.wrapper = dataSource;
    this.registry = dataSource.getRegistry().doc();
    this.errorHandler = dataSource.getErrorHandler();
  }
  @Override
  public Multi<DocCommands> findAll(DocFilter filter) {
    final var sql = registry.docCommands().findAll(filter);
    if(log.isDebugEnabled()) {
      log.debug("DocCommands byId query, with props: {} \r\n{}", 
          sql.getProps().deepToString(),
          sql.getValue());
    }
    return wrapper.getClient().preparedQuery(sql.getValue())
        .mapping(registry.docCommands().defaultMapper())
        .execute(sql.getProps())
        .onItem()
        .transformToMulti((RowSet<DocCommands> rowset) -> Multi.createFrom().iterable(rowset))
        .onFailure().invoke(e -> errorHandler.deadEnd(new SqlTupleFailed("Can't get 'DOC_COMMANDS' for filter: '" + filter + "'!", sql, e)));
  }
  @Override
  public Multi<DocCommands> findAll() {
    final var sql = registry.docCommands().findAll();
    if(log.isDebugEnabled()) {
      log.debug("DocCommands findAll query, with props: {} \r\n{}", 
          "",
          sql.getValue());
    }
    return wrapper.getClient().preparedQuery(sql.getValue())
        .mapping(registry.docCommands().defaultMapper())
        .execute()
        .onItem()
        .transformToMulti((RowSet<DocCommands> rowset) -> Multi.createFrom().iterable(rowset))
        .onFailure().invoke(e -> errorHandler.deadEnd(new SqlFailed("Can't find 'DOC_COMMANDS'!", sql, e)));
  }
}
