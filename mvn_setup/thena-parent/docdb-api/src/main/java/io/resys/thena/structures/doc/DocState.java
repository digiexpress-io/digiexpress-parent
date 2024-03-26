package io.resys.thena.structures.doc;

import io.resys.thena.api.entities.Tenant;
import io.smallrye.mutiny.Uni;

public interface DocState {

  <R> Uni<R> withTransaction(String repoId, TransactionFunction<R> callback);

  Uni<DocQueries> query(String repoNameOrId);
  Uni<DocInserts> insert(String repoNameOrId);
  Uni<DocRepo> withRepo(String repoNameOrId);

  DocInserts insert(Tenant repo);
  DocQueries query(Tenant repo);
  DocRepo withRepo(Tenant repo);

  interface DocRepo {
    String getRepoName();
    Tenant getRepo();
    
    DocInserts insert();
    DocQueries query();
  }
  
  @FunctionalInterface
  interface TransactionFunction<R> {
    Uni<R> apply(DocRepo repoState);
  }
}
