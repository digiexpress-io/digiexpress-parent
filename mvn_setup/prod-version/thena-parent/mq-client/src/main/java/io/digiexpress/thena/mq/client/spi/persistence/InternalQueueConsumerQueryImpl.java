package io.digiexpress.thena.mq.client.spi.persistence;

import java.util.List;

import io.digiexpress.thena.mq.client.api.ThenaMqLogConstants;
import io.digiexpress.thena.mq.client.api.entities.QueueConsumer;
import io.digiexpress.thena.mq.client.api.persistence.ThenaMqChannelState.InternalQueueConsumerQuery;
import io.digiexpress.thena.mq.client.api.persistence.ThenaMqDataSource;
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
  public Uni<List<QueueConsumer>> findByQueueNameAndAppId(String queueName, String appId, boolean lockForUpdate) {
    
    final var sql = dataSource.getRegistry().queueConsumer().findAllByQueueNameAndAppId(queueName, appId, lockForUpdate);
    
    if(log.isDebugEnabled()) {
      log.debug("InternalQueueConsumerQueryImpl.findByQueueNameAndAppId query, with props: {} \r\n{}", 
          sql.getProps().deepToString(), 
          sql.getValue());
    }
    
    return dataSource.getClient().preparedQuery(sql.getValue())
        .mapping(dataSource.getRegistry().queueConsumer().defaultMapper())
        .execute(sql.getProps())
        .onItem()
        .transformToUni((RowSet<QueueConsumer> rowset) -> Multi.createFrom().iterable(rowset).collect().asList())
        .onFailure().invoke(e -> dataSource.getErrorHandler()
            .deadEnd(new SqlTupleFailed("Can't find 'QUEUE_CONSUMER' by 'queue_name/app_d'!", sql, e)));
  }
}
