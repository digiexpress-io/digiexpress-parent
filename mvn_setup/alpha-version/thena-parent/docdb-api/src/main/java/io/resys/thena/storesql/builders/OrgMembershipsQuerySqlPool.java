package io.resys.thena.storesql.builders;

import java.util.List;

import io.resys.thena.api.LogConstants;
import io.resys.thena.api.entities.org.OrgMembership;
import io.resys.thena.api.registry.OrgRegistry;
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
public class OrgMembershipsQuerySqlPool implements OrgQueries.MembershipQuery {
  private final ThenaSqlDataSource wrapper;
  private final OrgRegistry registry;
  private final ThenaSqlDataSourceErrorHandler errorHandler;
  
  public OrgMembershipsQuerySqlPool(ThenaSqlDataSource dataSource) {
    this.wrapper = dataSource;
    this.registry = dataSource.getRegistry().org();
    this.errorHandler = dataSource.getErrorHandler();
  }
  
  @Override
  public Multi<OrgMembership> findAll() {
    final var sql = registry.orgMemberships().findAll();
    if(log.isDebugEnabled()) {
      log.debug("UserMembership findAll query, with props: {} \r\n{}", 
          "",
          sql.getValue());
    }
    return wrapper.getClient().preparedQuery(sql.getValue())
        .mapping(registry.orgMemberships().defaultMapper())
        .execute()
        .onItem()
        .transformToMulti((RowSet<OrgMembership> rowset) -> Multi.createFrom().iterable(rowset))
        .onFailure().invoke(e -> errorHandler.deadEnd(new SqlFailed("Can't find 'MEMBERSHIP'!", sql, e)));
  }
  
  @Override
  public Multi<OrgMembership> findAll(List<String> id) {
    final var sql = registry.orgMemberships().findAll(id);
    if(log.isDebugEnabled()) {
      log.debug("UserMembership findAll query, with props: {} \r\n{}", 
          "",
          sql.getValue());
    }
    return wrapper.getClient().preparedQuery(sql.getValue())
        .mapping(registry.orgMemberships().defaultMapper())
        .execute(sql.getProps())
        .onItem()
        .transformToMulti((RowSet<OrgMembership> rowset) -> Multi.createFrom().iterable(rowset))
        .onFailure().invoke(e -> errorHandler.deadEnd(new SqlTupleFailed("Can't find 'MEMBERSHIP'!", sql, e)));
  }

  @Override
  public Uni<OrgMembership> getById(String id) {
    final var sql = registry.orgMemberships().getById(id);
    if(log.isDebugEnabled()) {
      log.debug("UserMembership byId query, with props: {} \r\n{}", 
          sql.getProps().deepToString(),
          sql.getValue());
    }
    return wrapper.getClient().preparedQuery(sql.getValue())
        .mapping(registry.orgMemberships().defaultMapper())
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
    final var sql = registry.orgMemberships().findAllByGroupId(id);
    if(log.isDebugEnabled()) {
      log.debug("UserMembership findAllByGroupId query, with props: {} \r\n{}", 
          "",
          sql.getValue());
    }
    return wrapper.getClient().preparedQuery(sql.getValue())
        .mapping(registry.orgMemberships().defaultMapper())
        .execute(sql.getProps())
        .onItem()
        .transformToMulti((RowSet<OrgMembership> rowset) -> Multi.createFrom().iterable(rowset))
        .onFailure().invoke(e -> errorHandler.deadEnd(new SqlTupleFailed("Can't find 'MEMBERSHIP'!", sql, e)));
	}

	@Override
	public Multi<OrgMembership> findAllByMemberId(String id) {
    final var sql = registry.orgMemberships().findAllByUserId(id);
    if(log.isDebugEnabled()) {
      log.debug("UserMembership findAllByUserId query, with props: {} \r\n{}", 
          "",
          sql.getValue());
    }
    return wrapper.getClient().preparedQuery(sql.getValue())
        .mapping(registry.orgMemberships().defaultMapper())
        .execute(sql.getProps())
        .onItem()
        .transformToMulti((RowSet<OrgMembership> rowset) -> Multi.createFrom().iterable(rowset))
        .onFailure().invoke(e -> errorHandler.deadEnd(new SqlTupleFailed("Can't find 'MEMBERSHIP'!", sql, e)));
	}
}
