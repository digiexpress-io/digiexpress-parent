package io.resys.thena.storesql;

import java.util.List;

import io.resys.thena.api.LogConstants;
import io.resys.thena.api.entities.grim.GrimLabel;
import io.resys.thena.datasource.ThenaDataSource;
import io.resys.thena.datasource.ThenaSqlDataSource;
import io.resys.thena.datasource.ThenaSqlDataSourceErrorHandler.SqlFailed;
import io.resys.thena.structures.grim.GrimQueries;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.RowSet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = LogConstants.SHOW_SQL)
@RequiredArgsConstructor
public class GrimQueriesSqlImpl implements GrimQueries {
  private final ThenaSqlDataSource dataSource;
  
  @Override
  public ThenaDataSource getDataSource() {
    return dataSource;
  }

  @Override
  public LabelQuery labels() {
    final var registry = dataSource.getRegistry().grim();
    final var errorHandler = dataSource.getErrorHandler();
    return new LabelQuery() {
      @Override
      public Uni<List<GrimLabel>> findAll() {
        final var sql = registry.labels().findAll();
        if(log.isDebugEnabled()) {
          log.debug("User findAll query, with props: {} \r\n{}", 
              "",
              sql.getValue());
        }
        return dataSource.getClient().preparedQuery(sql.getValue())
            .mapping(registry.labels().defaultMapper())
            .execute()
            .onItem()
            .transformToMulti((RowSet<GrimLabel> rowset) -> Multi.createFrom().iterable(rowset))
            .collect().asList()
            .onFailure().invoke(e -> errorHandler.deadEnd(new SqlFailed("Can't find 'LABEL'!", sql, e)));
      }
    };
  }

  @Override
  public MissionQuery missions() {
    return new GrimMissionContainerQuerySqlImpl(dataSource);
  }
}
