package io.resys.thena.api.registry.org;

import java.util.Collection;
import java.util.function.Function;

import io.resys.thena.api.entities.org.OrgCommitTree;
import io.resys.thena.api.registry.ThenaRegistryService;
import io.resys.thena.datasource.ThenaSqlClient;


public interface OrgCommitTreeRegistry extends ThenaRegistryService<OrgCommitTree, io.vertx.mutiny.sqlclient.Row> {
  ThenaSqlClient.SqlTuple getById(String id);
  ThenaSqlClient.SqlTuple findByCommmitId(String commitId);
  ThenaSqlClient.Sql findAll();
  ThenaSqlClient.SqlTupleList insertAll(Collection<OrgCommitTree> tree);
  
  ThenaSqlClient.Sql createTable();
  ThenaSqlClient.Sql createConstraints();
  ThenaSqlClient.Sql dropTable();
  
  Function<io.vertx.mutiny.sqlclient.Row, OrgCommitTree> defaultMapper();
  
}