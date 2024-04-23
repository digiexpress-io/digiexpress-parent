package io.resys.thena.api.registry.doc;

import java.util.Collection;
import java.util.function.Function;

import io.resys.thena.api.entities.doc.DocCommit;
import io.resys.thena.api.registry.ThenaRegistryService;
import io.resys.thena.datasource.ThenaSqlClient;


public interface DocCommitRegistry extends ThenaRegistryService<DocCommit, io.vertx.mutiny.sqlclient.Row> {
  
  ThenaSqlClient.Sql findAll();
  ThenaSqlClient.SqlTupleList insertAll(Collection<DocCommit> commits);
  ThenaSqlClient.SqlTuple findAllByDocIdsAndBranch(Collection<String> id, String branchId);
  
  ThenaSqlClient.Sql createTable();
  ThenaSqlClient.Sql createConstraints();
  ThenaSqlClient.Sql dropTable();
  
  Function<io.vertx.mutiny.sqlclient.Row, DocCommit> defaultMapper();
  
}