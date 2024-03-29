package io.resys.thena.storesql.builders;

import java.util.List;

import io.resys.thena.api.LogConstants;
import io.resys.thena.api.entities.org.OrgMemberRight;
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
public class OrgMemberRightsQueryImpl implements OrgQueries.MemberRightsQuery {
  private final ThenaSqlDataSource wrapper;
  private final OrgRegistry registry;
  private final ThenaSqlDataSourceErrorHandler errorHandler;
  
  public OrgMemberRightsQueryImpl(ThenaSqlDataSource dataSource) {
    this.wrapper = dataSource;
    this.registry = dataSource.getRegistry().org();
    this.errorHandler = dataSource.getErrorHandler();
  }
  @Override
  public Multi<OrgMemberRight> findAll() {
    final var sql = registry.orgMemberRights().findAll();
    if(log.isDebugEnabled()) {
      log.debug("MemberRight findAll query, with props: {} \r\n{}", 
          "",
          sql.getValue());
    }
    return wrapper.getClient().preparedQuery(sql.getValue())
        .mapping(registry.orgMemberRights().defaultMapper())
        .execute()
        .onItem()
        .transformToMulti((RowSet<OrgMemberRight> rowset) -> Multi.createFrom().iterable(rowset))
        .onFailure().invoke(e -> errorHandler.deadEnd(new SqlFailed("Can't find 'MEMBER_RIGHT'!", sql, e)));
  }
  
  @Override
  public Multi<OrgMemberRight> findAll(List<String> id) {
    final var sql = registry.orgMemberRights().findAll(id);
    if(log.isDebugEnabled()) {
      log.debug("MemberRight findAll query, with props: {} \r\n{}", 
          sql.getPropsDeepString(),
          sql.getValue());
    }
    return wrapper.getClient().preparedQuery(sql.getValue())
        .mapping(registry.orgMemberRights().defaultMapper())
        .execute(sql.getProps())
        .onItem()
        .transformToMulti((RowSet<OrgMemberRight> rowset) -> Multi.createFrom().iterable(rowset))
        .onFailure().invoke(e -> errorHandler.deadEnd(new SqlTupleFailed("Can't find 'MEMBER_RIGHT'!", sql, e)));
  }

  @Override
  public Uni<OrgMemberRight> getById(String id) {
    final var sql = registry.orgMemberRights().getById(id);
    if(log.isDebugEnabled()) {
      log.debug("MemberRight getById query, with props: {} \r\n{}", 
          sql.getProps().deepToString(),
          sql.getValue());
    }
    return wrapper.getClient().preparedQuery(sql.getValue())
        .mapping(registry.orgMemberRights().defaultMapper())
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
    final var sql = registry.orgMemberRights().findAllByUserId(id);
    if(log.isDebugEnabled()) {
      log.debug("MemberRight findAllByMemberId query, with props: {} \r\n{}", 
          id,
          sql.getValue());
    }
    return wrapper.getClient().preparedQuery(sql.getValue())
        .mapping(registry.orgMemberRights().defaultMapper())
        .execute(sql.getProps())
        .onItem()
        .transformToMulti((RowSet<OrgMemberRight> rowset) -> Multi.createFrom().iterable(rowset))
        .onFailure().invoke(e -> errorHandler.deadEnd(new SqlTupleFailed("Can't find 'MEMBER_RIGHT'!", sql, e)));
	}

	@Override
	public Multi<OrgMemberRight> findAllByRightId(String id) {
    final var sql = registry.orgMemberRights().findAllByRoleId(id);
    if(log.isDebugEnabled()) {
      log.debug("MemberRight findAllByRightId query, with props: {} \r\n{}", 
          id,
          sql.getValue());
    }
    return wrapper.getClient().preparedQuery(sql.getValue())
        .mapping(registry.orgMemberRights().defaultMapper())
        .execute(sql.getProps())
        .onItem()
        .transformToMulti((RowSet<OrgMemberRight> rowset) -> Multi.createFrom().iterable(rowset))
        .onFailure().invoke(e -> errorHandler.deadEnd(new SqlTupleFailed("Can't find 'MEMBER_RIGHT'!", sql, e)));
	}

  @Override
  public Multi<OrgMemberRight> findAllByPartyId(String partyId) {
    final var sql = registry.orgMemberRights().findAllByPartyId(partyId);
    if(log.isDebugEnabled()) {
      log.debug("MemberRight findAllByPartyId query, with props: {} \r\n{}", 
          partyId,
          sql.getValue());
    }
    return wrapper.getClient().preparedQuery(sql.getValue())
        .mapping(registry.orgMemberRights().defaultMapper())
        .execute(sql.getProps())
        .onItem()
        .transformToMulti((RowSet<OrgMemberRight> rowset) -> Multi.createFrom().iterable(rowset))
        .onFailure().invoke(e -> errorHandler.deadEnd(new SqlTupleFailed("Can't find 'MEMBER_RIGHT'!", sql, e)));
  }
}
