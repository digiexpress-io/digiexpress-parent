package io.resys.thena.api.actions;

import java.util.List;

import io.resys.thena.api.envelope.DocContainer.DocObject;
import io.resys.thena.api.envelope.DocContainer.DocTenantObjects;
import io.resys.thena.api.envelope.QueryEnvelope;
import io.smallrye.mutiny.Uni;

public interface DocQueryActions {
  DocObjectsQuery docQuery();

  enum IncludeInQuery {
    CHILD_DOCS,
    COMMIT_TREE,
    COMMANDS,
    BRANCHES
  } 
  
  interface DocObjectsQuery {
    DocObjectsQuery matchId(String matchId);          // can be external id/doc id
    DocObjectsQuery matchIds(List<String> matchId);   // can be external id/doc id
    DocObjectsQuery branchName(String branchName);
    DocObjectsQuery docType(String docType);
    DocObjectsQuery include(IncludeInQuery ... includeChildren);
    
    Uni<QueryEnvelope<DocObject>> get();
    Uni<QueryEnvelope<DocTenantObjects>> findAll();
  }
  
}