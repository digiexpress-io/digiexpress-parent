package io.resys.thena.api.registry.doc;

import java.util.Collection;
import java.util.function.Function;

import io.resys.thena.api.entities.doc.DocLog;
import io.resys.thena.api.registry.ThenaRegistryService;
import io.resys.thena.datasource.ThenaSqlClient;


public interface DocLogRegistry extends ThenaRegistryService<DocLog, io.vertx.mutiny.sqlclient.Row> {
  ThenaSqlClient.SqlTuple getById(String id);
  ThenaSqlClient.SqlTuple findByBranchId(String branchId);
  ThenaSqlClient.Sql findAll();
  ThenaSqlClient.SqlTuple insertOne(DocLog doc);
  ThenaSqlClient.SqlTupleList insertAll(Collection<DocLog> logs);
  
  ThenaSqlClient.Sql createTable();
  ThenaSqlClient.Sql createConstraints();
  ThenaSqlClient.Sql dropTable();
  
  Function<io.vertx.mutiny.sqlclient.Row, DocLog> defaultMapper();
  
}