package io.resys.thena.storesql.builders;

import java.util.Collection;

import io.resys.thena.api.LogConstants;
import io.resys.thena.api.entities.grim.GrimAnyObject;
import io.resys.thena.api.entities.grim.GrimCommitViewer;
import io.resys.thena.api.entities.grim.ThenaGrimObject.GrimDocType;
import io.resys.thena.api.registry.GrimRegistry;
import io.resys.thena.api.registry.grim.GrimCommitViewerRegistry.AnyObjectCriteria;
import io.resys.thena.datasource.ThenaSqlDataSource;
import io.resys.thena.datasource.ThenaSqlDataSourceErrorHandler;
import io.resys.thena.structures.grim.GrimQueries.CommitViewerQuery;
import io.smallrye.mutiny.Multi;
import io.vertx.mutiny.sqlclient.RowSet;
import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = LogConstants.SHOW_SQL)
public class CommitViewerQuerySqlImpl implements CommitViewerQuery {
  private final ThenaSqlDataSource dataSource;
  private final GrimRegistry registry;
  private final ThenaSqlDataSourceErrorHandler errorHandler;
  
  public CommitViewerQuerySqlImpl(ThenaSqlDataSource dataSource) {
    super();
    this.dataSource = dataSource;
    this.registry = dataSource.getRegistry().grim();
    this.errorHandler = dataSource.getErrorHandler();
  }

  @Override
  public Multi<GrimAnyObject> findAnyObjects(Collection<AnyObjectCriteria> commits) {
    final var sql = registry.commitViewers().findAllObjectsByIdAndType(commits);
    if(log.isDebugEnabled()) {
      log.debug("User findAllObjectsByIdAndType query, with props: {} \r\n{}", 
          sql.getPropsDeepString(),
          sql.getValue());
    }
    return dataSource.getClient().preparedQuery(sql.getValue())
        .mapping(registry.commitViewers().anyObjectMapper())
        .execute(sql.getProps())
        .onItem()
        .transformToMulti(RowSet::toMulti)
        .onFailure().invoke(e -> errorHandler.deadEnd(sql.failed(e, "Can't find all objects!")));
  }
  @Override
  public Multi<GrimCommitViewer> findAllViewersByUsed(String userId, String usedBy, Collection<String> commits) {
    final var sql = registry.commitViewers().findAllByUsedByAndCommit(userId, usedBy, commits);
    if(log.isDebugEnabled()) {
      log.debug("User findAllByUsedByAndCommit query, with props: {} \r\n{}", 
          sql.getPropsDeepString(),
          sql.getValue());
    }
    return dataSource.getClient().preparedQuery(sql.getValue())
        .mapping(registry.commitViewers().defaultMapper())
        .execute(sql.getProps())
        .onItem()
        .transformToMulti(RowSet::toMulti)
        .onFailure().invoke(e -> errorHandler.deadEnd(sql.failed(e, "Can't find '%s'!", GrimDocType.GRIM_COMMIT_VIEWER)));
  }
}
