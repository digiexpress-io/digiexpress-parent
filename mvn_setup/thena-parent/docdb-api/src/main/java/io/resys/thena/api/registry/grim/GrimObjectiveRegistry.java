package io.resys.thena.api.registry.grim;

import java.util.Collection;
import java.util.function.Function;

import io.resys.thena.api.entities.grim.GrimObjective;
import io.resys.thena.api.registry.ThenaRegistryService;
import io.resys.thena.datasource.ThenaSqlClient;


public interface GrimObjectiveRegistry extends ThenaRegistryService<GrimObjective, io.vertx.mutiny.sqlclient.Row> {
  ThenaSqlClient.Sql findAll();
  ThenaSqlClient.SqlTuple getById(String id);
  ThenaSqlClient.SqlTuple findAllByMissionId(Collection<String> id);
  ThenaSqlClient.SqlTupleList insertAll(Collection<GrimObjective> users);
  
  
  ThenaSqlClient.Sql createTable();
  ThenaSqlClient.Sql createConstraints();
  ThenaSqlClient.Sql dropTable();
  
  Function<io.vertx.mutiny.sqlclient.Row, GrimObjective> defaultMapper();
}