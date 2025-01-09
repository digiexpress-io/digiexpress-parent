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
import io.resys.thena.storesql.builders.DocBranchQuerySqlPool;
import io.resys.thena.storesql.builders.DocCommandsQuerySqlPool;
import io.resys.thena.storesql.builders.DocCommitQuerySqlPool;
import io.resys.thena.storesql.builders.DocLogQuerySqlPool;
import io.resys.thena.storesql.builders.DocQuerySqlPool;
import io.resys.thena.structures.doc.DocQueries;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class DocDbQueriesSqlImpl implements DocQueries {
  private final ThenaSqlDataSource dataSource;
  
  @Override
  public DocBranchQuery branches() {
    return new DocBranchQuerySqlPool(dataSource);
  }

  @Override
  public DocQuery docs() {
    return new DocQuerySqlPool(dataSource);
  }

  @Override
  public DocCommitQuery commits() {
    return new DocCommitQuerySqlPool(dataSource);
  }

  @Override
  public DocCommitTreeQuery trees() {
    return new DocLogQuerySqlPool(dataSource);
  }

  @Override
  public DocCommandsQuery commands() {
    return new DocCommandsQuerySqlPool(dataSource);
  }
}
