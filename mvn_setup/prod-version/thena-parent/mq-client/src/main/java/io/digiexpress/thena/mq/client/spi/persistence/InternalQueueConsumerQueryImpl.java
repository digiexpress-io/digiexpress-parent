package io.digiexpress.thena.mq.client.spi.persistence;

import java.util.List;

import io.digiexpress.thena.mq.client.api.ThenaMqLogConstants;
import io.digiexpress.thena.mq.client.api.entities.QueueConsumer;
import io.digiexpress.thena.mq.client.api.persistence.ThenaMqChannelState.InternalQueueConsumerQuery;
import io.digiexpress.thena.mq.client.api.persistence.ThenaMqDataSource;
import io.resys.thena.datasource.ThenaSqlDataSourceErrorHandler.SqlFailed;
import io.resys.thena.datasource.ThenaSqlDataSourceErrorHandler.SqlTupleFailed;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.RowSet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@RequiredArgsConstructor
@Slf4j(topic = ThenaMqLogConstants.SHOW_SQL)
public class InternalQueueConsumerQueryImpl implements InternalQueueConsumerQuery {
  private final ThenaMqDataSource dataSource;
  
  
  @Override
  public Uni<List<QueueConsumer>> findAllByAppId(String appId, boolean lockForUpdate) {
    
    final var sql = dataSource.getRegistry().queueConsumer().findAllByAppId(appId, lockForUpdate);
    
    if(log.isDebugEnabled()) {
      log.debug("InternalQueueConsumerQueryImpl.findByAppId query, with props: {} \r\n{}", 
          sql.getProps().deepToString(), 
          sql.getValue());
    }
    
    return dataSource.getClient().preparedQuery(sql.getValue())
        .mapping(dataSource.getRegistry().queueConsumer().defaultMapper())
        .execute(sql.getProps())
        .onItem()
        .transformToUni((RowSet<QueueConsumer> rowset) -> Multi.createFrom().iterable(rowset).collect().asList())
        .onFailure().invoke(e -> dataSource.getErrorHandler()
            .deadEnd(new SqlTupleFailed("Can't find 'QUEUE_CONSUMER' by 'app_id'!", sql, e)));
  }


  @Override
  public Uni<List<QueueConsumer>> findAllEnabled() {
    
    final var sql = dataSource.getRegistry().queueConsumer().findAllEnabled();
    
    if(log.isDebugEnabled()) {
      log.debug("InternalQueueConsumerQueryImpl.findAllEnabled query, with props: {} \r\n{}", 
          "", 
          sql.getValue());
    }
    
    return dataSource.getClient().preparedQuery(sql.getValue())
        .mapping(dataSource.getRegistry().queueConsumer().defaultMapper())
        .execute()
        .onItem()
        .transformToUni((RowSet<QueueConsumer> rowset) -> Multi.createFrom().iterable(rowset).collect().asList())
        .onFailure().invoke(e -> dataSource.getErrorHandler()
            .deadEnd(new SqlFailed("Can't find enabled 'QUEUE_CONSUMER'-s!", sql, e)));
  }


  @Override
  public Uni<List<QueueConsumer>> findAllEnabled(String appId) {
    final var sql = dataSource.getRegistry().queueConsumer().findAllEnabledByAppId(appId);
    
    if(log.isDebugEnabled()) {
      log.debug("InternalQueueConsumerQueryImpl.findAllEnabled query, with props: {} \r\n{}", 
          sql.getPropsDeepString(), 
          sql.getValue());
    }
    
    return dataSource.getClient().preparedQuery(sql.getValue())
        .mapping(dataSource.getRegistry().queueConsumer().defaultMapper())
        .execute()
        .onItem()
        .transformToUni((RowSet<QueueConsumer> rowset) -> Multi.createFrom().iterable(rowset).collect().asList())
        .onFailure().invoke(e -> dataSource.getErrorHandler()
            .deadEnd(new SqlTupleFailed("Can't find enabled 'QUEUE_CONSUMER'-s by appId!", sql, e)));
  }
}
