package io.resys.thena.docdb.store.sql.queries;

import java.util.List;

import io.resys.thena.docdb.api.LogConstants;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgUserMembership;
import io.resys.thena.docdb.models.org.OrgQueries;
import io.resys.thena.docdb.store.sql.SqlBuilder;
import io.resys.thena.docdb.store.sql.SqlMapper;
import io.resys.thena.docdb.store.sql.support.SqlClientWrapper;
import io.resys.thena.docdb.support.ErrorHandler;
import io.resys.thena.docdb.support.ErrorHandler.SqlFailed;
import io.resys.thena.docdb.support.ErrorHandler.SqlTupleFailed;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.RowSet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j(topic = LogConstants.SHOW_SQL)
@RequiredArgsConstructor
public class OrgUserMembershipsQuerySqlPool implements OrgQueries.UserMembershipQuery {
  private final SqlClientWrapper wrapper;
  private final SqlMapper sqlMapper;
  private final SqlBuilder sqlBuilder;
  private final ErrorHandler errorHandler;

  @Override
  public Multi<OrgUserMembership> findAll() {
    final var sql = sqlBuilder.orgUserMemberships().findAll();
    if(log.isDebugEnabled()) {
      log.debug("UserMembership findAll query, with props: {} \r\n{}", 
          "",
          sql.getValue());
    }
    return wrapper.getClient().preparedQuery(sql.getValue())
        .mapping(row -> sqlMapper.orgUserMemberships(row))
        .execute()
        .onItem()
        .transformToMulti((RowSet<OrgUserMembership> rowset) -> Multi.createFrom().iterable(rowset))
        .onFailure().invoke(e -> errorHandler.deadEnd(new SqlFailed("Can't find 'USER_MEMBERSHIP'!", sql, e)));
  }
  
  @Override
  public Multi<OrgUserMembership> findAll(List<String> id) {
    final var sql = sqlBuilder.orgUserMemberships().findAll(id);
    if(log.isDebugEnabled()) {
      log.debug("UserMembership findAll query, with props: {} \r\n{}", 
          "",
          sql.getValue());
    }
    return wrapper.getClient().preparedQuery(sql.getValue())
        .mapping(row -> sqlMapper.orgUserMemberships(row))
        .execute(sql.getProps())
        .onItem()
        .transformToMulti((RowSet<OrgUserMembership> rowset) -> Multi.createFrom().iterable(rowset))
        .onFailure().invoke(e -> errorHandler.deadEnd(new SqlTupleFailed("Can't find 'USER_MEMBERSHIP'!", sql, e)));
  }

  @Override
  public Uni<OrgUserMembership> getById(String id) {
    final var sql = sqlBuilder.orgUserMemberships().getById(id);
    if(log.isDebugEnabled()) {
      log.debug("UserMembership byId query, with props: {} \r\n{}", 
          sql.getProps().deepToString(),
          sql.getValue());
    }
    return wrapper.getClient().preparedQuery(sql.getValue())
        .mapping(row -> sqlMapper.orgUserMemberships(row))
        .execute(sql.getProps())
        .onItem()
        .transform((RowSet<OrgUserMembership> rowset) -> {
          final var it = rowset.iterator();
          if(it.hasNext()) {
            return it.next();
          }
          return null;
        })
        .onFailure(e -> errorHandler.notFound(e)).recoverWithNull()
        .onFailure().invoke(e -> errorHandler.deadEnd(new SqlTupleFailed("Can't get 'USER_MEMBERSHIP' by 'id': '" + id + "'!", sql, e)));
  }

	@Override
	public Multi<OrgUserMembership> findAllByGroupId(String id) {
    final var sql = sqlBuilder.orgUserMemberships().findAllByGroupId(id);
    if(log.isDebugEnabled()) {
      log.debug("UserMembership findAllByGroupId query, with props: {} \r\n{}", 
          "",
          sql.getValue());
    }
    return wrapper.getClient().preparedQuery(sql.getValue())
        .mapping(row -> sqlMapper.orgUserMemberships(row))
        .execute(sql.getProps())
        .onItem()
        .transformToMulti((RowSet<OrgUserMembership> rowset) -> Multi.createFrom().iterable(rowset))
        .onFailure().invoke(e -> errorHandler.deadEnd(new SqlTupleFailed("Can't find 'USER_MEMBERSHIP'!", sql, e)));
	}

	@Override
	public Multi<OrgUserMembership> findAllByUserId(String id) {
    final var sql = sqlBuilder.orgUserMemberships().findAllByUserId(id);
    if(log.isDebugEnabled()) {
      log.debug("UserMembership findAllByUserId query, with props: {} \r\n{}", 
          "",
          sql.getValue());
    }
    return wrapper.getClient().preparedQuery(sql.getValue())
        .mapping(row -> sqlMapper.orgUserMemberships(row))
        .execute(sql.getProps())
        .onItem()
        .transformToMulti((RowSet<OrgUserMembership> rowset) -> Multi.createFrom().iterable(rowset))
        .onFailure().invoke(e -> errorHandler.deadEnd(new SqlTupleFailed("Can't find 'USER_MEMBERSHIP'!", sql, e)));
	}
}
