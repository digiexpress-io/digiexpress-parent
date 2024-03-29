package io.resys.thena.api.registry.org;

import java.util.function.Function;

import io.resys.thena.api.entities.org.OrgActorData;
import io.resys.thena.api.registry.ThenaRegistryService;
import io.resys.thena.datasource.ThenaSqlClient;


public interface OrgActorDataRegistry extends ThenaRegistryService<OrgActorData, io.vertx.mutiny.sqlclient.Row> {
  
  ThenaSqlClient.Sql createTable();
  ThenaSqlClient.Sql createConstraints();
  ThenaSqlClient.Sql dropTable();
  
  Function<io.vertx.mutiny.sqlclient.Row, OrgActorData> defaultMapper();
}