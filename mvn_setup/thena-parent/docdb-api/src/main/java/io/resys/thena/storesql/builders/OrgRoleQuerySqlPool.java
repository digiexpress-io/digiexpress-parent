package io.resys.thena.storesql.builders;

import java.util.Collection;

import io.resys.thena.api.LogConstants;
import io.resys.thena.api.models.ThenaOrgObject.OrgRight;
import io.resys.thena.models.org.OrgQueries;
import io.resys.thena.storesql.SqlBuilder;
import io.resys.thena.storesql.SqlMapper;
import io.resys.thena.storesql.support.SqlClientWrapper;
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
public class OrgRoleQuerySqlPool implements OrgQueries.RightsQuery {
  private final SqlClientWrapper wrapper;
  private final SqlMapper sqlMapper;
  private final SqlBuilder sqlBuilder;
  private final ErrorHandler errorHandler;

  @Override
  public Multi<OrgRight> findAll() {
    final var sql = sqlBuilder.orgRights().findAll();
    if(log.isDebugEnabled()) {
      log.debug("Role findAll query, with props: {} \r\n{}", 
          "",
          sql.getValue());
    }
    return wrapper.getClient().preparedQuery(sql.getValue())
        .mapping(row -> sqlMapper.orgRight(row))
        .execute()
        .onItem()
        .transformToMulti((RowSet<OrgRight> rowset) -> Multi.createFrom().iterable(rowset))
        .onFailure().invoke(e -> errorHandler.deadEnd(new SqlFailed("Can't find 'RIGHTS'!", sql, e)));
  }
  
  @Override
  public Multi<OrgRight> findAll(Collection<String> id) {
    final var sql = sqlBuilder.orgRights().findAll(id);
    if(log.isDebugEnabled()) {
      log.debug("Role findAll query, with props: {} \r\n{}", 
          "",
          sql.getValue());
    }
    return wrapper.getClient().preparedQuery(sql.getValue())
        .mapping(row -> sqlMapper.orgRight(row))
        .execute(sql.getProps())
        .onItem()
        .transformToMulti((RowSet<OrgRight> rowset) -> Multi.createFrom().iterable(rowset))
        .onFailure().invoke(e -> errorHandler.deadEnd(new SqlTupleFailed("Can't find 'RIGHTS'!", sql, e)));
  }

  @Override
  public Uni<OrgRight> getById(String id) {
    final var sql = sqlBuilder.orgRights().getById(id);
    if(log.isDebugEnabled()) {
      log.debug("Role byId query, with props: {} \r\n{}", 
          sql.getProps().deepToString(),
          sql.getValue());
    }
    return wrapper.getClient().preparedQuery(sql.getValue())
        .mapping(row -> sqlMapper.orgRight(row))
        .execute(sql.getProps())
        .onItem()
        .transform((RowSet<OrgRight> rowset) -> {
          final var it = rowset.iterator();
          if(it.hasNext()) {
            return it.next();
          }
          return null;
        })
        .onFailure(e -> errorHandler.notFound(e)).recoverWithNull()
        .onFailure().invoke(e -> errorHandler.deadEnd(new SqlTupleFailed("Can't get 'RIGHTS' by 'id': '" + id + "'!", sql, e)));
  }
}
