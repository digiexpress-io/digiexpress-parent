package io.digiexpress.thena.batch.client.spi.persistence;

import java.util.List;
import java.util.function.Function;

import io.digiexpress.thena.batch.client.api.entities.RuntimeStep;
import io.digiexpress.thena.batch.client.api.entities.RuntimeInstance.RuntimeStatus;
import io.resys.thena.api.registry.ThenaRegistryService;
import io.resys.thena.datasource.ThenaSqlClient;


public interface RuntimeStepRegistry extends ThenaRegistryService<RuntimeStep, io.vertx.mutiny.sqlclient.Row> {
  
  
  ThenaSqlClient.SqlTuple findAllByInstanceStatus(List<RuntimeStatus> status);
  ThenaSqlClient.SqlTuple findAllByInstanceId(String instanceId);
  ThenaSqlClient.SqlTuple getById(String id, boolean lockForUpdate);
  ThenaSqlClient.SqlTupleList insertMany(List<RuntimeStep> docs);
  ThenaSqlClient.SqlTupleList updateMany(List<RuntimeStep> docs);
  
  @Override ThenaSqlClient.Sql findAll();
  @Override ThenaSqlClient.SqlTuple getById(String id);
  
  @Override ThenaSqlClient.Sql createTable();
  @Override ThenaSqlClient.Sql createConstraints();
  @Override ThenaSqlClient.Sql dropTable();
  
  @Override Function<io.vertx.mutiny.sqlclient.Row, RuntimeStep> defaultMapper();
}
