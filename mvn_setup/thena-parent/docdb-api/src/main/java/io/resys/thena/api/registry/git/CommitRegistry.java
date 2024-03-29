package io.resys.thena.api.registry.git;

import java.util.function.Function;

import io.resys.thena.api.entities.git.Commit;
import io.resys.thena.api.entities.git.CommitTree;
import io.resys.thena.api.registry.ThenaRegistryService;
import io.resys.thena.datasource.SqlQueryBuilder.Sql;
import io.resys.thena.datasource.SqlQueryBuilder.SqlTuple;
import io.resys.thena.structures.git.GitQueries.LockCriteria;


public interface CommitRegistry extends ThenaRegistryService<Commit, io.vertx.mutiny.sqlclient.Row> {
  SqlTuple getById(String id);
  SqlTuple getLock(LockCriteria crit);
  Sql findAll();
  SqlTuple insertOne(Commit commit);
  
  Sql createTable();
  Sql createConstraints();
  Sql dropTable();
  

  Function<io.vertx.mutiny.sqlclient.Row, Commit> defaultMapper();
  Function<io.vertx.mutiny.sqlclient.Row, CommitTree> commitTreeMapper();
  Function<io.vertx.mutiny.sqlclient.Row, CommitTree> commitTreeWithBlobsMapper();
}