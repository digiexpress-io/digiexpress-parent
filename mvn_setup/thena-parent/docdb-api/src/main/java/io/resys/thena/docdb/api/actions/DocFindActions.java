package io.resys.thena.docdb.api.actions;

import java.util.List;

import io.resys.thena.docdb.api.actions.PullActions.MatchCriteria;
import io.resys.thena.docdb.api.actions.PullActions.PullObjectsQuery;
import io.resys.thena.docdb.api.models.QueryEnvelope;
import io.resys.thena.docdb.api.models.ThenaDocObjects.DocObject;
import io.resys.thena.docdb.api.models.ThenaGitObjects.PullObjects;
import io.smallrye.mutiny.Uni;

public interface DocFindActions {
  DocQuery docQuery();
  
  
  interface DocQuery {
    
    PullObjectsQuery projectName(String repoName);
    PullObjectsQuery branchNameOrCommitOrTag(String branchNameOrCommitOrTag);
    PullObjectsQuery docId(List<String> blobName);
    PullObjectsQuery docId(String blobName);
    PullObjectsQuery includeExternalId(String blobName);
    PullObjectsQuery matchBy(List<MatchCriteria> blobCriteria);
    PullObjectsQuery matchBy(MatchCriteria blobCriteria);
    
    Uni<QueryEnvelope<DocObject>> get();
    Uni<QueryEnvelope<PullObjects>> findAll();
  }
}