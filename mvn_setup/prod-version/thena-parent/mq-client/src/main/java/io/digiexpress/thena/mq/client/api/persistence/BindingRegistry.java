package io.digiexpress.thena.mq.client.api.persistence;

import java.util.List;
import java.util.function.Function;

import io.digiexpress.thena.mq.client.api.entities.Binding;
import io.resys.thena.datasource.ThenaSqlClient;

public interface BindingRegistry extends ThenaMqRegistryTemplate<Binding, io.vertx.mutiny.sqlclient.Row>  {
  ThenaSqlClient.SqlTupleList insertMany(List<Binding> bindings);
  ThenaSqlClient.SqlTuple deleteById(String id);
  
  @Override ThenaSqlClient.SqlTuple getById(String id);  // matches by external_id or id
  @Override ThenaSqlClient.Sql findAll();
  @Override ThenaSqlClient.Sql createTable();
  @Override ThenaSqlClient.Sql createConstraints();
  @Override ThenaSqlClient.Sql dropTable();
  @Override Function<io.vertx.mutiny.sqlclient.Row, Binding> defaultMapper();
}
