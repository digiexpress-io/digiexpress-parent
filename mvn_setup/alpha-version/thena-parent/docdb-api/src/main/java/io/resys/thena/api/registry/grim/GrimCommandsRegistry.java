package io.resys.thena.api.registry.grim;

import java.util.Collection;
import java.util.function.Function;

import io.resys.thena.api.entities.grim.GrimCommands;
import io.resys.thena.api.registry.ThenaRegistryService;
import io.resys.thena.datasource.ThenaSqlClient;


public interface GrimCommandsRegistry extends ThenaRegistryService<GrimCommands, io.vertx.mutiny.sqlclient.Row> {
  ThenaSqlClient.Sql findAll();
  ThenaSqlClient.SqlTuple findAllByIds(Collection<String> id);
  ThenaSqlClient.SqlTuple findAllByMissionIds(GrimMissionFilter filter);
  ThenaSqlClient.SqlTupleList insertAll(Collection<GrimCommands> commits);  
  ThenaSqlClient.SqlTuple getById(String id);
  
  ThenaSqlClient.Sql createTable();
  ThenaSqlClient.Sql createConstraints();
  ThenaSqlClient.Sql dropTable();
  
  Function<io.vertx.mutiny.sqlclient.Row, GrimCommands> defaultMapper();
}