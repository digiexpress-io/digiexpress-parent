package io.resys.thena.api.registry.doc;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import io.resys.thena.api.entities.doc.DocBranch;
import io.resys.thena.api.entities.doc.DocBranchLock;
import io.resys.thena.api.registry.ThenaRegistryService;
import io.resys.thena.datasource.SqlQueryBuilder.Sql;
import io.resys.thena.datasource.SqlQueryBuilder.SqlTuple;
import io.resys.thena.datasource.SqlQueryBuilder.SqlTupleList;
import io.resys.thena.structures.doc.DocQueries.DocBranchLockCriteria;
import io.resys.thena.structures.doc.DocQueries.DocLockCriteria;


public interface DocBranchRegistry extends ThenaRegistryService<DocBranch, io.vertx.mutiny.sqlclient.Row> {
  SqlTuple getById(String branchId);
  SqlTuple updateOne(DocBranch doc);
  SqlTuple insertOne(DocBranch doc);
  SqlTupleList insertAll(Collection<DocBranch> docs);
  SqlTupleList updateAll(List<DocBranch> doc);
  SqlTuple getBranchLock(DocBranchLockCriteria crit);
  SqlTuple getBranchLocks(List<DocBranchLockCriteria> crit);
  SqlTuple getDocLock(DocLockCriteria crit);
  SqlTuple getDocLocks(List<DocLockCriteria> crit);
  Sql findAll();
  
  
  Sql createTable();
  Sql createConstraints();
  Sql dropTable();
  
  Function<io.vertx.mutiny.sqlclient.Row, DocBranch> defaultMapper();
  Function<io.vertx.mutiny.sqlclient.Row, DocBranchLock> docBranchLockMapper();
  
}