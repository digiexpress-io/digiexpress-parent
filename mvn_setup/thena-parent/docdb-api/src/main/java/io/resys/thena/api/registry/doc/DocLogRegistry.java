package io.resys.thena.api.registry.doc;

import java.util.Collection;
import java.util.function.Function;

import io.resys.thena.api.entities.doc.DocLog;
import io.resys.thena.api.registry.ThenaRegistryService;
import io.resys.thena.datasource.SqlQueryBuilder.Sql;
import io.resys.thena.datasource.SqlQueryBuilder.SqlTuple;
import io.resys.thena.datasource.SqlQueryBuilder.SqlTupleList;


public interface DocLogRegistry extends ThenaRegistryService<DocLog, io.vertx.mutiny.sqlclient.Row> {
  SqlTuple getById(String id);
  SqlTuple findByBranchId(String branchId);
  Sql findAll();
  SqlTuple insertOne(DocLog doc);
  SqlTupleList insertAll(Collection<DocLog> logs);
  
  Sql createTable();
  Sql createConstraints();
  Sql dropTable();
  
  Function<io.vertx.mutiny.sqlclient.Row, DocLog> defaultMapper();
  
}