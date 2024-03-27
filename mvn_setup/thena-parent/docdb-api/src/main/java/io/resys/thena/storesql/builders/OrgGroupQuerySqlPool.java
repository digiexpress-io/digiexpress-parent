package io.resys.thena.storesql.builders;

import java.util.Collection;

import io.resys.thena.api.LogConstants;
import io.resys.thena.api.entities.org.OrgParty;
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
public class OrgGroupQuerySqlPool implements OrgQueries.PartyQuery {
  private final ThenaSqlDataSource wrapper;
  private final SqlDataMapper sqlMapper;
  private final SqlQueryBuilder sqlBuilder;
  private final ThenaSqlDataSourceErrorHandler errorHandler;
  public OrgGroupQuerySqlPool(ThenaSqlDataSource dataSource) {
    this.wrapper = dataSource;
    this.sqlMapper = dataSource.getDataMapper();
    this.sqlBuilder = dataSource.getQueryBuilder();
    this.errorHandler = dataSource.getErrorHandler();
  }
  @Override
  public Multi<OrgParty> findAll() {
    final var sql = sqlBuilder.orgParties().findAll();
    if(log.isDebugEnabled()) {
      log.debug("Group findAll query, with props: {} \r\n{}", 
          "",
          sql.getValue());
    }
    return wrapper.getClient().preparedQuery(sql.getValue())
        .mapping(row -> sqlMapper.orgParty(row))
        .execute()
        .onItem()
        .transformToMulti((RowSet<OrgParty> rowset) -> Multi.createFrom().iterable(rowset))
        .onFailure().invoke(e -> errorHandler.deadEnd(new SqlFailed("Can't find 'PARTY'!", sql, e)));
  }
  
  @Override
  public Multi<OrgParty> findAll(Collection<String> id) {
    final var sql = sqlBuilder.orgParties().findAll(id);
    if(log.isDebugEnabled()) {
      log.debug("Group findAll query, with props: {} \r\n{}", 
          "",
          sql.getValue());
    }
    return wrapper.getClient().preparedQuery(sql.getValue())
        .mapping(row -> sqlMapper.orgParty(row))
        .execute(sql.getProps())
        .onItem()
        .transformToMulti((RowSet<OrgParty> rowset) -> Multi.createFrom().iterable(rowset))
        .onFailure().invoke(e -> errorHandler.deadEnd(new SqlTupleFailed("Can't find 'PARTY'!", sql, e)));
  }

  @Override
  public Uni<OrgParty> getById(String id) {
    final var sql = sqlBuilder.orgParties().getById(id);
    if(log.isDebugEnabled()) {
      log.debug("Group byId query, with props: {} \r\n{}", 
          sql.getProps().deepToString(),
          sql.getValue());
    }
    return wrapper.getClient().preparedQuery(sql.getValue())
        .mapping(row -> sqlMapper.orgParty(row))
        .execute(sql.getProps())
        .onItem()
        .transform((RowSet<OrgParty> rowset) -> {
          final var it = rowset.iterator();
          if(it.hasNext()) {
            return it.next();
          }
          return null;
        })
        .onFailure(e -> errorHandler.notFound(e)).recoverWithNull()
        .onFailure().invoke(e -> errorHandler.deadEnd(new SqlTupleFailed("Can't get 'PARTY' by 'id': '" + id + "'!", sql, e)));
  }
}
