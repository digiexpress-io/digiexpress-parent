package io.resys.thena.storesql.builders;

import java.util.List;

import io.resys.thena.api.LogConstants;
import io.resys.thena.api.entities.grim.GrimUniqueMissionLabel;
import io.resys.thena.api.registry.GrimRegistry;
import io.resys.thena.datasource.ThenaSqlDataSource;
import io.resys.thena.datasource.ThenaSqlDataSourceErrorHandler;
import io.resys.thena.structures.grim.GrimQueries;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.RowSet;
import lombok.extern.slf4j.Slf4j;


@Slf4j(topic = LogConstants.SHOW_SQL)
public class InternalMissionLabelSqlImpl implements GrimQueries.InternalMissionLabelQuery {

  private final ThenaSqlDataSource dataSource;
  private final GrimRegistry registry;
  private final ThenaSqlDataSourceErrorHandler errorHandler;
  
  public InternalMissionLabelSqlImpl(ThenaSqlDataSource dataSource) {
    super();
    this.dataSource = dataSource;
    this.registry = dataSource.getRegistry().grim();
    this.errorHandler = dataSource.getErrorHandler();
  }

  @Override
  public Uni<List<GrimUniqueMissionLabel>> findAllUnique() {
    final var sql = registry.missionLabels().findAllUniqueForMissions();
    if(log.isDebugEnabled()) {
      log.debug("InternalMissionLabelSqlImpl:findAllUnique query, with props: {} \r\n{}", 
          "",
          sql.getValue());
    }
    return dataSource.getClient().preparedQuery(sql.getValue())
        .mapping(registry.missionLabels().uniqueLabelMapper())
        .execute()
        .onItem()
        .transformToMulti(RowSet::toMulti).collect().asList()
        .onFailure().invoke(e -> errorHandler.deadEnd(sql.failed(e, "Can't find next mission ref sequence!")));
  }

}
