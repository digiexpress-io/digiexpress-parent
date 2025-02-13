package io.resys.thena.storesql.builders;

import java.util.Collection;

import io.resys.thena.api.LogConstants;
import io.resys.thena.api.entities.org.OrgMember;
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
public class OrgMemberQuerySqlPool implements OrgQueries.MemberQuery {
  private final ThenaSqlDataSource wrapper;
  private final OrgRegistry registry;
  private final ThenaSqlDataSourceErrorHandler errorHandler;
  
  public OrgMemberQuerySqlPool(ThenaSqlDataSource dataSource) {
    this.wrapper = dataSource;
    this.registry = dataSource.getRegistry().org();
    this.errorHandler = dataSource.getErrorHandler();
  }
  @Override
  public Multi<OrgMember> findAll() {
    final var sql = registry.orgMembers().findAll();
    if(log.isDebugEnabled()) {
      log.debug("User findAll query, with props: {} \r\n{}", 
          "",
          sql.getValue());
    }
    return wrapper.getClient().preparedQuery(sql.getValue())
        .mapping(registry.orgMembers().defaultMapper())
        .execute()
        .onItem()
        .transformToMulti((RowSet<OrgMember> rowset) -> Multi.createFrom().iterable(rowset))
        .onFailure().invoke(e -> errorHandler.deadEnd(new SqlFailed("Can't find 'MEMBER'!", sql, e)));
  }
  
  @Override
  public Multi<OrgMember> findAll(Collection<String> id) {
    final var sql = registry.orgMembers().findAll(id);
    if(log.isDebugEnabled()) {
      log.debug("User findAll query, with props: {} \r\n{}", 
          "",
          sql.getValue());
    }
    return wrapper.getClient().preparedQuery(sql.getValue())
        .mapping(registry.orgMembers().defaultMapper())
        .execute(sql.getProps())
        .onItem()
        .transformToMulti((RowSet<OrgMember> rowset) -> Multi.createFrom().iterable(rowset))
        .onFailure().invoke(e -> errorHandler.deadEnd(new SqlTupleFailed("Can't find 'MEMBER'!", sql, e)));
  }


  @Override
  public Uni<OrgMember> getById(String id) {
    final var sql = registry.orgMembers().getById(id);
    if(log.isDebugEnabled()) {
      log.debug("User byId query, with props: {} \r\n{}", 
          sql.getProps().deepToString(),
          sql.getValue());
    }
    return wrapper.getClient().preparedQuery(sql.getValue())
        .mapping(registry.orgMembers().defaultMapper())
        .execute(sql.getProps())
        .onItem()
        .transform((RowSet<OrgMember> rowset) -> {
          final var it = rowset.iterator();
          if(it.hasNext()) {
            return it.next();
          }
          return null;
        })
        .onFailure(e -> errorHandler.notFound(e)).recoverWithNull()
        .onFailure().invoke(e -> errorHandler.deadEnd(new SqlTupleFailed("Can't get 'MEMBER' by 'id': '" + id + "'!", sql, e)));
  }
  
  @Override
  public Multi<OrgMember> findAllByPartyId(String id) {
    final var sql = registry.orgMembers().findAllByPartyId(id);
    if(log.isDebugEnabled()) {
      log.debug("User findAllByPartyId query, with props: {} \r\n{}", 
          sql.getProps().deepToString(),
          sql.getValue());
    }
    return wrapper.getClient().preparedQuery(sql.getValue())
        .mapping(registry.orgMembers().defaultMapper())
        .execute(sql.getProps())
        .onItem()
        .transformToMulti((RowSet<OrgMember> rowset) -> Multi.createFrom().iterable(rowset))
        .onFailure().invoke(e -> errorHandler.deadEnd(new SqlTupleFailed("Can't get 'MEMBER' by 'groupId': '" + id + "'!", sql, e)));
  }
  @Override
  public Multi<OrgMember> findAllByRightId(String rightId) {
    final var sql = registry.orgMembers().findAllByRightId(rightId);
    if(log.isDebugEnabled()) {
      log.debug("User findAllByRightId query, with props: {} \r\n{}", 
          sql.getProps().deepToString(),
          sql.getValue());
    }
    return wrapper.getClient().preparedQuery(sql.getValue())
        .mapping(registry.orgMembers().defaultMapper())
        .execute(sql.getProps())
        .onItem()
        .transformToMulti((RowSet<OrgMember> rowset) -> Multi.createFrom().iterable(rowset))
        .onFailure().invoke(e -> errorHandler.deadEnd(new SqlTupleFailed("Can't get 'MEMBER' by 'rightId': '" + rightId + "'!", sql, e)));
  }
}
