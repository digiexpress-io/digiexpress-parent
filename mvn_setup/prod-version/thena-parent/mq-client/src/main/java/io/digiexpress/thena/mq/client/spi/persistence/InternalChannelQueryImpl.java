package io.digiexpress.thena.mq.client.spi.persistence;

import java.util.Optional;

import io.digiexpress.thena.mq.client.api.ThenaMqLogConstants;
import io.digiexpress.thena.mq.client.api.entities.Channel;
import io.digiexpress.thena.mq.client.api.persistence.ThenaMqChannelState.InternalChannelQuery;
import io.digiexpress.thena.mq.client.api.persistence.ThenaMqDataSource;
import io.resys.thena.datasource.ThenaSqlClient;
import io.resys.thena.datasource.ThenaSqlDataSourceErrorHandler.SqlFailed;
import io.resys.thena.datasource.ThenaSqlDataSourceErrorHandler.SqlSchemaFailed;
import io.resys.thena.datasource.ThenaSqlDataSourceErrorHandler.SqlTupleFailed;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.RowSet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;



@Slf4j(topic = ThenaMqLogConstants.SHOW_SQL)
@RequiredArgsConstructor
public class InternalChannelQueryImpl implements InternalChannelQuery {
  private final ThenaMqDataSource dataSource;
  
  private ThenaSqlClient getClient() {
    return dataSource.getClient();
  }

  @Override
  public Uni<Optional<Channel>> getByNameOrId(String nameOrId) {
    final var sql = dataSource.getRegistry().channel().getByIdOrName(nameOrId);
    
    if(log.isDebugEnabled()) {
      log.debug("InternalChannelQueryImpl.getByNameOrId query, with props: {} \r\n{}", 
          sql.getProps().deepToString(), 
          sql.getValue());
    }
    
    return getClient().preparedQuery(sql.getValue())
        .mapping(dataSource.getRegistry().channel().defaultMapper())
        .execute(sql.getProps())
        .onItem()
        .transform((RowSet<Channel> rowset) -> {
          final var it = rowset.iterator();
          if(it.hasNext()) {
            return Optional.ofNullable(it.next());
          }
          return Optional.<Channel>empty();
        })
        .onFailure(e -> dataSource.getErrorHandler().notFound(e)).recoverWithItem(Optional.<Channel>empty())
        .onFailure().invoke(e -> dataSource.getErrorHandler().deadEnd(new SqlTupleFailed("Can't find 'CHANNEL' by 'name' or 'id'!", sql, e)));
  }
  
  @Override
  public Multi<Channel> findAll() {
    final var sql = this.dataSource.getRegistry().channel().findAll();
    if(log.isDebugEnabled()) {
      log.debug("InternalChannelQueryImpl.findAll, with props: {} \r\n{}", 
          "", 
          sql.getValue());
    }
    return getClient().preparedQuery(sql.getValue())
      .mapping(dataSource.getRegistry().channel().defaultMapper())
      .execute()
      .onItem()
      .transformToMulti((RowSet<Channel> rowset) -> Multi.createFrom().iterable(rowset))
      .onFailure(e -> dataSource.getErrorHandler().notFound(e)).recoverWithCompletion()
      .onFailure().invoke(e -> dataSource.getErrorHandler().deadEnd(new SqlFailed("Can't find 'CHANNEL'-s!", sql, e)));
  }
  
  
  @Override
  public Uni<Channel> delete(final Channel newRepo) {
    final var next = dataSource.withChannel(newRepo);
    final var reg = next.getRegistry();

    final var sqlQuery = next.getRegistry();
    final var pool = next.getPool();
    return pool.withTransaction(tx -> {
      final var tenantDelete = sqlQuery.channel().deleteById(newRepo.getId());
      final var tablesDrop = new StringBuilder()
        .append(reg.deliveryAttempt().dropTable().getValue())
        .append(reg.delivery().dropTable().getValue())
        .append(reg.message().dropTable().getValue())
        .append(reg.queue().dropTable().getValue());     
      
      
      if(log.isDebugEnabled()) {
        log.debug("Delete 'CHANNEL' by name, with props: {} \r\n{}", 
            tenantDelete.getProps().deepToString(), 
            tenantDelete.getValue());
        
        log.debug(new StringBuilder("Drop schema: ")
            .append(System.lineSeparator())
            .append(tablesDrop.toString())
            .toString());
      }
      
      final Uni<Void> insert = tx.preparedQuery(tenantDelete.getValue()).execute(tenantDelete.getProps())
          .onItem().transformToUni(rowSet -> Uni.createFrom().voidItem())
          .onFailure().invoke(e -> next.getErrorHandler().deadEnd(new SqlTupleFailed("Can't delete from 'CHANNEL'!", tenantDelete, e)));
      final Uni<Void> nested = tx.query(tablesDrop.toString()).execute()
          .onItem().transformToUni(rowSet -> Uni.createFrom().voidItem())
          .onFailure().invoke(e -> next.getErrorHandler().deadEnd(new SqlSchemaFailed("Can't drop 'CHANNEL' tables!", tablesDrop.toString(), e)));
      
      return insert
          .onItem().transformToUni(junk -> nested)
          .onItem().transform(junk -> newRepo);
    });
  }

  @Override
  public Uni<Void> delete() {
    final var tenantDelete = dataSource.getRegistry().channel().dropTable();
    final var pool = dataSource.getPool();
    return  pool.query(tenantDelete.getValue()).execute()
        .onItem().transformToUni(rowSet -> Uni.createFrom().voidItem())
        .onFailure().invoke(e -> dataSource.getErrorHandler().deadEnd(new SqlSchemaFailed("Can't drop 'CHANNEL' table!", tenantDelete.getValue(), e)));
    
  }
}
