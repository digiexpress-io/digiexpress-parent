package io.resys.thena.docdb.models.org;

import io.resys.thena.docdb.api.models.Repo;
import io.resys.thena.docdb.models.doc.DocState.DocRepo;
import io.smallrye.mutiny.Uni;

public interface OrgState {

  <R> Uni<R> withTransaction(String repoId, TransactionFunction<R> callback);

  Uni<OrgQueries> query(String repoNameOrId);
  Uni<OrgInserts> insert(String repoNameOrId);
  Uni<DocRepo> withRepo(String repoNameOrId);

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