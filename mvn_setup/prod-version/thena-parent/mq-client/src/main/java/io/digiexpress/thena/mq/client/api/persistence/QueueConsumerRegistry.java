package io.digiexpress.thena.mq.client.api.persistence;

import java.util.List;
import java.util.function.Function;

import io.digiexpress.thena.mq.client.api.entities.QueueConsumer;
import io.resys.thena.datasource.ThenaSqlClient;

public interface QueueConsumerRegistry extends ThenaMqRegistryTemplate<QueueConsumer, io.vertx.mutiny.sqlclient.Row> {

  ThenaSqlClient.SqlTupleList insertMany(List<QueueConsumer> queue);
  ThenaSqlClient.SqlTupleList updateMany(List<QueueConsumer> queue);
  ThenaSqlClient.SqlTuple deleteById(String id);  // matches by external_id or id
  ThenaSqlClient.SqlTuple findAllByAppId(String appId, boolean lockForUpdate);
  ThenaSqlClient.Sql findAllEnabled();  
  ThenaSqlClient.SqlTuple findAllEnabledByAppId(String appId);
  
  @Override ThenaSqlClient.SqlTuple getByIdOrName(String id);  // matches by external_id or id
  @Override ThenaSqlClient.Sql findAll();
  @Override ThenaSqlClient.Sql createTable();
  @Override ThenaSqlClient.Sql createConstraints();
  @Override ThenaSqlClient.Sql dropTable();
  @Override Function<io.vertx.mutiny.sqlclient.Row, QueueConsumer> defaultMapper();

}
