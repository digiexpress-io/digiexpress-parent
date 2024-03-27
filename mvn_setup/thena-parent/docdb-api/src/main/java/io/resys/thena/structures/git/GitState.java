package io.resys.thena.structures.git;

import io.resys.thena.datasource.ThenaDataSource;
import io.smallrye.mutiny.Uni;

public interface GitState {
  ThenaDataSource getDataSource();
  GitInserts insert();
  GitQueries query();
  
  <R> Uni<R> withTransaction(TransactionFunction<R> callback);
  
  @FunctionalInterface
  interface TransactionFunction<R> {
    Uni<R> apply(GitState repoState);
  }
}
