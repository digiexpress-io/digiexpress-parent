package io.digiexpress.thena.mq.client.spi.persistence;

import java.util.List;

import io.digiexpress.thena.mq.client.api.ThenaMqLogConstants;
import io.digiexpress.thena.mq.client.api.entities.Delivery;
import io.digiexpress.thena.mq.client.api.entities.Delivery.DeliveryStatus;
import io.digiexpress.thena.mq.client.api.persistence.ThenaMqChannelState.InternalDeliveryQuery;
import io.digiexpress.thena.mq.client.api.persistence.ThenaMqDataSource;
import io.resys.thena.datasource.ThenaSqlDataSourceErrorHandler.SqlTupleFailed;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.RowSet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = ThenaMqLogConstants.SHOW_SQL)
@RequiredArgsConstructor
public class InternalDeliveryQueryImpl implements InternalDeliveryQuery {
 
  private final ThenaMqDataSource dataSource;


  @Override
  public Uni<List<Delivery>> findAllByAppIdAndStatus(String appId, DeliveryStatus status, boolean lockForUpdate) {
   final var sql = dataSource.getRegistry().delivery().findAllByAppIdAndStatus(appId, status, lockForUpdate);
    
    if(log.isDebugEnabled()) {
      log.debug("InternalMessageQueryImpl.findAllByAppIdAndDeliveryStatus query, with props: {} \r\n{}", 
          sql.getProps().deepToString(), 
          sql.getValue());
    }
    
    return dataSource.getClient().preparedQuery(sql.getValue())
        .mapping(dataSource.getRegistry().delivery().defaultMapper())
        .execute(sql.getProps())
        .onItem()
        .transformToUni((RowSet<Delivery> rowset) -> Multi.createFrom().iterable(rowset).collect().asList())
        .onFailure().invoke(e -> dataSource.getErrorHandler().deadEnd(new SqlTupleFailed("Can't find 'QUEUE_DELIVERIES' by 'app_id' and 'status' for update!", sql, e)));

  }
}
