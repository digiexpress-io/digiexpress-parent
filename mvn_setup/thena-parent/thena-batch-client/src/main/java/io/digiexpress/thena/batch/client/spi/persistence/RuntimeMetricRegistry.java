package io.digiexpress.thena.batch.client.spi.persistence;

import java.util.List;
import java.util.function.Function;

import io.digiexpress.thena.batch.client.api.entities.RuntimeMetric;
import io.resys.thena.api.registry.ThenaRegistryService;
import io.resys.thena.datasource.ThenaSqlClient;


public interface RuntimeMetricRegistry extends ThenaRegistryService<RuntimeMetric, io.vertx.mutiny.sqlclient.Row> {
  
  
  ThenaSqlClient.SqlTupleList insertMany(List<RuntimeMetric> metrics);
  
  @Override ThenaSqlClient.Sql findAll();
  @Override ThenaSqlClient.SqlTuple getById(String id);
  
  @Override ThenaSqlClient.Sql createTable();
  @Override ThenaSqlClient.Sql createConstraints();
  @Override ThenaSqlClient.Sql dropTable();
  
  @Override Function<io.vertx.mutiny.sqlclient.Row, RuntimeMetric> defaultMapper();
}
