package io.resys.thena.storesql.builders;

import io.resys.thena.api.LogConstants;
import io.resys.thena.api.entities.org.OrgActorStatus;
import io.resys.thena.datasource.SqlDataMapper;
import io.resys.thena.datasource.SqlQueryBuilder;
import io.resys.thena.datasource.ThenaSqlDataSource;
import io.resys.thena.datasource.ThenaSqlDataSourceErrorHandler;
import io.resys.thena.datasource.ThenaSqlDataSourceErrorHandler.SqlFailed;
import io.resys.thena.datasource.ThenaSqlDataSourceErrorHandler.SqlTupleFailed;
import io.resys.thena.structures.org.OrgQueries;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.RowSet;
import lombok.extern.slf4j.Slf4j;


@Slf4j(topic = LogConstants.SHOW_SQL)
public class OrgActorStatusQuerySqlPool implements OrgQueries.ActorStatusQuery {
  private final ThenaSqlDataSource wrapper;
  private final SqlDataMapper sqlMapper;
  private final SqlQueryBuilder sqlBuilder;
  private final ThenaSqlDataSourceErrorHandler errorHandler;
  
  public OrgActorStatusQuerySqlPool(ThenaSqlDataSource dataSource) {
    this.wrapper = dataSource;
    this.sqlMapper = dataSource.getDataMapper();
    this.sqlBuilder = dataSource.getQueryBuilder();
    this.errorHandler = dataSource.getErrorHandler();
  }
  @Override
  public Multi<OrgActorStatus> findAll() {
    final var sql = sqlBuilder.orgActorStatus().findAll();
    if(log.isDebugEnabled()) {
      log.debug("ActorStatus findAll query, with props: {} \r\n{}", 
          "",
          sql.getValue());
    }
    return wrapper.getClient().preparedQuery(sql.getValue())
        .mapping(row -> sqlMapper.orgActorStatus(row))
        .execute()
        .onItem()
        .transformToMulti((RowSet<OrgActorStatus> rowset) -> Multi.createFrom().iterable(rowset))
        .onFailure().invoke(e -> errorHandler.deadEnd(new SqlFailed("Can't find 'ACTOR_STATUS'!", sql, e)));
  }
  

  @Override
  public Uni<OrgActorStatus> getById(String id) {
    final var sql = sqlBuilder.orgActorStatus().getById(id);
    if(log.isDebugEnabled()) {
      log.debug("OrgActorStatus getById query, with props: {} \r\n{}", 
          sql.getProps().deepToString(),
          sql.getValue());
    }
    return wrapper.getClient().preparedQuery(sql.getValue())
        .mapping(row -> sqlMapper.orgActorStatus(row))
        .execute(sql.getProps())
        .onItem()
        .transform((RowSet<OrgActorStatus> rowset) -> {
          final var it = rowset.iterator();
          if(it.hasNext()) {
            return it.next();
          }
          return null;
        })
        .onFailure(e -> errorHandler.notFound(e)).recoverWithNull()
        .onFailure().invoke(e -> errorHandler.deadEnd(new SqlTupleFailed("Can't get 'ACTOR_STATUS' by 'id': '" + id + "'!", sql, e)));
  }
  
  @Override
  public Multi<OrgActorStatus> findAllByRightId(String id) {
    final var sql = sqlBuilder.orgActorStatus().findAllByIdRightId(id);
    if(log.isDebugEnabled()) {
      log.debug("OrgActorStatus findAllByRightId query, with props: {} \r\n{}", 
          sql.getProps().deepToString(),
          sql.getValue());
    }
    return wrapper.getClient().preparedQuery(sql.getValue())
        .mapping(row -> sqlMapper.orgActorStatus(row))
        .execute(sql.getProps())
        .onItem()
        .transformToMulti((RowSet<OrgActorStatus> rowset) -> Multi.createFrom().iterable(rowset))
        .onFailure().invoke(e -> errorHandler.deadEnd(new SqlTupleFailed("Can't get 'ACTOR_STATUS' by 'right_id': '" + id + "'!", sql, e)));
  }


  @Override
  public Multi<OrgActorStatus> findAllByMemberId(String id) {
    final var sql = sqlBuilder.orgActorStatus().findAllByMemberId(id);
    if(log.isDebugEnabled()) {
      log.debug("OrgActorStatus findAllByMemberId query, with props: {} \r\n{}", 
          sql.getProps().deepToString(),
          sql.getValue());
    }
    return wrapper.getClient().preparedQuery(sql.getValue())
        .mapping(row -> sqlMapper.orgActorStatus(row))
        .execute(sql.getProps())
        .onItem()
        .transformToMulti((RowSet<OrgActorStatus> rowset) -> Multi.createFrom().iterable(rowset))
        .onFailure().invoke(e -> errorHandler.deadEnd(new SqlTupleFailed("Can't get 'ACTOR_STATUS' by 'member_id': '" + id + "'!", sql, e)));
  }
  @Override
  public Multi<OrgActorStatus> findAllByPartyId(String id) {
    final var sql = sqlBuilder.orgActorStatus().findAllByPartyId(id);
    if(log.isDebugEnabled()) {
      log.debug("OrgActorStatus findAllByPartyId query, with props: {} \r\n{}", 
          sql.getProps().deepToString(),
          sql.getValue());
    }
    return wrapper.getClient().preparedQuery(sql.getValue())
        .mapping(row -> sqlMapper.orgActorStatus(row))
        .execute(sql.getProps())
        .onItem()
        .transformToMulti((RowSet<OrgActorStatus> rowset) -> Multi.createFrom().iterable(rowset))
        .onFailure().invoke(e -> errorHandler.deadEnd(new SqlTupleFailed("Can't get 'ACTOR_STATUS' by 'party_id': '" + id + "'!", sql, e)));
  }
}
