package io.resys.thena.storesql;

import io.resys.thena.datasource.ThenaDataSource;
import io.resys.thena.datasource.ThenaSqlDataSource;
import io.resys.thena.storesql.builders.CommitViewerQuerySqlImpl;
import io.resys.thena.storesql.builders.GrimMissionContainerQuerySqlImpl;
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
    return new GrimMissionContainerQuerySqlImpl(dataSource);
  }
  @Override
  public CommitViewerQuery commitViewer() {
    return new CommitViewerQuerySqlImpl(dataSource);
  }
}
