package io.digiexpress.thena.mq.client.api.persistence;

import java.util.List;
import java.util.function.Function;

import io.digiexpress.thena.mq.client.api.entities.Queue;
import io.resys.thena.datasource.ThenaSqlClient;

public interface QueueRegistry extends ThenaMqRegistryTemplate<Queue, io.vertx.mutiny.sqlclient.Row> {

  ThenaSqlClient.SqlTupleList insertMany(List<Queue> queue);
  ThenaSqlClient.SqlTuple deleteById(String id);  // matches by external_id or id
 
  @Override ThenaSqlClient.SqlTuple getById(String id);  // matches by external_id or id
  @Override ThenaSqlClient.Sql findAll();
  @Override ThenaSqlClient.Sql createTable();
  @Override ThenaSqlClient.Sql createConstraints();
  @Override ThenaSqlClient.Sql dropTable();
  @Override Function<io.vertx.mutiny.sqlclient.Row, Queue> defaultMapper();

}
