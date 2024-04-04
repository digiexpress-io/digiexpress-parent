package io.resys.thena.api.registry.grim;

import java.util.Collection;
import java.util.function.Function;

import io.resys.thena.api.entities.grim.GrimMissionData;
import io.resys.thena.api.registry.ThenaRegistryService;
import io.resys.thena.datasource.ThenaSqlClient;


public interface GrimMissionDataRegistry extends ThenaRegistryService<GrimMissionData, io.vertx.mutiny.sqlclient.Row> {
  ThenaSqlClient.Sql findAll();
  ThenaSqlClient.SqlTuple getById(String id);
  ThenaSqlClient.SqlTuple findAllByMissionIds(Collection<String> id);
  ThenaSqlClient.SqlTupleList insertAll(Collection<GrimMissionData> data);
  ThenaSqlClient.SqlTupleList updateAll(Collection<GrimMissionData> data);
  ThenaSqlClient.SqlTupleList deleteAll(Collection<GrimMissionData> users);
  
  ThenaSqlClient.Sql createTable();
  ThenaSqlClient.Sql createConstraints();
  ThenaSqlClient.Sql dropTable();
  
  Function<io.vertx.mutiny.sqlclient.Row, GrimMissionData> defaultMapper();
}