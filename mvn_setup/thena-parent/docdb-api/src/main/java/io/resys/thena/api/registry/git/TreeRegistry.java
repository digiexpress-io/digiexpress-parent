package io.resys.thena.api.registry.git;

import java.util.function.Function;

import io.resys.thena.api.entities.git.Tree;
import io.resys.thena.api.registry.ThenaRegistryService;
import io.resys.thena.datasource.SqlQueryBuilder.Sql;
import io.resys.thena.datasource.SqlQueryBuilder.SqlTuple;


public interface TreeRegistry extends ThenaRegistryService<Tree, io.vertx.mutiny.sqlclient.Row> {
  SqlTuple getById(String id);
  Sql findAll();
  SqlTuple insertOne(Tree tree);
  
  Sql createTable();
  Sql createConstraints();
  Sql dropTable();
  
  Function<io.vertx.mutiny.sqlclient.Row, Tree> defaultMapper();
}