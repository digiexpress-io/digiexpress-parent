package io.resys.thena.api.registry.doc;

import java.util.List;
import java.util.function.Function;

import io.resys.thena.api.entities.doc.Doc;
import io.resys.thena.api.registry.ThenaRegistryService;
import io.resys.thena.datasource.ThenaSqlClient;
import io.resys.thena.structures.doc.DocQueries.FlattedCriteria;


public interface DocMainRegistry extends ThenaRegistryService<Doc, io.vertx.mutiny.sqlclient.Row> {
  ThenaSqlClient.SqlTuple findAllFlatted(FlattedCriteria criteria);
  ThenaSqlClient.Sql findAllFlatted();
  ThenaSqlClient.SqlTuple findById(String id); // matches by external_id or id or parent_id
  ThenaSqlClient.SqlTuple getById(String id);  // matches by external_id or id
  ThenaSqlClient.SqlTuple deleteById(String id);
  ThenaSqlClient.Sql findAll();
  
  ThenaSqlClient.SqlTupleList insertMany(List<Doc> docs);
  ThenaSqlClient.SqlTupleList updateMany(List<Doc> docs);
  
  ThenaSqlClient.Sql createTable();
  ThenaSqlClient.Sql createConstraints();
  ThenaSqlClient.Sql dropTable();
  
  Function<io.vertx.mutiny.sqlclient.Row, Doc> defaultMapper();
  
}