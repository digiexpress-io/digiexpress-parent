package io.digiexpress.thena.batch.client.spi.persistence;

import java.util.List;
import java.util.function.Function;

import io.digiexpress.thena.batch.client.api.entities.RuntimeParams;
import io.resys.thena.api.registry.ThenaRegistryService;
import io.resys.thena.datasource.ThenaSqlClient;


public interface RuntimeParamsRegistry extends ThenaRegistryService<RuntimeParams, io.vertx.mutiny.sqlclient.Row> {
  
  
  ThenaSqlClient.SqlTupleList insertMany(List<RuntimeParams> docs);
  
  @Override ThenaSqlClient.Sql findAll();
  @Override ThenaSqlClient.SqlTuple getById(String id);
  
  @Override ThenaSqlClient.Sql createTable();
  @Override ThenaSqlClient.Sql createConstraints();
  @Override ThenaSqlClient.Sql dropTable();
  
  @Override Function<io.vertx.mutiny.sqlclient.Row, RuntimeParams> defaultMapper();
}
