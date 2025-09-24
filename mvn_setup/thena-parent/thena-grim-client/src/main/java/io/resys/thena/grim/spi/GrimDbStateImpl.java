package io.resys.thena.grim.spi;

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
import io.resys.thena.grim.spi.GrimDataSource.GrimBatchForViewers;
import io.resys.thena.grim.spi.GrimDataSource.GrimBatchMissions;
import io.resys.thena.grim.spi.GrimDataSource.GrimState;
import io.resys.thena.grim.spi.GrimDataSource.InternalCommitQuery;
import io.resys.thena.grim.spi.GrimDataSource.InternalCommitTreeQuery;
import io.resys.thena.grim.spi.GrimDataSource.InternalCommitViewerQuery;
import io.resys.thena.grim.spi.GrimDataSource.InternalMissionLabelQuery;
import io.resys.thena.grim.spi.GrimDataSource.InternalMissionQuery;
import io.resys.thena.grim.spi.GrimDataSource.InternalMissionRemarkQuery;
import io.resys.thena.grim.spi.GrimDataSource.InternalMissionSequence;
import io.resys.thena.grim.spi.GrimDataSource.InternalMissionStatsQuery;
import io.resys.thena.grim.spi.GrimDataSource.InternalProcQuery;
import io.resys.thena.grim.spi.GrimDataSource.InternalProcessSequence;
import io.resys.thena.grim.spi.GrimDataSource.TransactionFunction;
import io.resys.thena.grim.spi.builders.InternalCommitQuerySqlImpl;
import io.resys.thena.grim.spi.builders.InternalCommitTreeQuerySqlImpl;
import io.resys.thena.grim.spi.builders.InternalCommitViewerQuerySqlImpl;
import io.resys.thena.grim.spi.builders.InternalGrimInsertsImpl;
import io.resys.thena.grim.spi.builders.InternalMissionContainerQuerySqlImpl;
import io.resys.thena.grim.spi.builders.InternalMissionLabelSqlImpl;
import io.resys.thena.grim.spi.builders.InternalMissionRemarkQuerySqlImpl;
import io.resys.thena.grim.spi.builders.InternalMissionSequenceSqlImpl;
import io.resys.thena.grim.spi.builders.InternalMissionStatsQuerySqlImpl;
import io.resys.thena.grim.spi.builders.InternalProcQueryImpl;
import io.resys.thena.grim.spi.builders.InternalProcessSequenceSqlImpl;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GrimDbStateImpl implements GrimState {
  private final ThenaSqlDataSource dataSource;
  
  @Override
  public <R> Uni<R> withTransaction(TransactionFunction<R> callback) {
    return dataSource.getPool().withTransaction(conn -> {
      return callback.apply(new GrimDbStateImpl(dataSource.withTx(conn)));
    });
  }
  @Override
  public ThenaSqlDataSource getDataSource() {
    return dataSource;
  }
  @Override
  public String getTenantId() {
    return dataSource.getTenant().getId();
  }
  @Override
  public InternalMissionQuery missions() {
    return new InternalMissionContainerQuerySqlImpl(dataSource);
  }
  @Override
  public InternalCommitViewerQuery commitViewer() {
    return new InternalCommitViewerQuerySqlImpl(dataSource);
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
  @Override
  public InternalCommitTreeQuery commitTree() {
    return new InternalCommitTreeQuerySqlImpl(dataSource);
  }
  @Override
  public InternalCommitQuery commit() {
    return new InternalCommitQuerySqlImpl(dataSource);
  }
  @Override
  public InternalMissionStatsQuery missionStats() {
    return new InternalMissionStatsQuerySqlImpl(dataSource);
  }
  @Override
  public Uni<GrimBatchMissions> batchMany(GrimBatchMissions output) {
    return new InternalGrimInsertsImpl(dataSource).batchMany(output);
  }
  @Override
  public Uni<GrimBatchForViewers> batchMany(GrimBatchForViewers output) {
    return new InternalGrimInsertsImpl(dataSource).batchMany(output);
  }
  @Override
  public InternalProcQuery missionProcs() {
    return new InternalProcQueryImpl(dataSource);
  }
  @Override
  public InternalProcessSequence processSequences() {
    return new InternalProcessSequenceSqlImpl(dataSource);
  }
}
