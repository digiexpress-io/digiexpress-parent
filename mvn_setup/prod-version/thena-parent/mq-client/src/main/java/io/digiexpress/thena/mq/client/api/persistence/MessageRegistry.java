package io.digiexpress.thena.mq.client.api.persistence;

import java.util.List;
import java.util.function.Function;

import io.digiexpress.thena.mq.client.api.entities.QueueMessage;
import io.digiexpress.thena.mq.client.api.entities.QueueMessage.RoutingStatus;
import io.resys.thena.datasource.ThenaSqlClient;

public interface MessageRegistry extends ThenaMqRegistryTemplate<QueueMessage, io.vertx.mutiny.sqlclient.Row> {

  ThenaSqlClient.SqlTupleList insertMany(List<QueueMessage> docs);
  ThenaSqlClient.SqlTupleList updateMany(List<QueueMessage> docs);
  ThenaSqlClient.SqlTuple findAllByRoutingStatus(RoutingStatus status, boolean lockForUpdate);
  
  
  @Override ThenaSqlClient.SqlTuple getById(String id);  // matches by external_id or id
  @Override ThenaSqlClient.Sql findAll();
  @Override ThenaSqlClient.Sql createTable();
  @Override ThenaSqlClient.Sql createConstraints();
  @Override ThenaSqlClient.Sql dropTable();
  @Override Function<io.vertx.mutiny.sqlclient.Row, QueueMessage> defaultMapper();
}
