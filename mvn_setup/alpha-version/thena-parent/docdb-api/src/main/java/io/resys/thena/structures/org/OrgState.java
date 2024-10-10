package io.resys.thena.structures.org;

import io.resys.thena.datasource.ThenaDataSource;
import io.smallrye.mutiny.Uni;

public interface OrgState {
  String getTenantId();
  ThenaDataSource getDataSource();
  <R> Uni<R> withTransaction(TransactionFunction<R> callback);
  OrgInserts insert();
  OrgQueries query();
  
  @FunctionalInterface
  interface TransactionFunction<R> {
    Uni<R> apply(OrgState repoState);
  }
}
