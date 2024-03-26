package io.resys.thena.storesql.builders;

import java.util.List;

import io.resys.thena.api.LogConstants;
import io.resys.thena.api.models.ThenaOrgObject.OrgMemberRight;
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
public class OrgUserRoleQuerySqlPool implements OrgQueries.MemberRightsQuery {
  private final SqlClientWrapper wrapper;
  private final SqlMapper sqlMapper;
  private final SqlBuilder sqlBuilder;
  private final ErrorHandler errorHandler;

  @Override
  public Multi<OrgMemberRight> findAll() {
    final var sql = sqlBuilder.orgMemberRights().findAll();
    if(log.isDebugEnabled()) {
      log.debug("UserRole findAll query, with props: {} \r\n{}", 
          "",
          sql.getValue());
    }
    return wrapper.getClient().preparedQuery(sql.getValue())
        .mapping(row -> sqlMapper.orgMemberRight(row))
        .execute()
        .onItem()
        .transformToMulti((RowSet<OrgMemberRight> rowset) -> Multi.createFrom().iterable(rowset))
        .onFailure().invoke(e -> errorHandler.deadEnd(new SqlFailed("Can't find 'MEMBER_RIGHT'!", sql, e)));
  }
  
  @Override
  public Multi<OrgMemberRight> findAll(List<String> id) {
    final var sql = sqlBuilder.orgMemberRights().findAll(id);
    if(log.isDebugEnabled()) {
      log.debug("UserRole findAll query, with props: {} \r\n{}", 
          "",
          sql.getValue());
    }
    return wrapper.getClient().preparedQuery(sql.getValue())
        .mapping(row -> sqlMapper.orgMemberRight(row))
        .execute(sql.getProps())
        .onItem()
        .transformToMulti((RowSet<OrgMemberRight> rowset) -> Multi.createFrom().iterable(rowset))
        .onFailure().invoke(e -> errorHandler.deadEnd(new SqlTupleFailed("Can't find 'MEMBER_RIGHT'!", sql, e)));
  }

  @Override
  public Uni<OrgMemberRight> getById(String id) {
    final var sql = sqlBuilder.orgMemberRights().getById(id);
    if(log.isDebugEnabled()) {
      log.debug("UserRole byId query, with props: {} \r\n{}", 
          sql.getProps().deepToString(),
          sql.getValue());
    }
    return wrapper.getClient().preparedQuery(sql.getValue())
        .mapping(row -> sqlMapper.orgMemberRight(row))
        .execute(sql.getProps())
        .onItem()
        .transform((RowSet<OrgMemberRight> rowset) -> {
          final var it = rowset.iterator();
          if(it.hasNext()) {
            return it.next();
          }
          return null;
        })
        .onFailure(e -> errorHandler.notFound(e)).recoverWithNull()
        .onFailure().invoke(e -> errorHandler.deadEnd(new SqlTupleFailed("Can't get 'MEMBER_RIGHT' by 'id': '" + id + "'!", sql, e)));
  }

	@Override
	public Multi<OrgMemberRight> findAllByMemberId(String id) {
    final var sql = sqlBuilder.orgMemberRights().findAllByUserId(id);
    if(log.isDebugEnabled()) {
      log.debug("UserRole findAllByUserId query, with props: {} \r\n{}", 
          "",
          sql.getValue());
    }
    return wrapper.getClient().preparedQuery(sql.getValue())
        .mapping(row -> sqlMapper.orgMemberRight(row))
        .execute(sql.getProps())
        .onItem()
        .transformToMulti((RowSet<OrgMemberRight> rowset) -> Multi.createFrom().iterable(rowset))
        .onFailure().invoke(e -> errorHandler.deadEnd(new SqlTupleFailed("Can't find 'MEMBER_RIGHT'!", sql, e)));
	}

	@Override
	public Multi<OrgMemberRight> findAllByRightId(String id) {
    final var sql = sqlBuilder.orgMemberRights().findAllByRoleId(id);
    if(log.isDebugEnabled()) {
      log.debug("UserRole findAllByRoleId query, with props: {} \r\n{}", 
          "",
          sql.getValue());
    }
    return wrapper.getClient().preparedQuery(sql.getValue())
        .mapping(row -> sqlMapper.orgMemberRight(row))
        .execute(sql.getProps())
        .onItem()
        .transformToMulti((RowSet<OrgMemberRight> rowset) -> Multi.createFrom().iterable(rowset))
        .onFailure().invoke(e -> errorHandler.deadEnd(new SqlTupleFailed("Can't find 'MEMBER_RIGHT'!", sql, e)));
	}
}
