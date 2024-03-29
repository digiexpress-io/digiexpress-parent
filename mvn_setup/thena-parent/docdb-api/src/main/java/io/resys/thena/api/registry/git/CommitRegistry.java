package io.resys.thena.api.registry.git;

import java.util.function.Function;

import io.resys.thena.api.entities.git.Commit;
import io.resys.thena.api.entities.git.CommitTree;
import io.resys.thena.api.registry.ThenaRegistryService;
import io.resys.thena.datasource.ThenaSqlClient;
import io.resys.thena.structures.git.GitQueries.LockCriteria;


public interface CommitRegistry extends ThenaRegistryService<Commit, io.vertx.mutiny.sqlclient.Row> {
  ThenaSqlClient.SqlTuple getById(String id);
  ThenaSqlClient.SqlTuple getLock(LockCriteria crit);
  ThenaSqlClient.Sql findAll();
  ThenaSqlClient.SqlTuple insertOne(Commit commit);
  
  ThenaSqlClient.Sql createTable();
  ThenaSqlClient.Sql createConstraints();
  ThenaSqlClient.Sql dropTable();
  

  Function<io.vertx.mutiny.sqlclient.Row, Commit> defaultMapper();
  Function<io.vertx.mutiny.sqlclient.Row, CommitTree> commitTreeMapper();
  Function<io.vertx.mutiny.sqlclient.Row, CommitTree> commitTreeWithBlobsMapper();
}