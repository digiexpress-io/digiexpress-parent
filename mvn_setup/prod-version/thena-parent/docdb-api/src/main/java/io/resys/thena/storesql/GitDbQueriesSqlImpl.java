package io.resys.thena.storesql;

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

import io.resys.thena.datasource.ThenaSqlDataSource;
import io.resys.thena.storesql.builders.GitBlobHistoryQuerySqlPool;
import io.resys.thena.storesql.builders.GitBlobQuerySqlPool;
import io.resys.thena.storesql.builders.GitCommitQuerySqlPool;
import io.resys.thena.storesql.builders.GitRefQuerySqlPool;
import io.resys.thena.storesql.builders.GitTagQuerySqlPool;
import io.resys.thena.storesql.builders.GitTreeQuerySqlPool;
import io.resys.thena.structures.git.GitQueries;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class GitDbQueriesSqlImpl implements GitQueries {
  private final ThenaSqlDataSource dataSource;
  
  @Override
  public GitTagQuery tags() {
    return new GitTagQuerySqlPool(dataSource);
  }
  @Override
  public GitCommitQuery commits() {
    return new GitCommitQuerySqlPool(dataSource);
  }
  @Override
  public GitRefQuery refs() {
    return new GitRefQuerySqlPool(dataSource);
  }
  @Override
  public GitTreeQuery trees() {
    return new GitTreeQuerySqlPool(dataSource);
  }
  @Override
  public GitBlobQuery blobs() {
    return new GitBlobQuerySqlPool(dataSource);
  }
  @Override
  public GitBlobHistoryQuery blobHistory() {
    return new GitBlobHistoryQuerySqlPool(dataSource);
  }
}
