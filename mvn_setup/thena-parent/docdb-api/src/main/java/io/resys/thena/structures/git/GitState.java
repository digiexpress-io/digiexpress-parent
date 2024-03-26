package io.resys.thena.structures.git;

import io.resys.thena.api.entities.Tenant;
import io.smallrye.mutiny.Uni;

public interface GitState {
  Uni<GitInserts> insert(String repoNameOrId);
  GitInserts insert(Tenant repo);
  
  Uni<GitQueries> query(String repoNameOrId);
  GitQueries query(Tenant repo);
  
  GitRepo withRepo(Tenant repo);
  Uni<GitRepo> withRepo(String repoNameOrId);
  <R> Uni<R> withTransaction(String repoId, String headName, TransactionFunction<R> callback);
  

  interface GitRepo {
    String getRepoName();
    Tenant getRepo();
    GitInserts insert();
    GitQueries query();
  }
  
  @FunctionalInterface
  interface TransactionFunction<R> {
    Uni<R> apply(GitRepo repoState);
  }
}
