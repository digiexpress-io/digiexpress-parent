package io.digiexpress.thena.mq.client.api.persistence;

import java.util.List;
import java.util.function.Function;

import io.digiexpress.thena.mq.client.api.entities.Channel;
import io.resys.thena.datasource.ThenaSqlClient;


public interface ChannelRegistry extends ThenaMqRegistryTemplate<Channel, io.vertx.mutiny.sqlclient.Row> {
  ThenaSqlClient.SqlTuple deleteById(String id);
  ThenaSqlClient.SqlTuple insertOne(Channel docs);
  ThenaSqlClient.SqlTupleList updateMany(List<Channel> docs);
  
  @Override ThenaSqlClient.SqlTuple getByIdOrName(String idOrName);  // matches by external_id or id
  @Override ThenaSqlClient.Sql findAll();
  @Override ThenaSqlClient.Sql createTable();
  @Override ThenaSqlClient.Sql createConstraints();
  @Override ThenaSqlClient.Sql dropTable();
  @Override Function<io.vertx.mutiny.sqlclient.Row, Channel> defaultMapper();
}