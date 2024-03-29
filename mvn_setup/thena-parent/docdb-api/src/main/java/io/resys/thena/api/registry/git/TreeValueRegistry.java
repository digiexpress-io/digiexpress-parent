package io.resys.thena.api.registry.git;

import java.util.function.Function;

import io.resys.thena.api.entities.git.Tree;
import io.resys.thena.api.entities.git.TreeValue;
import io.resys.thena.api.registry.ThenaRegistryService;
import io.resys.thena.datasource.SqlQueryBuilder.Sql;
import io.resys.thena.datasource.SqlQueryBuilder.SqlTuple;
import io.resys.thena.datasource.SqlQueryBuilder.SqlTupleList;


public interface TreeValueRegistry extends ThenaRegistryService<TreeValue, io.vertx.mutiny.sqlclient.Row> {
  SqlTuple getByTreeId(String treeId);
  Sql findAll();
  SqlTuple insertOne(Tree tree, TreeValue item);
  SqlTupleList insertAll(Tree item);
  
  Sql createTable();
  Sql createConstraints();
  Sql dropTable();
  
  Function<io.vertx.mutiny.sqlclient.Row, TreeValue> defaultMapper();
}