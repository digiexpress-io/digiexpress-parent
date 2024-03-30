package io.resys.thena.api.registry.grim;

import java.util.function.Function;

import io.resys.thena.api.entities.grim.GrimAssignment;
import io.resys.thena.api.registry.ThenaRegistryService;
import io.resys.thena.datasource.ThenaSqlClient;


public interface GrimAssignmentRegistry extends ThenaRegistryService<GrimAssignment, io.vertx.mutiny.sqlclient.Row> {
  ThenaSqlClient.Sql findAll();
  ThenaSqlClient.SqlTuple getById(String id);
  
  ThenaSqlClient.Sql createTable();
  ThenaSqlClient.Sql createConstraints();
  ThenaSqlClient.Sql dropTable();
  
  Function<io.vertx.mutiny.sqlclient.Row, GrimAssignment> defaultMapper();
}