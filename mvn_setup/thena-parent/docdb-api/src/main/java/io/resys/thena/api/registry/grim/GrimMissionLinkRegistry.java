package io.resys.thena.api.registry.grim;

import java.util.Collection;
import java.util.function.Function;

import io.resys.thena.api.entities.grim.GrimMissionLink;
import io.resys.thena.api.registry.ThenaRegistryService;
import io.resys.thena.datasource.ThenaSqlClient;


public interface GrimMissionLinkRegistry extends ThenaRegistryService<GrimMissionLink, io.vertx.mutiny.sqlclient.Row> {
  ThenaSqlClient.Sql findAll();
  ThenaSqlClient.SqlTuple getById(String id);
  
  ThenaSqlClient.SqlTuple findAllByMissionId(Collection<String> id);
  ThenaSqlClient.SqlTupleList insertAll(Collection<GrimMissionLink> users);
  
  ThenaSqlClient.Sql createTable();
  ThenaSqlClient.Sql createConstraints();
  ThenaSqlClient.Sql dropTable();
  
  Function<io.vertx.mutiny.sqlclient.Row, GrimMissionLink> defaultMapper();
}