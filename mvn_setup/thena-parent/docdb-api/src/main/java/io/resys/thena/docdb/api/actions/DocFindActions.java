package io.resys.thena.docdb.api.actions;

import java.util.List;

import io.resys.thena.docdb.api.actions.PullActions.MatchCriteria;
import io.resys.thena.docdb.api.models.QueryEnvelope;
import io.resys.thena.docdb.api.models.ThenaDocObjects.DocObject;
import io.resys.thena.docdb.api.models.ThenaDocObjects.DocObjects;
import io.smallrye.mutiny.Uni;

public interface DocFindActions {
  DocQuery docQuery();
  
  
  interface DocQuery {
    
    DocQuery projectName(String repoName);
    DocQuery branchNameOrCommitOrTag(String branchNameOrCommitOrTag);
    DocQuery docId(List<String> blobName);
    DocQuery docId(String blobName);
    DocQuery matchBy(List<MatchCriteria> blobCriteria);
    DocQuery matchBy(MatchCriteria blobCriteria);
    
    DocQuery matchExternalId(boolean include); // include external id when matching
    DocQuery loadJson(boolean load); // load json yes/no 
    Uni<QueryEnvelope<DocObject>> get();
    Uni<QueryEnvelope<DocObjects>> findAll();
  }
}