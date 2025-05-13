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

import io.resys.thena.datasource.ThenaDataSource;
import io.resys.thena.datasource.ThenaSqlDataSource;
import io.resys.thena.storesql.builders.InternalFsCommitQuerySql;
import io.resys.thena.storesql.builders.InternalFsCommitTreeQuerySql;
import io.resys.thena.storesql.builders.InternalFsDirentContainerQuerySql;
import io.resys.thena.storesql.builders.InternalFsDirentLabelSql;
import io.resys.thena.storesql.builders.InternalFsDirentSequenceSql;
import io.resys.thena.structures.fs.FsQueries;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class FsQueriesSqlImpl implements FsQueries {
  private final ThenaSqlDataSource dataSource;
  
  @Override
  public ThenaDataSource getDataSource() {
    return dataSource;
  }
  @Override
  public InternalDirentQuery dirents() {
    return new InternalFsDirentContainerQuerySql(dataSource);
  }
  @Override
  public InternalDirentSequence direntSequences() {
    return new InternalFsDirentSequenceSql(dataSource);
  }
  @Override
  public InternalDirentLabelQuery direntLabels() {
    return new InternalFsDirentLabelSql(dataSource);
  }
  @Override
  public InternalCommitTreeQuery commitTree() {
    return new InternalFsCommitTreeQuerySql(dataSource);
  }
  @Override
  public InternalCommitQuery commit() {
    return new InternalFsCommitQuerySql(dataSource);
  }
}
