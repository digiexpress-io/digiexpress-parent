package io.resys.thena.api.registry.org;

import java.util.Collection;
import java.util.function.Function;

import io.resys.thena.api.entities.org.OrgCommit;
import io.resys.thena.api.registry.ThenaRegistryService;
import io.resys.thena.datasource.SqlQueryBuilder.Sql;
import io.resys.thena.datasource.SqlQueryBuilder.SqlTuple;
import io.resys.thena.datasource.SqlQueryBuilder.SqlTupleList;


public interface OrgCommitRegistry extends ThenaRegistryService<OrgCommit, io.vertx.mutiny.sqlclient.Row> {
  SqlTuple getById(String id);
  Sql findAll();
  SqlTuple insertOne(OrgCommit commit);
  SqlTupleList insertAll(Collection<OrgCommit> commit);
  
  Sql createTable();
  Sql createConstraints();
  Sql dropTable();
  
  Function<io.vertx.mutiny.sqlclient.Row, OrgCommit> defaultMapper();
  
}