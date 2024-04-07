package io.resys.thena.api.registry.grim;

import java.util.Collection;
import java.util.function.Function;

import io.resys.thena.api.entities.grim.GrimObjectiveGoal;
import io.resys.thena.api.registry.ThenaRegistryService;
import io.resys.thena.datasource.ThenaSqlClient;


public interface GrimObjectiveGoalRegistry extends ThenaRegistryService<GrimObjectiveGoal, io.vertx.mutiny.sqlclient.Row> {
  ThenaSqlClient.Sql findAll();
  ThenaSqlClient.SqlTuple getById(String id);
  
  ThenaSqlClient.SqlTuple findAllByMissionIds(GrimMissionFilter filter);
  ThenaSqlClient.SqlTupleList insertAll(Collection<GrimObjectiveGoal> goals);
  ThenaSqlClient.SqlTupleList updateAll(Collection<GrimObjectiveGoal> goals);
  ThenaSqlClient.SqlTupleList deleteAll(Collection<GrimObjectiveGoal> goals);
  
  ThenaSqlClient.Sql createTable();
  ThenaSqlClient.Sql createConstraints();
  ThenaSqlClient.Sql dropTable();
  
  Function<io.vertx.mutiny.sqlclient.Row, GrimObjectiveGoal> defaultMapper();
}