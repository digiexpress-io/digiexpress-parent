package io.resys.thena.api.registry;

import java.util.function.Function;

import io.resys.thena.api.entities.Tenant;
import io.resys.thena.datasource.ThenaSqlClient;


public interface TenantRegistry extends ThenaRegistryService<Tenant, io.vertx.mutiny.sqlclient.Row> {
  
  ThenaSqlClient.SqlTuple exists();
  ThenaSqlClient.Sql findAll();
  ThenaSqlClient.SqlTuple getByName(String name);
  ThenaSqlClient.SqlTuple getByNameOrId(String name);
  ThenaSqlClient.SqlTuple insertOne(Tenant repo);
  ThenaSqlClient.SqlTuple deleteOne(Tenant repo);
  
  ThenaSqlClient.Sql createTable();
  ThenaSqlClient.Sql createConstraints();
  ThenaSqlClient.Sql dropTable();
  
  Function<io.vertx.mutiny.sqlclient.Row, Tenant> defaultMapper();
  
}