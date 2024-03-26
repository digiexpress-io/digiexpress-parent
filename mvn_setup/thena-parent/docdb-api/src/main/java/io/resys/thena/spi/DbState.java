package io.resys.thena.spi;

import io.resys.thena.api.models.Repo;
import io.resys.thena.models.doc.DocState;
import io.resys.thena.models.git.GitState;
import io.resys.thena.models.org.OrgState;
import io.resys.thena.support.ErrorHandler;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

public interface DbState {
  DbCollections getCollections();
  ErrorHandler getErrorHandler();
  
  RepoBuilder project();
  GitState toGitState();
  DocState toDocState();
  OrgState toOrgState();
  
  interface RepoBuilder {
    Uni<Repo> getByName(String name);
    Uni<Repo> getByNameOrId(String nameOrId);
    Multi<Repo> findAll();
    Uni<Repo> delete(Repo newRepo);
    Uni<Repo> insert(Repo newRepo);
  }
}
