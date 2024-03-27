package io.resys.thena.spi;

import io.resys.thena.api.entities.Tenant;
import io.resys.thena.structures.doc.DocState;
import io.resys.thena.structures.git.GitState;
import io.resys.thena.structures.org.OrgState;
import io.resys.thena.support.ErrorHandler;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

public interface DbState {
  DbCollections getCollections();
  ErrorHandler getErrorHandler();
  
  RepoBuilder tenant();
  GitState toGitState();
  DocState toDocState();
  OrgState toOrgState();
  
  interface RepoBuilder {
    Uni<Tenant> getByName(String name);
    Uni<Tenant> getByNameOrId(String nameOrId);
    Multi<Tenant> findAll();
    Uni<Tenant> delete(Tenant newRepo);
    Uni<Tenant> insert(Tenant newRepo);
  }
}
