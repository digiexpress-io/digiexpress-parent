package io.resys.thena.docdb.api.actions;

import java.util.List;

import io.resys.thena.docdb.api.models.QueryEnvelope;
import io.resys.thena.docdb.api.models.ThenaDocObjects.DocObject;
import io.resys.thena.docdb.api.models.ThenaDocObjects.DocObjects;
import io.smallrye.mutiny.Uni;

public interface DocQueryActions {
  DocObjectsQuery docQuery();

  
  interface DocObjectsQuery {
    DocObjectsQuery matchId(String matchId);          // can be external id/doc id
    DocObjectsQuery matchIds(List<String> matchId);   // can be external id/doc id
    DocObjectsQuery branchName(String branchName);
    DocObjectsQuery docType(String docType);
    DocObjectsQuery children(boolean includeChildren);
    
    Uni<QueryEnvelope<DocObject>> get();
    Uni<QueryEnvelope<DocObjects>> findAll();
  }
  
  /* Probably no need
   * 
   *
  DocBranchObjectsQuery docBranchQuery();
  interface DocBranchObjectsQuery {
    DocBranchObjectsQuery repoId(String repoId);
    DocBranchObjectsQuery matchId(String matchId);    // can be external id/branch id/doc id
    DocBranchObjectsQuery branchName(String branchName);
    DocBranchObjectsQuery matchBy(List<MatchCriteria> blobCriteria);
    DocBranchObjectsQuery matchBy(MatchCriteria blobCriteria);

    Uni<QueryEnvelope<DocBranchObject>> get();
    Uni<QueryEnvelope<DocObjects>> findAll();
  }*/
}