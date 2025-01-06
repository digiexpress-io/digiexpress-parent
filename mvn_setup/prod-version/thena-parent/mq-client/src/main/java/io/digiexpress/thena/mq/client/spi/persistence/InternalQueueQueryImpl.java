package io.digiexpress.thena.mq.client.spi.persistence;

import java.util.Optional;

import io.digiexpress.thena.mq.client.api.ThenaMqLogConstants;
import io.digiexpress.thena.mq.client.api.entities.Queue;
import io.digiexpress.thena.mq.client.api.persistence.ThenaMqChannelState.InternalQueueQuery;
import io.digiexpress.thena.mq.client.api.persistence.ThenaMqDataSource;
import io.resys.thena.datasource.ThenaSqlDataSourceErrorHandler.SqlTupleFailed;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.RowSet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j(topic = ThenaMqLogConstants.SHOW_SQL)
public class InternalQueueQueryImpl implements InternalQueueQuery {
  private final ThenaMqDataSource dataSource;
  
  @Override
  public Uni<Optional<Queue>> findByQueueName(String queueName) {
    final var sql = dataSource.getRegistry().queue().getById(queueName);
    
    if(log.isDebugEnabled()) {
      log.debug("InternalQueueQueryImpl.findByQueueName query, with props: {} \r\n{}", 
          sql.getProps().deepToString(), 
          sql.getValue());
    }
    
    return dataSource.getClient().preparedQuery(sql.getValue())
        .mapping(dataSource.getRegistry().queue().defaultMapper())
        .execute(sql.getProps())
        .onItem()
        .transform((RowSet<Queue> rowset) -> {
          final var it = rowset.iterator();
          if(it.hasNext()) {
            return Optional.ofNullable(it.next());
          }
          return Optional.<Queue>empty();
        })
        .onFailure().invoke(e -> dataSource.getErrorHandler().deadEnd(new SqlTupleFailed("Can't find 'QUEUE' by 'queue_name' or 'id'!", sql, e)));
  }

}
