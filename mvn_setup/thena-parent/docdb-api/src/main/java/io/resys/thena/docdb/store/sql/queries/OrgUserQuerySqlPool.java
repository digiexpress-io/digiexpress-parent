package io.resys.thena.docdb.store.sql.queries;

import java.util.List;

import io.resys.thena.docdb.api.LogConstants;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgUser;
import io.resys.thena.docdb.models.org.OrgQueries;
import io.resys.thena.docdb.store.sql.SqlBuilder;
import io.resys.thena.docdb.store.sql.SqlMapper;
import io.resys.thena.docdb.store.sql.support.SqlClientWrapper;
import io.resys.thena.docdb.support.ErrorHandler;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.RowSet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j(topic = LogConstants.SHOW_SQL)
@RequiredArgsConstructor
public class OrgUserQuerySqlPool implements OrgQueries.UserQuery {
  private final SqlClientWrapper wrapper;
  private final SqlMapper sqlMapper;
  private final SqlBuilder sqlBuilder;
  private final ErrorHandler errorHandler;

  @Override
  public Multi<OrgUser> findAll() {
    final var sql = sqlBuilder.orgUsers().findAll();
    if(log.isDebugEnabled()) {
      log.debug("User findAll query, with props: {} \r\n{}", 
          "",
          sql.getValue());
    }
    return wrapper.getClient().preparedQuery(sql.getValue())
        .mapping(row -> sqlMapper.orgUser(row))
        .execute()
        .onItem()
        .transformToMulti((RowSet<OrgUser> rowset) -> Multi.createFrom().iterable(rowset))
        .onFailure().invoke(e -> errorHandler.deadEnd("Can't find 'USER'!", e));
  }
  
  @Override
  public Multi<OrgUser> findAll(List<String> id) {
    final var sql = sqlBuilder.orgUsers().findAll(id);
    if(log.isDebugEnabled()) {
      log.debug("User findAll query, with props: {} \r\n{}", 
          "",
          sql.getValue());
    }
    return wrapper.getClient().preparedQuery(sql.getValue())
        .mapping(row -> sqlMapper.orgUser(row))
        .execute(sql.getProps())
        .onItem()
        .transformToMulti((RowSet<OrgUser> rowset) -> Multi.createFrom().iterable(rowset))
        .onFailure().invoke(e -> errorHandler.deadEnd("Can't find 'USER'!", e));
  }


  @Override
  public Uni<OrgUser> getById(String id) {
    final var sql = sqlBuilder.orgUsers().getById(id);
    if(log.isDebugEnabled()) {
      log.debug("User byId query, with props: {} \r\n{}", 
          sql.getProps().deepToString(),
          sql.getValue());
    }
    return wrapper.getClient().preparedQuery(sql.getValue())
        .mapping(row -> sqlMapper.orgUser(row))
        .execute(sql.getProps())
        .onItem()
        .transform((RowSet<OrgUser> rowset) -> {
          final var it = rowset.iterator();
          if(it.hasNext()) {
            return it.next();
          }
          return null;
        })
        .onFailure(e -> errorHandler.notFound(e)).recoverWithNull()
        .onFailure().invoke(e -> errorHandler.deadEnd("Can't get 'USER' by 'id': '" + id + "'!", e));
  }
}
