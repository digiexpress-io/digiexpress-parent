package io.resys.thena.api.registry.org;

import java.util.Collection;
import java.util.function.Function;

import io.resys.thena.api.entities.org.OrgCommit;
import io.resys.thena.api.registry.ThenaRegistryService;
import io.resys.thena.datasource.ThenaSqlClient;


public interface OrgCommitRegistry extends ThenaRegistryService<OrgCommit, io.vertx.mutiny.sqlclient.Row> {
  ThenaSqlClient.SqlTuple getById(String id);
  ThenaSqlClient.Sql findAll();
  ThenaSqlClient.SqlTuple insertOne(OrgCommit commit);
  ThenaSqlClient.SqlTupleList insertAll(Collection<OrgCommit> commit);
  
  ThenaSqlClient.Sql createTable();
  ThenaSqlClient.Sql createConstraints();
  ThenaSqlClient.Sql dropTable();
  
  Function<io.vertx.mutiny.sqlclient.Row, OrgCommit> defaultMapper();
  
}