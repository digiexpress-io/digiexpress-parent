package io.resys.thena.docdb.sql.queries;

/*-
 * #%L
 * thena-docdb-api
 * %%
 * Copyright (C) 2021 - 2023 Copyright 2021 ReSys OÜ
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
import java.util.Arrays;
import java.util.List;

import io.resys.thena.docdb.api.LogConstants;
import io.resys.thena.docdb.api.actions.PullActions.MatchCriteria;
import io.resys.thena.docdb.api.models.ThenaObject.BlobHistory;
import io.resys.thena.docdb.spi.ClientQuery.BlobHistoryQuery;
import io.resys.thena.docdb.sql.factories.ClientQuerySqlPool.ClientQuerySqlContext;
import io.smallrye.mutiny.Multi;
import io.vertx.mutiny.sqlclient.RowSet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = LogConstants.SHOW_SQL)
@RequiredArgsConstructor
public class BlobHistoryQuerySqlPool implements BlobHistoryQuery {

  private final ClientQuerySqlContext context;
  private boolean latestOnly;
  private String name;
  private List<MatchCriteria> criteria = new ArrayList<>();

  @Override public BlobHistoryQuery latestOnly(boolean latestOnly) { this.latestOnly = latestOnly; return this; }
  @Override public BlobHistoryQuery blobName(String name) { this.name = name; return this; }
  @Override public BlobHistoryQuery criteria(List<MatchCriteria> criteria) { this.criteria.addAll(criteria); return this; }
  @Override public BlobHistoryQuery criteria(MatchCriteria... criteria) { this.criteria.addAll(Arrays.asList(criteria)); return this; }
  
  @Override
  public Multi<BlobHistory> find() {
    final var sql = context.getBuilder().blobs().find(name, latestOnly, criteria);
    final var stream = context.getWrapper().getClient().preparedQuery(sql.getValue())
        .mapping(row -> context.getMapper().blobHistory(row));
    
    if(log.isDebugEnabled()) {
      log.debug("Blob history query, with props: {} \r\n{}", 
          sql.getProps().deepToString(), 
          sql.getValue());
    }
    
    return (sql.getProps().size() > 0 ? stream.execute(sql.getProps()) : stream.execute())
        .onItem()
        .transformToMulti((RowSet<BlobHistory> rowset) -> Multi.createFrom().iterable(rowset))
        .onFailure().invoke(e -> context.getErrorHandler().deadEnd(
          new StringBuilder("Can't find 'BLOB'-s by 'name': '").append(name).append("'!").toString() 
          , e));
  }
  
}
