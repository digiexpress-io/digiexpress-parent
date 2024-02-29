package io.resys.thena.docdb.store.sql.queries;

import java.util.List;

import io.resys.thena.docdb.api.LogConstants;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgGroupRole;
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
public class OrgGroupRoleQuerySqlPool implements OrgQueries.GroupRolesQuery {
  private final SqlClientWrapper wrapper;
  private final SqlMapper sqlMapper;
  private final SqlBuilder sqlBuilder;
  private final ErrorHandler errorHandler;

  @Override
  public Multi<OrgGroupRole> findAll() {
    final var sql = sqlBuilder.orgGroupRoles().findAll();
    if(log.isDebugEnabled()) {
      log.debug("GroupRole findAll query, with props: {} \r\n{}", 
          "",
          sql.getValue());
    }
    return wrapper.getClient().preparedQuery(sql.getValue())
        .mapping(row -> sqlMapper.orgGroupRole(row))
        .execute()
        .onItem()
        .transformToMulti((RowSet<OrgGroupRole> rowset) -> Multi.createFrom().iterable(rowset))
        .onFailure().invoke(e -> errorHandler.deadEnd("Can't find 'GROUP_ROLE'!", e));
  }
  
  @Override
  public Multi<OrgGroupRole> findAll(List<String> id) {
    final var sql = sqlBuilder.orgGroupRoles().findAll(id);
    if(log.isDebugEnabled()) {
      log.debug("GroupRole findAll query, with props: {} \r\n{}", 
          "",
          sql.getValue());
    }
    return wrapper.getClient().preparedQuery(sql.getValue())
        .mapping(row -> sqlMapper.orgGroupRole(row))
        .execute(sql.getProps())
        .onItem()
        .transformToMulti((RowSet<OrgGroupRole> rowset) -> Multi.createFrom().iterable(rowset))
        .onFailure().invoke(e -> errorHandler.deadEnd("Can't find 'GROUP_ROLE'!", e));
  }

  @Override
  public Uni<OrgGroupRole> getById(String id) {
    final var sql = sqlBuilder.orgGroupRoles().getById(id);
    if(log.isDebugEnabled()) {
      log.debug("GroupRole byId query, with props: {} \r\n{}", 
          sql.getProps().deepToString(),
          sql.getValue());
    }
    return wrapper.getClient().preparedQuery(sql.getValue())
        .mapping(row -> sqlMapper.orgGroupRole(row))
        .execute(sql.getProps())
        .onItem()
        .transform((RowSet<OrgGroupRole> rowset) -> {
          final var it = rowset.iterator();
          if(it.hasNext()) {
            return it.next();
          }
          return null;
        })
        .onFailure(e -> errorHandler.notFound(e)).recoverWithNull()
        .onFailure().invoke(e -> errorHandler.deadEnd("Can't get 'GROUP_ROLE' by 'id': '" + id + "'!", e));
  }

	@Override
	public Multi<OrgGroupRole> findAllByGroupId(String id) {
    final var sql = sqlBuilder.orgGroupRoles().findAllByGroupId(id);
    if(log.isDebugEnabled()) {
      log.debug("GroupRole findAllByGroupId query, with props: {} \r\n{}", 
          "",
          sql.getValue());
    }
    return wrapper.getClient().preparedQuery(sql.getValue())
        .mapping(row -> sqlMapper.orgGroupRole(row))
        .execute(sql.getProps())
        .onItem()
        .transformToMulti((RowSet<OrgGroupRole> rowset) -> Multi.createFrom().iterable(rowset))
        .onFailure().invoke(e -> errorHandler.deadEnd("Can't find 'GROUP_ROLE'!", e));
	}

	@Override
	public Multi<OrgGroupRole> findAllByRoleId(String id) {
    final var sql = sqlBuilder.orgGroupRoles().findAllByRoleId(id);
    if(log.isDebugEnabled()) {
      log.debug("UserRole findAllByRoleId query, with props: {} \r\n{}", 
          "",
          sql.getValue());
    }
    return wrapper.getClient().preparedQuery(sql.getValue())
        .mapping(row -> sqlMapper.orgGroupRole(row))
        .execute(sql.getProps())
        .onItem()
        .transformToMulti((RowSet<OrgGroupRole> rowset) -> Multi.createFrom().iterable(rowset))
        .onFailure().invoke(e -> errorHandler.deadEnd("Can't find 'GROUP_ROLE'!", e));
	}
}