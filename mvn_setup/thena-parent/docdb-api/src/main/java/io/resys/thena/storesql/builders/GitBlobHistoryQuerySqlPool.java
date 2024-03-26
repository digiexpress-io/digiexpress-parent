package io.resys.thena.storesql.builders;

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

import io.resys.thena.api.LogConstants;
import io.resys.thena.api.actions.PullActions.MatchCriteria;
import io.resys.thena.api.models.ThenaGitObject.BlobHistory;
import io.resys.thena.storesql.GitDbQueriesSqlImpl.ClientQuerySqlContext;
import io.resys.thena.structures.git.GitQueries.GitBlobHistoryQuery;
import io.resys.thena.support.ErrorHandler.SqlTupleFailed;
import io.smallrye.mutiny.Multi;
import io.vertx.mutiny.sqlclient.RowSet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = LogConstants.SHOW_SQL)
@RequiredArgsConstructor
public class GitBlobHistoryQuerySqlPool implements GitBlobHistoryQuery {

  private final ClientQuerySqlContext context;
  private boolean latestOnly;
  private String name;
  private List<MatchCriteria> criteria = new ArrayList<>();

  @Override public GitBlobHistoryQuery latestOnly(boolean latestOnly) { this.latestOnly = latestOnly; return this; }
  @Override public GitBlobHistoryQuery blobName(String name) { this.name = name; return this; }
  @Override public GitBlobHistoryQuery criteria(List<MatchCriteria> criteria) { this.criteria.addAll(criteria); return this; }
  @Override public GitBlobHistoryQuery criteria(MatchCriteria... criteria) { this.criteria.addAll(Arrays.asList(criteria)); return this; }
  
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
        .onFailure().invoke(e -> 
          context.getErrorHandler().deadEnd(
          		new SqlTupleFailed("Can't find 'BLOB'-s by 'name'", sql, e)
        ));
  }
  
}
