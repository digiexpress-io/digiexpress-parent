package io.resys.thena.api.registry.grim;

import java.util.function.Function;

import io.resys.thena.api.entities.grim.GrimObjective;
import io.resys.thena.api.registry.ThenaRegistryService;
import io.resys.thena.datasource.ThenaSqlClient;


public interface GrimObjectiveRegistry extends ThenaRegistryService<GrimObjective, io.vertx.mutiny.sqlclient.Row> {
  ThenaSqlClient.Sql findAll();
  ThenaSqlClient.SqlTuple getById(String id);
  
  ThenaSqlClient.Sql createTable();
  ThenaSqlClient.Sql createConstraints();
  ThenaSqlClient.Sql dropTable();
  
  Function<io.vertx.mutiny.sqlclient.Row, GrimObjective> defaultMapper();
}