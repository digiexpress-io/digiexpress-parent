package io.resys.thena.api.registry.git;

import java.util.function.Function;

import io.resys.thena.api.entities.git.Tree;
import io.resys.thena.api.registry.ThenaRegistryService;
import io.resys.thena.datasource.ThenaSqlClient;


public interface TreeRegistry extends ThenaRegistryService<Tree, io.vertx.mutiny.sqlclient.Row> {
  ThenaSqlClient.SqlTuple getById(String id);
  ThenaSqlClient.Sql findAll();
  ThenaSqlClient.SqlTuple insertOne(Tree tree);
  
  ThenaSqlClient.Sql createTable();
  ThenaSqlClient.Sql createConstraints();
  ThenaSqlClient.Sql dropTable();
  
  Function<io.vertx.mutiny.sqlclient.Row, Tree> defaultMapper();
}