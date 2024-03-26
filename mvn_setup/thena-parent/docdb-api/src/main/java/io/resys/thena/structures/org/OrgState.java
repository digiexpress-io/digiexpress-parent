package io.resys.thena.structures.org;

import io.resys.thena.api.models.Repo;
import io.smallrye.mutiny.Uni;

public interface OrgState {

  <R> Uni<R> withTransaction(String repoId, TransactionFunction<R> callback);

  Uni<OrgQueries> query(String repoNameOrId);
  Uni<OrgInserts> insert(String repoNameOrId);
  Uni<OrgRepo> withRepo(String repoNameOrId);

  OrgInserts insert(Repo repo);
  OrgQueries query(Repo repo);
  OrgRepo withRepo(Repo repo);

  interface OrgRepo {
    String getRepoName();
    Repo getRepo();
    
    OrgInserts insert();
    OrgQueries query();
  }
  
  @FunctionalInterface
  interface TransactionFunction<R> {
    Uni<R> apply(OrgRepo repoState);
  }
}
