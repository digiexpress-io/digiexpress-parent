package io.resys.thena.api.registry.git;

import java.util.function.Function;

import io.resys.thena.api.entities.git.Tree;
import io.resys.thena.api.entities.git.TreeValue;
import io.resys.thena.api.registry.ThenaRegistryService;
import io.resys.thena.datasource.ThenaSqlClient;


public interface TreeValueRegistry extends ThenaRegistryService<TreeValue, io.vertx.mutiny.sqlclient.Row> {
  ThenaSqlClient.SqlTuple getByTreeId(String treeId);
  ThenaSqlClient.Sql findAll();
  ThenaSqlClient.SqlTuple insertOne(Tree tree, TreeValue item);
  ThenaSqlClient.SqlTupleList insertAll(Tree item);
  
  ThenaSqlClient.Sql createTable();
  ThenaSqlClient.Sql createConstraints();
  ThenaSqlClient.Sql dropTable();
  
  Function<io.vertx.mutiny.sqlclient.Row, TreeValue> defaultMapper();
}