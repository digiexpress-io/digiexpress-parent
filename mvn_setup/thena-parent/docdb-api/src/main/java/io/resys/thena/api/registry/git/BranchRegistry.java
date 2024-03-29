package io.resys.thena.api.registry.git;

import java.util.function.Function;

import io.resys.thena.api.entities.git.Branch;
import io.resys.thena.api.entities.git.Commit;
import io.resys.thena.api.registry.ThenaRegistryService;
import io.resys.thena.datasource.SqlQueryBuilder.Sql;
import io.resys.thena.datasource.SqlQueryBuilder.SqlTuple;


public interface BranchRegistry extends ThenaRegistryService<Branch, io.vertx.mutiny.sqlclient.Row> {
  SqlTuple getByName(String name);
  SqlTuple getByNameOrCommit(String refNameOrCommit);
  Sql getFirst();
  Sql findAll();
  SqlTuple insertOne(Branch ref);
  SqlTuple updateOne(Branch ref, Commit commit);
  
  Sql createTable();
  Sql createConstraints();
  Sql dropTable();
  
  Function<io.vertx.mutiny.sqlclient.Row, Branch> defaultMapper();
}