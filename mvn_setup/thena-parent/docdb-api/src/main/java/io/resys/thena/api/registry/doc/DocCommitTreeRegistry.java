package io.resys.thena.api.registry.doc;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import io.resys.thena.api.entities.grim.GrimCommitTree;
import io.resys.thena.api.registry.ThenaRegistryService;
import io.resys.thena.datasource.ThenaSqlClient;


public interface DocCommitTreeRegistry extends ThenaRegistryService<GrimCommitTree, io.vertx.mutiny.sqlclient.Row> {
  ThenaSqlClient.Sql findAll();
  ThenaSqlClient.SqlTuple getById(String id);
  ThenaSqlClient.SqlTuple findAllByCommitIds(List<String> commitId);
  ThenaSqlClient.SqlTupleList insertAll(Collection<GrimCommitTree> commits);
  
  ThenaSqlClient.Sql createTable();
  ThenaSqlClient.Sql createConstraints();
  ThenaSqlClient.Sql dropTable();
  
  Function<io.vertx.mutiny.sqlclient.Row, GrimCommitTree> defaultMapper();
}