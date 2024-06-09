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
  enum Branches {
    main
  }
  
  interface DocObjectsQuery {
    DocObjectsQuery branchName(String branchName);
    DocObjectsQuery docType(String ...docType);
    DocObjectsQuery parentId(String parentId);
    DocObjectsQuery ownerId(String ownerId);
    DocObjectsQuery include(IncludeInQuery ... includeChildren);

    Uni<QueryEnvelope<DocObject>> get();
    Uni<QueryEnvelope<DocObject>> findOne();
    Uni<QueryEnvelope<DocObject>> get(String matchId);
    Uni<QueryEnvelope<DocTenantObjects>> findAll(List<String> matchId);
    Uni<QueryEnvelope<DocTenantObjects>> findAll();
    
    default DocObjectsQuery branchMain() {
      return branchName(Branches.main.name());
    }
    

  }
}