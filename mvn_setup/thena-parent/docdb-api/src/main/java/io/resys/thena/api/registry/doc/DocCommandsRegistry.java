package io.resys.thena.api.registry.doc;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import org.immutables.value.Value;

import io.resys.thena.api.entities.doc.DocCommands;
import io.resys.thena.api.registry.ThenaRegistryService;
import io.resys.thena.datasource.ThenaSqlClient;


public interface DocCommandsRegistry extends ThenaRegistryService<DocCommands, io.vertx.mutiny.sqlclient.Row> {
  ThenaSqlClient.Sql findAll();
  ThenaSqlClient.SqlTuple findAllByIds(Collection<String> id);
  ThenaSqlClient.SqlTuple findAllByMissionIds(List<DocCommandFilter> filter);
  ThenaSqlClient.SqlTupleList insertAll(Collection<DocCommands> commits);  
  ThenaSqlClient.SqlTuple getById(String id);
  
  ThenaSqlClient.Sql createTable();
  ThenaSqlClient.Sql createConstraints();
  ThenaSqlClient.Sql dropTable();
  
  Function<io.vertx.mutiny.sqlclient.Row, DocCommands> defaultMapper();
  
  
  @Value.Immutable
  interface DocCommandFilter {
    String getDocId();
    String getBranchId();
  }
}