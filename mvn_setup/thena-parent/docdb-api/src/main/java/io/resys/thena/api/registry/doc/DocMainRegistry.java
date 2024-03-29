package io.resys.thena.api.registry.doc;

import java.util.List;
import java.util.function.Function;

import io.resys.thena.api.entities.doc.Doc;
import io.resys.thena.api.entities.doc.DocFlatted;
import io.resys.thena.api.registry.ThenaRegistryService;
import io.resys.thena.datasource.SqlQueryBuilder.Sql;
import io.resys.thena.datasource.SqlQueryBuilder.SqlTuple;
import io.resys.thena.datasource.SqlQueryBuilder.SqlTupleList;
import io.resys.thena.structures.doc.DocQueries.FlattedCriteria;


public interface DocMainRegistry extends ThenaRegistryService<Doc, io.vertx.mutiny.sqlclient.Row> {
  SqlTuple findAllFlatted(FlattedCriteria criteria);
  Sql findAllFlatted();
  SqlTuple findById(String id); // matches by external_id or id or parent_id
  SqlTuple getById(String id);  // matches by external_id or id
  SqlTuple deleteById(String id);
  Sql findAll();
  SqlTuple insertOne(Doc doc);
  SqlTuple updateOne(Doc doc);
  
  SqlTupleList insertMany(List<Doc> docs);
  SqlTupleList updateMany(List<Doc> docs);
  
  Sql createTable();
  Sql createConstraints();
  Sql dropTable();
  
  Function<io.vertx.mutiny.sqlclient.Row, Doc> defaultMapper();
  Function<io.vertx.mutiny.sqlclient.Row, DocFlatted> docFlattedMapper();
  
}