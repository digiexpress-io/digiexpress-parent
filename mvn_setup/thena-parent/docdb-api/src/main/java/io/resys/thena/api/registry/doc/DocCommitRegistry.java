package io.resys.thena.api.registry.doc;

import java.util.Collection;
import java.util.function.Function;

import io.resys.thena.api.entities.doc.DocCommit;
import io.resys.thena.api.registry.ThenaRegistryService;
import io.resys.thena.datasource.SqlQueryBuilder.Sql;
import io.resys.thena.datasource.SqlQueryBuilder.SqlTuple;
import io.resys.thena.datasource.SqlQueryBuilder.SqlTupleList;


public interface DocCommitRegistry extends ThenaRegistryService<DocCommit, io.vertx.mutiny.sqlclient.Row> {
  
  SqlTuple getById(String id);
  Sql findAll();
  SqlTuple insertOne(DocCommit commit);
  SqlTupleList insertAll(Collection<DocCommit> commits);
  
  Sql createTable();
  Sql createConstraints();
  Sql dropTable();
  
  Function<io.vertx.mutiny.sqlclient.Row, DocCommit> defaultMapper();
  
}