package io.resys.thena.api.registry.grim;

import java.util.Collection;
import java.util.function.Function;

import io.resys.thena.api.entities.grim.GrimMissionLabel;
import io.resys.thena.api.registry.ThenaRegistryService;
import io.resys.thena.datasource.ThenaSqlClient;


public interface GrimMissionLabelRegistry extends ThenaRegistryService<GrimMissionLabel, io.vertx.mutiny.sqlclient.Row> {
  ThenaSqlClient.Sql findAll();
  ThenaSqlClient.SqlTuple getById(String id);
  ThenaSqlClient.SqlTuple findAllByMissionIds(Collection<String> id);
  ThenaSqlClient.SqlTupleList insertAll(Collection<GrimMissionLabel> users);
  ThenaSqlClient.SqlTupleList deleteAll(Collection<GrimMissionLabel> users);
  
  
  ThenaSqlClient.Sql createTable();
  ThenaSqlClient.Sql createConstraints();
  ThenaSqlClient.Sql dropTable();
  
  Function<io.vertx.mutiny.sqlclient.Row, GrimMissionLabel> defaultMapper();
}