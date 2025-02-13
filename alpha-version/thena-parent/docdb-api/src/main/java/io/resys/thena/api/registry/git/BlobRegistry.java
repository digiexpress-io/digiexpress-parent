package io.resys.thena.api.registry.git;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import javax.annotation.Nullable;

import io.resys.thena.api.actions.GitPullActions.MatchCriteria;
import io.resys.thena.api.entities.git.Blob;
import io.resys.thena.api.entities.git.BlobHistory;
import io.resys.thena.api.registry.ThenaRegistryService;
import io.resys.thena.datasource.ThenaSqlClient;


public interface BlobRegistry extends ThenaRegistryService<Blob, io.vertx.mutiny.sqlclient.Row> {
  ThenaSqlClient.Sql findAll();
  ThenaSqlClient.SqlTuple getById(String blobId);
  
  ThenaSqlClient.SqlTuple insertOne(Blob blob);
  ThenaSqlClient.SqlTupleList insertAll(Collection<Blob> blobs);
  
  ThenaSqlClient.SqlTuple find(@Nullable String name, boolean latestOnly, List<MatchCriteria> criteria);
  ThenaSqlClient.SqlTuple findByTree(String treeId, List<MatchCriteria> criteria);
  ThenaSqlClient.SqlTuple findByTree(String treeId, List<String> blobNames, List<MatchCriteria> criteria);
  ThenaSqlClient.SqlTuple findByIds(Collection<String> blobId);
  
  ThenaSqlClient.Sql createTable();
  ThenaSqlClient.Sql createConstraints();
  ThenaSqlClient.Sql dropTable();
  
  Function<io.vertx.mutiny.sqlclient.Row, Blob> defaultMapper();
  
  Function<io.vertx.mutiny.sqlclient.Row, BlobHistory> historyMapper();
}