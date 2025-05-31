package io.digiexpress.thena.batch.client.spi.persistence;

import java.util.List;
import java.util.function.Function;

import io.digiexpress.thena.batch.client.api.entities.RuntimeInstance.RuntimeStatus;
import io.digiexpress.thena.batch.client.api.entities.RuntimeStepRow;
import io.resys.thena.api.registry.ThenaRegistryService;
import io.resys.thena.datasource.ThenaSqlClient;


public interface RuntimeStepRowRegistry extends ThenaRegistryService<RuntimeStepRow, io.vertx.mutiny.sqlclient.Row> {
  
  ThenaSqlClient.SqlTuple findAllByInstanceStatus(List<RuntimeStatus> status);
  ThenaSqlClient.SqlTupleList insertMany(List<RuntimeStepRow> docs);
  
  @Override ThenaSqlClient.Sql findAll();
  @Override ThenaSqlClient.SqlTuple getById(String id);
  
  @Override ThenaSqlClient.Sql createTable();
  @Override ThenaSqlClient.Sql createConstraints();
  @Override ThenaSqlClient.Sql dropTable();
  
  @Override Function<io.vertx.mutiny.sqlclient.Row, RuntimeStepRow> defaultMapper();
}
