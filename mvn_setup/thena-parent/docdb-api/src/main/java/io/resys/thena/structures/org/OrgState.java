package io.resys.thena.structures.org;

import io.resys.thena.api.entities.Tenant;
import io.smallrye.mutiny.Uni;

public interface OrgState {

  <R> Uni<R> withTransaction(String repoId, TransactionFunction<R> callback);

  Uni<OrgQueries> query(String repoNameOrId);
  Uni<OrgInserts> insert(String repoNameOrId);
  Uni<OrgRepo> withRepo(String repoNameOrId);

  OrgInserts insert(Tenant repo);
  OrgQueries query(Tenant repo);
  OrgRepo withRepo(Tenant repo);

  interface OrgRepo {
    String getRepoName();
    Tenant getRepo();
    
    OrgInserts insert();
    OrgQueries query();
  }
  
  @FunctionalInterface
  interface TransactionFunction<R> {
    Uni<R> apply(OrgRepo repoState);
  }
}
