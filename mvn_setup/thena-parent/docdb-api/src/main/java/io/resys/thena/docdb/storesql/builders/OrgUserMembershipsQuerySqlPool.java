package io.resys.thena.docdb.storesql.builders;

import java.util.List;

import io.resys.thena.docdb.api.LogConstants;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgMembership;
import io.resys.thena.docdb.models.org.OrgQueries;
import io.resys.thena.docdb.storesql.SqlBuilder;
import io.resys.thena.docdb.storesql.SqlMapper;
import io.resys.thena.docdb.storesql.support.SqlClientWrapper;
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
public class OrgUserMembershipsQuerySqlPool implements OrgQueries.MembershipQuery {
  private final SqlClientWrapper wrapper;
  private final SqlMapper sqlMapper;
  private final SqlBuilder sqlBuilder;
  private final ErrorHandler errorHandler;

  @Override
  public Multi<OrgMembership> findAll() {
    final var sql = sqlBuilder.orgMemberships().findAll();
    if(log.isDebugEnabled()) {
      log.debug("UserMembership findAll query, with props: {} \r\n{}", 
          "",
          sql.getValue());
    }
    return wrapper.getClient().preparedQuery(sql.getValue())
        .mapping(row -> sqlMapper.orgMembership(row))
        .execute()
        .onItem()
        .transformToMulti((RowSet<OrgMembership> rowset) -> Multi.createFrom().iterable(rowset))
        .onFailure().invoke(e -> errorHandler.deadEnd(new SqlFailed("Can't find 'MEMBERSHIP'!", sql, e)));
  }
  
  @Override
  public Multi<OrgMembership> findAll(List<String> id) {
    final var sql = sqlBuilder.orgMemberships().findAll(id);
    if(log.isDebugEnabled()) {
      log.debug("UserMembership findAll query, with props: {} \r\n{}", 
          "",
          sql.getValue());
    }
    return wrapper.getClient().preparedQuery(sql.getValue())
        .mapping(row -> sqlMapper.orgMembership(row))
        .execute(sql.getProps())
        .onItem()
        .transformToMulti((RowSet<OrgMembership> rowset) -> Multi.createFrom().iterable(rowset))
        .onFailure().invoke(e -> errorHandler.deadEnd(new SqlTupleFailed("Can't find 'MEMBERSHIP'!", sql, e)));
  }

  @Override
  public Uni<OrgMembership> getById(String id) {
    final var sql = sqlBuilder.orgMemberships().getById(id);
    if(log.isDebugEnabled()) {
      log.debug("UserMembership byId query, with props: {} \r\n{}", 
          sql.getProps().deepToString(),
          sql.getValue());
    }
    return wrapper.getClient().preparedQuery(sql.getValue())
        .mapping(row -> sqlMapper.orgMembership(row))
        .execute(sql.getProps())
        .onItem()
        .transform((RowSet<OrgMembership> rowset) -> {
          final var it = rowset.iterator();
          if(it.hasNext()) {
            return it.next();
          }
          return null;
        })
        .onFailure(e -> errorHandler.notFound(e)).recoverWithNull()
        .onFailure().invoke(e -> errorHandler.deadEnd(new SqlTupleFailed("Can't get 'MEMBERSHIP' by 'id': '" + id + "'!", sql, e)));
  }

	@Override
	public Multi<OrgMembership> findAllByPartyId(String id) {
    final var sql = sqlBuilder.orgMemberships().findAllByGroupId(id);
    if(log.isDebugEnabled()) {
      log.debug("UserMembership findAllByGroupId query, with props: {} \r\n{}", 
          "",
          sql.getValue());
    }
    return wrapper.getClient().preparedQuery(sql.getValue())
        .mapping(row -> sqlMapper.orgMembership(row))
        .execute(sql.getProps())
        .onItem()
        .transformToMulti((RowSet<OrgMembership> rowset) -> Multi.createFrom().iterable(rowset))
        .onFailure().invoke(e -> errorHandler.deadEnd(new SqlTupleFailed("Can't find 'MEMBERSHIP'!", sql, e)));
	}

	@Override
	public Multi<OrgMembership> findAllByMemberId(String id) {
    final var sql = sqlBuilder.orgMemberships().findAllByUserId(id);
    if(log.isDebugEnabled()) {
      log.debug("UserMembership findAllByUserId query, with props: {} \r\n{}", 
          "",
          sql.getValue());
    }
    return wrapper.getClient().preparedQuery(sql.getValue())
        .mapping(row -> sqlMapper.orgMembership(row))
        .execute(sql.getProps())
        .onItem()
        .transformToMulti((RowSet<OrgMembership> rowset) -> Multi.createFrom().iterable(rowset))
        .onFailure().invoke(e -> errorHandler.deadEnd(new SqlTupleFailed("Can't find 'MEMBERSHIP'!", sql, e)));
	}
}
