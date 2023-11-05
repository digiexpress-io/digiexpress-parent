package io.resys.thena.docdb.spi;

import io.resys.thena.docdb.api.models.Repo;
import io.smallrye.mutiny.Uni;

public interface DocDbState {

  <R> Uni<R> withTransaction(String repoId, TransactionFunction<R> callback);

  Uni<DocDbQueries> query(String repoNameOrId);
  Uni<DocDbInserts> insert(String repoNameOrId);
  Uni<DocRepo> withRepo(String repoNameOrId);

  DocDbInserts insert(Repo repo);
  DocDbQueries query(Repo repo);
  DocRepo withRepo(Repo repo);

  interface DocRepo {
    String getRepoName();
    Repo getRepo();
    
    DocDbInserts insert();
    DocDbQueries query();
  }
  
  @FunctionalInterface
  interface TransactionFunction<R> {
    Uni<R> apply(DocRepo repoState);
  }
}
