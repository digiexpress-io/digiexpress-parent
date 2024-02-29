package io.resys.thena.docdb.store.sql.queries;

import java.util.List;

import io.resys.thena.docdb.api.LogConstants;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgUserRole;
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
public class OrgUserRoleQuerySqlPool implements OrgQueries.UserRolesQuery {
  private final SqlClientWrapper wrapper;
  private final SqlMapper sqlMapper;
  private final SqlBuilder sqlBuilder;
  private final ErrorHandler errorHandler;

  @Override
  public Multi<OrgUserRole> findAll() {
    final var sql = sqlBuilder.orgUserRoles().findAll();
    if(log.isDebugEnabled()) {
      log.debug("UserRole findAll query, with props: {} \r\n{}", 
          "",
          sql.getValue());
    }
    return wrapper.getClient().preparedQuery(sql.getValue())
        .mapping(row -> sqlMapper.orgUserRole(row))
        .execute()
        .onItem()
        .transformToMulti((RowSet<OrgUserRole> rowset) -> Multi.createFrom().iterable(rowset))
        .onFailure().invoke(e -> errorHandler.deadEnd("Can't find 'USER_ROLE'!", e));
  }
  
  @Override
  public Multi<OrgUserRole> findAll(List<String> id) {
    final var sql = sqlBuilder.orgUserRoles().findAll(id);
    if(log.isDebugEnabled()) {
      log.debug("UserRole findAll query, with props: {} \r\n{}", 
          "",
          sql.getValue());
    }
    return wrapper.getClient().preparedQuery(sql.getValue())
        .mapping(row -> sqlMapper.orgUserRole(row))
        .execute(sql.getProps())
        .onItem()
        .transformToMulti((RowSet<OrgUserRole> rowset) -> Multi.createFrom().iterable(rowset))
        .onFailure().invoke(e -> errorHandler.deadEnd("Can't find 'USER_ROLE'!", e));
  }

  @Override
  public Uni<OrgUserRole> getById(String id) {
    final var sql = sqlBuilder.orgUserRoles().getById(id);
    if(log.isDebugEnabled()) {
      log.debug("UserRole byId query, with props: {} \r\n{}", 
          sql.getProps().deepToString(),
          sql.getValue());
    }
    return wrapper.getClient().preparedQuery(sql.getValue())
        .mapping(row -> sqlMapper.orgUserRole(row))
        .execute(sql.getProps())
        .onItem()
        .transform((RowSet<OrgUserRole> rowset) -> {
          final var it = rowset.iterator();
          if(it.hasNext()) {
            return it.next();
          }
          return null;
        })
        .onFailure(e -> errorHandler.notFound(e)).recoverWithNull()
        .onFailure().invoke(e -> errorHandler.deadEnd("Can't get 'USER_ROLE' by 'id': '" + id + "'!", e));
  }

	@Override
	public Multi<OrgUserRole> findAllByUserId(String id) {
    final var sql = sqlBuilder.orgUserRoles().findAllByUserId(id);
    if(log.isDebugEnabled()) {
      log.debug("UserRole findAllByUserId query, with props: {} \r\n{}", 
          "",
          sql.getValue());
    }
    return wrapper.getClient().preparedQuery(sql.getValue())
        .mapping(row -> sqlMapper.orgUserRole(row))
        .execute(sql.getProps())
        .onItem()
        .transformToMulti((RowSet<OrgUserRole> rowset) -> Multi.createFrom().iterable(rowset))
        .onFailure().invoke(e -> errorHandler.deadEnd("Can't find 'USER_ROLE'!", e));
	}

	@Override
	public Multi<OrgUserRole> findAllByRoleId(String id) {
    final var sql = sqlBuilder.orgUserRoles().findAllByRoleId(id);
    if(log.isDebugEnabled()) {
      log.debug("UserRole findAllByRoleId query, with props: {} \r\n{}", 
          "",
          sql.getValue());
    }
    return wrapper.getClient().preparedQuery(sql.getValue())
        .mapping(row -> sqlMapper.orgUserRole(row))
        .execute(sql.getProps())
        .onItem()
        .transformToMulti((RowSet<OrgUserRole> rowset) -> Multi.createFrom().iterable(rowset))
        .onFailure().invoke(e -> errorHandler.deadEnd("Can't find 'USER_ROLE'!", e));
	}
}
