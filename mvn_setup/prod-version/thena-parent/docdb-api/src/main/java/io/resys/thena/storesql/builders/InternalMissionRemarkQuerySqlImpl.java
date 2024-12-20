package io.resys.thena.storesql.builders;

import io.resys.thena.api.LogConstants;
import io.resys.thena.api.entities.grim.ImmutableGrimMissionContainer;
import io.resys.thena.api.entities.grim.ThenaGrimContainers.GrimMissionContainer;
import io.resys.thena.api.entities.grim.ThenaGrimObject.GrimDocType;
import io.resys.thena.api.registry.GrimRegistry;
import io.resys.thena.datasource.ThenaSqlDataSource;
import io.resys.thena.datasource.ThenaSqlDataSourceErrorHandler;
import io.resys.thena.structures.grim.GrimQueries.InternalMissionRemarkQuery;
import io.smallrye.mutiny.Uni;
import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = LogConstants.SHOW_SQL)
public class InternalMissionRemarkQuerySqlImpl implements InternalMissionRemarkQuery {
  private final ThenaSqlDataSource dataSource;
  private final GrimRegistry registry;
  private final ThenaSqlDataSourceErrorHandler errorHandler;
  
  public InternalMissionRemarkQuerySqlImpl(ThenaSqlDataSource dataSource) {
    super();
    this.dataSource = dataSource;
    this.registry = dataSource.getRegistry().grim();
    this.errorHandler = dataSource.getErrorHandler();
  }

  @Override
  public Uni<GrimMissionContainer> getOneByRemarkId(String remarkId) {
    final var sql = registry.remarks().getById(remarkId);
    if(log.isDebugEnabled()) {
      log.debug("InternalMissionRemarkQuerySqlImpl getOneByRemarkId query, with props: {} \r\n{}", 
          sql.getPropsDeepString(),
          sql.getValue());
    }
    return dataSource.getClient().preparedQuery(sql.getValue())
        .mapping(registry.remarks().defaultMapper())
        .execute(sql.getProps())
        .onItem()
        .transform(rows -> {
          final var builder = ImmutableGrimMissionContainer.builder();
          for(final var row : rows) {
            builder.putRemarks(row.getId(), row);
          }

          final GrimMissionContainer result = builder.build();          
          return result;
        })
        .onFailure().invoke(e -> errorHandler.deadEnd(sql.failed(e, "Can't find '%s'!", GrimDocType.GRIM_REMARK)))
        ;
  }
  @Override
  public Uni<GrimMissionContainer> findAllByMissionId(String missionId) {
    final var sql = registry.remarks().findAllByMissionId(missionId);
    if(log.isDebugEnabled()) {
      log.debug("InternalMissionRemarkQuerySqlImpl findAllByMissionId query, with props: {} \r\n{}", 
          sql.getPropsDeepString(),
          sql.getValue());
    }
    return dataSource.getClient().preparedQuery(sql.getValue())
        .mapping(registry.remarks().defaultMapper())
        .execute(sql.getProps())
        .onItem()
        .transform(rows -> {
          final var builder = ImmutableGrimMissionContainer.builder();
          for(final var row : rows) {
            builder.putRemarks(row.getId(), row);
          }

          final GrimMissionContainer result = builder.build();          
          return result;
        })
        .onFailure().invoke(e -> errorHandler.deadEnd(sql.failed(e, "Can't find '%s'!", GrimDocType.GRIM_REMARK)));
  }
}
