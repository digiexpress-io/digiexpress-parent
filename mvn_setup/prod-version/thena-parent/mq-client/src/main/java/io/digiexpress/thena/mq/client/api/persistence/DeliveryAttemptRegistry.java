package io.digiexpress.thena.mq.client.api.persistence;

import java.util.List;
import java.util.function.Function;

import io.digiexpress.thena.mq.client.api.entities.Delivery.DeliveryAttempt;
import io.resys.thena.datasource.ThenaSqlClient;


public interface DeliveryAttemptRegistry extends ThenaMqRegistryTemplate<DeliveryAttempt, io.vertx.mutiny.sqlclient.Row>  {

  ThenaSqlClient.SqlTupleList updateMany(List<DeliveryAttempt> attempts);
  ThenaSqlClient.SqlTupleList insertMany(List<DeliveryAttempt> attempts);
  
  @Override ThenaSqlClient.SqlTuple getById(String id);  // matches by external_id or id
  @Override ThenaSqlClient.Sql findAll();
  @Override ThenaSqlClient.Sql createTable();
  @Override ThenaSqlClient.Sql createConstraints();
  @Override ThenaSqlClient.Sql dropTable();
  @Override Function<io.vertx.mutiny.sqlclient.Row, DeliveryAttempt> defaultMapper();
}
