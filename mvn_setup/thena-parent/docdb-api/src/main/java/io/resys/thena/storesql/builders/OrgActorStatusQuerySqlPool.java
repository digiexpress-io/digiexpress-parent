package io.resys.thena.storesql.builders;

import io.resys.thena.api.LogConstants;
import io.resys.thena.api.entities.org.OrgActorStatus;
import io.resys.thena.storesql.SqlBuilder;
import io.resys.thena.storesql.SqlMapper;
import io.resys.thena.storesql.support.SqlClientWrapper;
import io.resys.thena.structures.org.OrgQueries;
import io.resys.thena.support.ErrorHandler;
import io.resys.thena.support.ErrorHandler.SqlFailed;
import io.resys.thena.support.ErrorHandler.SqlTupleFailed;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.RowSet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j(topic = LogConstants.SHOW_SQL)
@RequiredArgsConstructor
public class OrgActorStatusQuerySqlPool implements OrgQueries.ActorStatusQuery {
  private final SqlClientWrapper wrapper;
  private final SqlMapper sqlMapper;
  private final SqlBuilder sqlBuilder;
  private final ErrorHandler errorHandler;

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
  public Multi<OrgActorStatus> findAllByIdRightId(String id) {
    final var sql = sqlBuilder.orgActorStatus().findAllByIdRightId(id);
    if(log.isDebugEnabled()) {
      log.debug("OrgActorStatus findAllByIdRightId query, with props: {} \r\n{}", 
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
  public Multi<OrgActorStatus> findAllByIdMemberId(String id) {
    final var sql = sqlBuilder.orgActorStatus().findAllByIdMemberId(id);
    if(log.isDebugEnabled()) {
      log.debug("OrgActorStatus findAllByIdMemberId query, with props: {} \r\n{}", 
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
}
