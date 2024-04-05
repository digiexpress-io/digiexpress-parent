package io.resys.thena.api.registry.grim;

import java.util.Collection;
import java.util.function.Function;

import io.resys.thena.api.entities.grim.GrimAnyObject;
import io.resys.thena.api.entities.grim.GrimCommitViewer;
import io.resys.thena.api.entities.grim.ThenaGrimObject.GrimDocType;
import io.resys.thena.api.registry.ThenaRegistryService;
import io.resys.thena.datasource.ThenaSqlClient;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;


public interface GrimCommitViewerRegistry extends ThenaRegistryService<GrimCommitViewer, io.vertx.mutiny.sqlclient.Row> {
  ThenaSqlClient.Sql findAll();
  ThenaSqlClient.SqlTuple getById(String id);
  
  ThenaSqlClient.SqlTuple findAllObjectsByIdAndType(Collection<AnyObjectCriteria> commits);
  ThenaSqlClient.SqlTuple findAllByUsedByAndCommit(String usedBy, String usedFor, Collection<String> commits);
  ThenaSqlClient.SqlTuple findAllByMissionIds(Collection<String> commitId);
  ThenaSqlClient.SqlTuple findAllByMissionIdsUsedByAndCommit(Collection<String> missionId, String usedBy, String usedFor);
  
  ThenaSqlClient.SqlTupleList insertAll(Collection<GrimCommitViewer> commits);
  ThenaSqlClient.SqlTupleList updateAll(Collection<GrimCommitViewer> commits);
  
  ThenaSqlClient.Sql createTable();
  ThenaSqlClient.Sql createConstraints();
  ThenaSqlClient.Sql dropTable();
  
  Function<io.vertx.mutiny.sqlclient.Row, GrimCommitViewer> defaultMapper();
  Function<io.vertx.mutiny.sqlclient.Row, GrimAnyObject> anyObjectMapper();  
  
  
  @Data @Builder @RequiredArgsConstructor
  public static class AnyObjectCriteria {
    private final String objectId;
    private final GrimDocType objectType;
  }
}