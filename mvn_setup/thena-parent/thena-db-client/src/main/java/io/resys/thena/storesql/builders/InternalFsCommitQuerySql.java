package io.resys.thena.storesql.builders;

import java.util.Arrays;

/*-
 * #%L
 * thena-docdb-api
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

import io.resys.thena.api.LogConstants;
import io.resys.thena.api.entities.fs.FsCommit;
import io.resys.thena.api.entities.fs.ThenaFsObject.FsDocType;
import io.resys.thena.api.registry.FsRegistry;
import io.resys.thena.api.registry.fs.ImmutableFsDirentFilter;
import io.resys.thena.datasource.ThenaSqlDataSource;
import io.resys.thena.datasource.ThenaSqlDataSourceErrorHandler;
import io.resys.thena.registry.fs.FsRegistrySqlImpl;
import io.resys.thena.structures.fs.FsQueries;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.RowSet;
import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = LogConstants.SHOW_SQL)
public class InternalFsCommitQuerySql implements FsQueries.InternalCommitQuery {
  private final ThenaSqlDataSource dataSource;
  private final FsRegistry registry;
  private final ThenaSqlDataSourceErrorHandler errorHandler;
  
  public InternalFsCommitQuerySql(ThenaSqlDataSource dataSource) {
    super();
    this.dataSource = dataSource;
    this.registry = new FsRegistrySqlImpl(dataSource.getRegistry());
    this.errorHandler = dataSource.getErrorHandler();
  }

  @Override
  public Uni<List<FsCommit>> findAllByDirentId(String direntId) {
    
    final var sql = registry.commits().findAll(ImmutableFsDirentFilter.builder().direntIds(Arrays.asList(direntId)).build());
    if(log.isDebugEnabled()) {
      log.debug("User findAllByDirentId query, with props: {} \r\n{}", 
          sql.getPropsDeepString(),
          sql.getValue());
    }
    return dataSource.getClient().preparedQuery(sql.getValue())
        .mapping(registry.commits().defaultMapper())
        .execute(sql.getProps())
        .onItem()
        .transformToMulti(RowSet::toMulti)
        .collect().asList()
        .onFailure().invoke(e -> errorHandler.deadEnd(sql.failed(e, "Can't find '%s'!", FsDocType.FS_COMMIT)));
  }
}
