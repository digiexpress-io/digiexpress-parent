package io.digiexpress.thena.batch.client.spi.persistence;

import java.util.List;
import java.util.function.Function;

import io.digiexpress.thena.batch.client.api.entities.RuntimeInstance;
import io.digiexpress.thena.batch.client.api.entities.RuntimeInstance.RuntimeStatus;
import io.resys.thena.api.registry.ThenaRegistryService;
import io.resys.thena.datasource.ThenaSqlClient;


public interface RuntimeInstanceRegistry extends ThenaRegistryService<RuntimeInstance, io.vertx.mutiny.sqlclient.Row> {
  
  ThenaSqlClient.Sql getNextRefSequence();
  
  ThenaSqlClient.SqlTuple findAllByStatus(List<RuntimeStatus> status);
  ThenaSqlClient.SqlTuple getById(String id, boolean lockForUpdate);
  ThenaSqlClient.SqlTuple getNextRefSequence(long howMany);
  ThenaSqlClient.SqlTupleList insertMany(List<RuntimeInstance> instances);
  ThenaSqlClient.SqlTupleList updateMany(List<RuntimeInstance> instances);
  
  @Override ThenaSqlClient.Sql findAll();
  @Override ThenaSqlClient.SqlTuple getById(String id);
  
  @Override ThenaSqlClient.Sql createTable();
  @Override ThenaSqlClient.Sql createConstraints();
  @Override ThenaSqlClient.Sql dropTable();
  
  @Override Function<io.vertx.mutiny.sqlclient.Row, RuntimeInstance> defaultMapper();
}
