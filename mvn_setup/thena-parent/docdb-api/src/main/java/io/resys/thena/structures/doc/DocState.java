package io.resys.thena.structures.doc;

import io.resys.thena.api.models.Repo;
import io.smallrye.mutiny.Uni;

public interface DocState {

  <R> Uni<R> withTransaction(String repoId, TransactionFunction<R> callback);

  Uni<DocQueries> query(String repoNameOrId);
  Uni<DocInserts> insert(String repoNameOrId);
  Uni<DocRepo> withRepo(String repoNameOrId);

  DocInserts insert(Repo repo);
  DocQueries query(Repo repo);
  DocRepo withRepo(Repo repo);

  interface DocRepo {
    String getRepoName();
    Repo getRepo();
    
    DocInserts insert();
    DocQueries query();
  }
  
  @FunctionalInterface
  interface TransactionFunction<R> {
    Uni<R> apply(DocRepo repoState);
  }
}
