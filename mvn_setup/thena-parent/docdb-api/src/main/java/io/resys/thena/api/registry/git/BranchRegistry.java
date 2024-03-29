package io.resys.thena.api.registry.git;

import java.util.function.Function;

import io.resys.thena.api.entities.git.Branch;
import io.resys.thena.api.entities.git.Commit;
import io.resys.thena.api.registry.ThenaRegistryService;
import io.resys.thena.datasource.ThenaSqlClient;


public interface BranchRegistry extends ThenaRegistryService<Branch, io.vertx.mutiny.sqlclient.Row> {
  ThenaSqlClient.SqlTuple getByName(String name);
  ThenaSqlClient.SqlTuple getByNameOrCommit(String refNameOrCommit);
  ThenaSqlClient.Sql getFirst();
  ThenaSqlClient.Sql findAll();
  ThenaSqlClient.SqlTuple insertOne(Branch ref);
  ThenaSqlClient.SqlTuple updateOne(Branch ref, Commit commit);
  
  ThenaSqlClient.Sql createTable();
  ThenaSqlClient.Sql createConstraints();
  ThenaSqlClient.Sql dropTable();
  
  Function<io.vertx.mutiny.sqlclient.Row, Branch> defaultMapper();
}