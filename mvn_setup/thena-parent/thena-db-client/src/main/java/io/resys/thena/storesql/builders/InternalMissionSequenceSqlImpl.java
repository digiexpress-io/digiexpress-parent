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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.resys.thena.api.LogConstants;
import io.resys.thena.api.registry.GrimRegistry;
import io.resys.thena.datasource.ThenaSqlDataSource;
import io.resys.thena.datasource.ThenaSqlDataSourceErrorHandler;
import io.resys.thena.structures.grim.GrimQueries;
import io.smallrye.mutiny.Uni;
import lombok.extern.slf4j.Slf4j;


@Slf4j(topic = LogConstants.SHOW_SQL)
public class InternalMissionSequenceSqlImpl implements GrimQueries.InternalMissionSequence {

  private final ThenaSqlDataSource dataSource;
  private final GrimRegistry registry;
  private final ThenaSqlDataSourceErrorHandler errorHandler;
  
  public InternalMissionSequenceSqlImpl(ThenaSqlDataSource dataSource) {
    super();
    this.dataSource = dataSource;
    this.registry = dataSource.getRegistry().grim();
    this.errorHandler = dataSource.getErrorHandler();
  }

  @Override
  public Uni<Long> nextVal() {
    final var sql = registry.missions().getNextRefSequence();
    if(log.isDebugEnabled()) {
      log.debug("User nextVal query, with props: {} \r\n{}", 
          "",
          sql.getValue());
    }
    return dataSource.getClient().preparedQuery(sql.getValue())
        .mapping(row -> row.getLong(0))
        .execute()
        .onItem()
        .transform(rowset -> {
          final var it = rowset.iterator();
          if(it.hasNext()) {
            return it.next();
          }
          return null;
        })
        .onFailure().invoke(e -> errorHandler.deadEnd(sql.failed(e, "Can't find next mission ref sequence!")));
  }
  @Override
  public Uni<List<Long>> nextVal(long howMany) {
    final var sql = registry.missions().getNextRefSequence(howMany);
    if(log.isDebugEnabled()) {
      log.debug("User nextVal query, with props: {} \r\n{}", 
          sql.getPropsDeepString(),
          sql.getValue());
    }
    return dataSource.getClient().preparedQuery(sql.getValue())
        .mapping(row -> row.getLong(0))
        .execute(sql.getProps())
        .onItem()
        .transform(rowset -> {
          
          final var it = rowset.iterator();
          final var result = new ArrayList<Long>();
          while(it.hasNext()) {
            result.add(it.next());
          }
          return Collections.unmodifiableList(result);
        })
        .onFailure().invoke(e -> errorHandler.deadEnd(sql.failed(e, "Can't find next mission ref sequence: %s!", howMany)));
  }
}
