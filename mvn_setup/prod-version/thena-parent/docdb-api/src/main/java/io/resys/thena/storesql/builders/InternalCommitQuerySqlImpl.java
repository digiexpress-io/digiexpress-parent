package io.resys.thena.storesql.builders;

import java.util.List;

import io.resys.thena.api.LogConstants;
import io.resys.thena.api.entities.grim.GrimCommit;
import io.resys.thena.api.entities.grim.ThenaGrimObject.GrimDocType;
import io.resys.thena.api.registry.GrimRegistry;
import io.resys.thena.datasource.ThenaSqlDataSource;
import io.resys.thena.datasource.ThenaSqlDataSourceErrorHandler;
import io.resys.thena.structures.grim.GrimQueries.InternalCommitQuery;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.RowSet;
import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = LogConstants.SHOW_SQL)
public class InternalCommitQuerySqlImpl implements InternalCommitQuery {
  private final ThenaSqlDataSource dataSource;
  private final GrimRegistry registry;
  private final ThenaSqlDataSourceErrorHandler errorHandler;
  
  public InternalCommitQuerySqlImpl(ThenaSqlDataSource dataSource) {
    super();
    this.dataSource = dataSource;
    this.registry = dataSource.getRegistry().grim();
    this.errorHandler = dataSource.getErrorHandler();
  }

  @Override
  public Uni<List<GrimCommit>> findAllByMissionId(String missionId) {
    
    final var sql = registry.commits().findAllByMissionId(missionId);
    if(log.isDebugEnabled()) {
      log.debug("User findAllByMissionId query, with props: {} \r\n{}", 
          sql.getPropsDeepString(),
          sql.getValue());
    }
    return dataSource.getClient().preparedQuery(sql.getValue())
        .mapping(registry.commits().defaultMapper())
        .execute(sql.getProps())
        .onItem()
        .transformToMulti(RowSet::toMulti)
        .collect().asList()
        .onFailure().invoke(e -> errorHandler.deadEnd(sql.failed(e, "Can't find '%s'!", GrimDocType.GRIM_COMMIT)));
  }
}
