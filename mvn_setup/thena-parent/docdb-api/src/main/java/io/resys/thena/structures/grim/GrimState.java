package io.resys.thena.structures.grim;

import io.resys.thena.datasource.ThenaDataSource;
import io.smallrye.mutiny.Uni;

public interface GrimState {
  String getTenantId();
  ThenaDataSource getDataSource();
  <R> Uni<R> withTransaction(TransactionFunction<R> callback);
  GrimInserts insert();
  GrimQueries query();
  
  @FunctionalInterface
  interface TransactionFunction<R> {
    Uni<R> apply(GrimState repoState);
  }
}
