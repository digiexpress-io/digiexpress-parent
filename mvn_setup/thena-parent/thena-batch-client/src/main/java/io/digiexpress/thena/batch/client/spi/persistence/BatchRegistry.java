package io.digiexpress.thena.batch.client.spi.persistence;

import java.util.List;
import java.util.function.Function;

import io.digiexpress.thena.batch.client.api.entities.Batch;
import io.resys.thena.api.registry.ThenaRegistryService;
import io.resys.thena.datasource.ThenaSqlClient;


public interface BatchRegistry extends ThenaRegistryService<Batch, io.vertx.mutiny.sqlclient.Row> {

  
  ThenaSqlClient.SqlTuple findAllByAppId(String appId, boolean lockForUpdate);
  ThenaSqlClient.SqlTuple findOneByAppIdAndName(String appId, String batchName);
  
  ThenaSqlClient.SqlTupleList insertMany(List<Batch> docs);
  ThenaSqlClient.SqlTupleList updateMany(List<Batch> docs);
  ThenaSqlClient.SqlTuple deleteById(String id);
  
  @Override ThenaSqlClient.Sql findAll();
  @Override ThenaSqlClient.SqlTuple getById(String id);
  
  @Override ThenaSqlClient.Sql createTable();
  @Override ThenaSqlClient.Sql createConstraints();
  @Override ThenaSqlClient.Sql dropTable();
  
  @Override Function<io.vertx.mutiny.sqlclient.Row, Batch> defaultMapper();
}
