package io.resys.thena.storesql.builders;

/*-
 * #%L
 * thena-db-client
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

import java.util.ArrayList;
import java.util.List;

import io.resys.thena.api.LogConstants;
import io.resys.thena.api.entities.git.BlobCommit;
import io.resys.thena.datasource.ThenaSqlDataSource;
import io.resys.thena.datasource.ThenaSqlDataSourceErrorHandler.SqlTupleFailed;
import io.resys.thena.registry.git.GitRegistrySqlImpl;
import io.resys.thena.structures.git.GitQueries.GitBlobCommitQuery;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.RowSet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = LogConstants.SHOW_SQL)
@RequiredArgsConstructor
public class GitBlobCommitQuerySqlPool implements GitBlobCommitQuery {

  private final ThenaSqlDataSource wrapper;
  private boolean includBlob;
  private String branchName;
  
  @Override
  public GitBlobCommitQuery branchName(String branchName) {
    this.branchName = branchName;
    return this;
  }
  @Override
  public GitBlobCommitQuery includBlob(boolean includBlob) {
    this.includBlob = includBlob;
    return this;
  }
  @Override
  public Uni<List<BlobCommit>> findAll() {
    final var registry = new GitRegistrySqlImpl(wrapper.getRegistry()).blobs();
    
    final var sql = registry.findAllBlobCommits(branchName, includBlob);
    if(log.isDebugEnabled()) {
      log.debug("findAllBlobCommits, with props: {} \r\n{}", 
          sql.getProps().deepToString(), 
          sql.getValue());
    }
    return wrapper.getClient().preparedQuery(sql.getValue())
        .mapping(registry.blobCommitMapper())
        .execute(sql.getProps())
        .onItem()
        .transform((RowSet<BlobCommit> rowset) -> {
          List<BlobCommit> result = new ArrayList<BlobCommit>();
          for(final var item : rowset) {
            result.add(item);
          }
          return result;
        })
        .onFailure().invoke(e -> 
        wrapper.getErrorHandler().deadEnd(
              new SqlTupleFailed("Can't find 'BLOB'-s and 'COMMIT'-s for history", sql, e)
        ));
  }

}
