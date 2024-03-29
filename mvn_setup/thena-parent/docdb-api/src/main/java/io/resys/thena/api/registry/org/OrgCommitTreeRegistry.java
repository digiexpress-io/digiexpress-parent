package io.resys.thena.api.registry.org;

import java.util.Collection;
import java.util.function.Function;

import io.resys.thena.api.entities.org.OrgCommitTree;
import io.resys.thena.api.registry.ThenaRegistryService;
import io.resys.thena.datasource.SqlQueryBuilder.Sql;
import io.resys.thena.datasource.SqlQueryBuilder.SqlTuple;
import io.resys.thena.datasource.SqlQueryBuilder.SqlTupleList;


public interface OrgCommitTreeRegistry extends ThenaRegistryService<OrgCommitTree, io.vertx.mutiny.sqlclient.Row> {
  SqlTuple getById(String id);
  SqlTuple findByCommmitId(String commitId);
  Sql findAll();
  SqlTupleList insertAll(Collection<OrgCommitTree> tree);
  
  Sql createTable();
  Sql createConstraints();
  Sql dropTable();
  
  Function<io.vertx.mutiny.sqlclient.Row, OrgCommitTree> defaultMapper();
  
}