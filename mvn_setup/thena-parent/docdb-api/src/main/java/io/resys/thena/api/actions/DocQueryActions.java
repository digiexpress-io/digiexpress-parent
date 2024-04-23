package io.resys.thena.api.actions;

import java.util.List;

import io.resys.thena.api.envelope.DocContainer.DocObject;
import io.resys.thena.api.envelope.DocContainer.DocTenantObjects;
import io.resys.thena.api.envelope.QueryEnvelope;
import io.smallrye.mutiny.Uni;

public interface DocQueryActions {
  DocObjectsQuery docQuery();

  enum IncludeInQuery {
    ALL,
    
    COMMANDS,
    COMMITS,
    COMMIT_TREE
  } 
  
  interface DocObjectsQuery {
    DocObjectsQuery branchName(String branchName);
    DocObjectsQuery include(IncludeInQuery ... includeChildren);
    
    Uni<QueryEnvelope<DocObject>> get(String matchId);
    Uni<QueryEnvelope<DocTenantObjects>> findAll(List<String> matchId);
  }
}