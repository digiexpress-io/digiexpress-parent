package io.resys.thena.api.registry.doc;

import java.util.Collection;
import java.util.function.Function;

import io.resys.thena.api.entities.doc.DocCommands;
import io.resys.thena.api.registry.ThenaRegistryService;
import io.resys.thena.datasource.ThenaSqlClient;


public interface DocCommandsRegistry extends ThenaRegistryService<DocCommands, io.vertx.mutiny.sqlclient.Row> {
  ThenaSqlClient.Sql findAll();
  ThenaSqlClient.SqlTuple findAllByDocIdsAndBranch(Collection<String> docIds, String branchId);

  ThenaSqlClient.SqlTupleList insertAll(Collection<DocCommands> commits);  
  ThenaSqlClient.SqlTuple getById(String id);
  
  ThenaSqlClient.Sql createTable();
  ThenaSqlClient.Sql createConstraints();
  ThenaSqlClient.Sql dropTable();
  
  Function<io.vertx.mutiny.sqlclient.Row, DocCommands> defaultMapper();
  

}