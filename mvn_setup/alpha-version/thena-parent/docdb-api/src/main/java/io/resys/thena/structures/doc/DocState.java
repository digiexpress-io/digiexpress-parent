package io.resys.thena.structures.doc;

import io.resys.thena.datasource.ThenaDataSource;
import io.smallrye.mutiny.Uni;

public interface DocState {
  String getTenantId();
  ThenaDataSource getDataSource();
  <R> Uni<R> withTransaction(TransactionFunction<R> callback);

  DocInserts insert();
  DocQueries query();

  @FunctionalInterface
  interface TransactionFunction<R> {
    Uni<R> apply(DocState repoState);
  }
}
