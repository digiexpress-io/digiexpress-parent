package io.resys.thena.structures.git;

import io.resys.thena.api.models.Repo;
import io.smallrye.mutiny.Uni;

public interface GitState {
  Uni<GitInserts> insert(String repoNameOrId);
  GitInserts insert(Repo repo);
  
  Uni<GitQueries> query(String repoNameOrId);
  GitQueries query(Repo repo);
  
  GitRepo withRepo(Repo repo);
  Uni<GitRepo> withRepo(String repoNameOrId);
  <R> Uni<R> withTransaction(String repoId, String headName, TransactionFunction<R> callback);
  

  interface GitRepo {
    String getRepoName();
    Repo getRepo();
    GitInserts insert();
    GitQueries query();
  }
  
  @FunctionalInterface
  interface TransactionFunction<R> {
    Uni<R> apply(GitRepo repoState);
  }
}
