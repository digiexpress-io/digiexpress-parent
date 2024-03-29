package io.resys.thena.api.registry.git;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import javax.annotation.Nullable;

import io.resys.thena.api.actions.GitPullActions.MatchCriteria;
import io.resys.thena.api.entities.git.Blob;
import io.resys.thena.api.entities.git.BlobHistory;
import io.resys.thena.api.registry.ThenaRegistryService;
import io.resys.thena.datasource.SqlQueryBuilder.Sql;
import io.resys.thena.datasource.SqlQueryBuilder.SqlTuple;
import io.resys.thena.datasource.SqlQueryBuilder.SqlTupleList;


public interface BlobRegistry extends ThenaRegistryService<Blob, io.vertx.mutiny.sqlclient.Row> {
  Sql findAll();
  SqlTuple getById(String blobId);
  
  SqlTuple insertOne(Blob blob);
  SqlTupleList insertAll(Collection<Blob> blobs);
  
  SqlTuple find(@Nullable String name, boolean latestOnly, List<MatchCriteria> criteria);
  SqlTuple findByTree(String treeId, List<MatchCriteria> criteria);
  SqlTuple findByTree(String treeId, List<String> blobNames, List<MatchCriteria> criteria);
  SqlTuple findByIds(Collection<String> blobId);
  
  Sql createTable();
  Sql createConstraints();
  Sql dropTable();
  
  Function<io.vertx.mutiny.sqlclient.Row, Blob> defaultMapper();
  
  Function<io.vertx.mutiny.sqlclient.Row, BlobHistory> historyMapper();
}