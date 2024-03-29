package io.resys.thena.storesql.builders;

import java.util.List;

import io.resys.thena.api.LogConstants;
import io.resys.thena.api.entities.org.OrgPartyRight;
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
public class OrgGroupRoleQuerySqlPool implements OrgQueries.PartyRightsQuery {
  private final ThenaSqlDataSource wrapper;
  private final SqlDataMapper sqlMapper;
  private final SqlQueryBuilder sqlBuilder;
  private final ThenaSqlDataSourceErrorHandler errorHandler;
  public OrgGroupRoleQuerySqlPool(ThenaSqlDataSource dataSource) {
    this.wrapper = dataSource;
    this.sqlMapper = dataSource.getDataMapper();
    this.sqlBuilder = dataSource.getQueryBuilder();
    this.errorHandler = dataSource.getErrorHandler();
  }
  @Override
  public Multi<OrgPartyRight> findAll() {
    final var sql = sqlBuilder.orgPartyRights().findAll();
    if(log.isDebugEnabled()) {
      log.debug("GroupRole findAll query, with props: {} \r\n{}", 
          "",
          sql.getValue());
    }
    return wrapper.getClient().preparedQuery(sql.getValue())
        .mapping(row -> sqlMapper.orgPartyRright(row))
        .execute()
        .onItem()
        .transformToMulti((RowSet<OrgPartyRight> rowset) -> Multi.createFrom().iterable(rowset))
        .onFailure().invoke(e -> errorHandler.deadEnd(new SqlFailed("Can't find 'PARTY_RIGHTS'!", sql, e)));
  }
  
  @Override
  public Multi<OrgPartyRight> findAll(List<String> id) {
    final var sql = sqlBuilder.orgPartyRights().findAll(id);
    if(log.isDebugEnabled()) {
      log.debug("GroupRole findAll query, with props: {} \r\n{}", 
          "",
          sql.getValue());
    }
    return wrapper.getClient().preparedQuery(sql.getValue())
        .mapping(row -> sqlMapper.orgPartyRright(row))
        .execute(sql.getProps())
        .onItem()
        .transformToMulti((RowSet<OrgPartyRight> rowset) -> Multi.createFrom().iterable(rowset))
        .onFailure().invoke(e -> errorHandler.deadEnd(new SqlTupleFailed("Can't find 'PARTY_RIGHTS'!", sql, e)));
  }

  @Override
  public Uni<OrgPartyRight> getById(String id) {
    final var sql = sqlBuilder.orgPartyRights().getById(id);
    if(log.isDebugEnabled()) {
      log.debug("GroupRole byId query, with props: {} \r\n{}", 
          sql.getProps().deepToString(),
          sql.getValue());
    }
    return wrapper.getClient().preparedQuery(sql.getValue())
        .mapping(row -> sqlMapper.orgPartyRright(row))
        .execute(sql.getProps())
        .onItem()
        .transform((RowSet<OrgPartyRight> rowset) -> {
          final var it = rowset.iterator();
          if(it.hasNext()) {
            return it.next();
          }
          return null;
        })
        .onFailure(e -> errorHandler.notFound(e)).recoverWithNull()
        .onFailure().invoke(e -> errorHandler.deadEnd(new SqlTupleFailed("Can't get 'PARTY_RIGHTS' by 'id': '" + id + "'!", sql, e)));
  }

	@Override
	public Multi<OrgPartyRight> findAllByPartyId(String id) {
    final var sql = sqlBuilder.orgPartyRights().findAllByGroupId(id);
    if(log.isDebugEnabled()) {
      log.debug("GroupRole findAllByGroupId query, with props: {} \r\n{}", 
          "",
          sql.getValue());
    }
    return wrapper.getClient().preparedQuery(sql.getValue())
        .mapping(row -> sqlMapper.orgPartyRright(row))
        .execute(sql.getProps())
        .onItem()
        .transformToMulti((RowSet<OrgPartyRight> rowset) -> Multi.createFrom().iterable(rowset))
        .onFailure().invoke(e -> errorHandler.deadEnd(new SqlTupleFailed("Can't find 'PARTY_RIGHTS'!", sql, e)));
	}

	@Override
	public Multi<OrgPartyRight> findAllByRightId(String id) {
    final var sql = sqlBuilder.orgPartyRights().findAllByRoleId(id);
    if(log.isDebugEnabled()) {
      log.debug("UserRole findAllByRoleId query, with props: {} \r\n{}", 
          "",
          sql.getValue());
    }
    return wrapper.getClient().preparedQuery(sql.getValue())
        .mapping(row -> sqlMapper.orgPartyRright(row))
        .execute(sql.getProps())
        .onItem()
        .transformToMulti((RowSet<OrgPartyRight> rowset) -> Multi.createFrom().iterable(rowset))
        .onFailure().invoke(e -> errorHandler.deadEnd(new SqlTupleFailed("Can't find 'PARTY_RIGHTS'!", sql, e)));
	}
}
