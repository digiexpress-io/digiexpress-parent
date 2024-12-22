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
import io.resys.thena.storesql.builders.InternalMissionContainerQuerySqlImpl;
import io.resys.thena.storesql.builders.InternalMissionLabelSqlImpl;
import io.resys.thena.storesql.builders.InternalMissionRemarkQuerySqlImpl;
import io.resys.thena.storesql.builders.InternalMissionSequenceSqlImpl;
import io.resys.thena.storesql.builders.InternalViewerQuerySqlImpl;
import io.resys.thena.structures.grim.GrimQueries;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class GrimQueriesSqlImpl implements GrimQueries {
  private final ThenaSqlDataSource dataSource;
  
  @Override
  public ThenaDataSource getDataSource() {
    return dataSource;
  }
  @Override
  public InternalMissionQuery missions() {
    return new InternalMissionContainerQuerySqlImpl(dataSource);
  }
  @Override
  public InternalCommitViewerQuery commitViewer() {
    return new InternalViewerQuerySqlImpl(dataSource);
  }
  @Override
  public InternalMissionSequence missionSequences() {
    return new InternalMissionSequenceSqlImpl(dataSource);
  }
  @Override
  public InternalMissionLabelQuery missionLabels() {
    return new InternalMissionLabelSqlImpl(dataSource);
  }
  @Override
  public InternalMissionRemarkQuery missionRemarks() {
    return new InternalMissionRemarkQuerySqlImpl(dataSource);
  }

}
