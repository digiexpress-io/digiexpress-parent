package io.resys.thena.api.registry.doc;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import io.resys.thena.api.entities.doc.DocBranch;
import io.resys.thena.api.entities.doc.DocLock.DocBranchLock;
import io.resys.thena.api.registry.ThenaRegistryService;
import io.resys.thena.datasource.ThenaSqlClient;
import io.resys.thena.structures.doc.DocQueries.DocBranchLockCriteria;
import io.resys.thena.structures.doc.DocQueries.DocLockCriteria;


public interface DocBranchRegistry extends ThenaRegistryService<DocBranch, io.vertx.mutiny.sqlclient.Row> {
  ThenaSqlClient.SqlTuple getById(String branchId);

  ThenaSqlClient.SqlTupleList insertAll(Collection<DocBranch> docs);
  ThenaSqlClient.SqlTupleList updateAll(List<DocBranch> doc);
  ThenaSqlClient.SqlTuple getBranchLock(DocBranchLockCriteria crit);
  ThenaSqlClient.SqlTuple getBranchLocks(List<DocBranchLockCriteria> crit);
  ThenaSqlClient.SqlTuple getDocLock(DocLockCriteria crit);
  ThenaSqlClient.SqlTuple getDocLocks(List<DocLockCriteria> crit);
  ThenaSqlClient.Sql findAll();
  
  
  ThenaSqlClient.Sql createTable();
  ThenaSqlClient.Sql createConstraints();
  ThenaSqlClient.Sql dropTable();
  
  Function<io.vertx.mutiny.sqlclient.Row, DocBranch> defaultMapper();
  Function<io.vertx.mutiny.sqlclient.Row, DocBranchLock> docBranchLockMapper();
  
}