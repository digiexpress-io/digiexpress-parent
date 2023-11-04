package io.resys.thena.docdb.api.actions;

import io.resys.thena.docdb.api.models.QueryEnvelope;
import io.resys.thena.docdb.api.models.ThenaGitObjects.PullObjects;
import io.smallrye.mutiny.Uni;

public interface DocFindActions {
  DocQuery docQuery();
  
  
  interface DocQuery {
    
    Uni<QueryEnvelope<PullObjects>> findAll();
  }
}