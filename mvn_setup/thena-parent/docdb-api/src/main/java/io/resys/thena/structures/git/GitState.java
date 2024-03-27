package io.resys.thena.structures.git;

import io.resys.thena.api.entities.Tenant;
import io.smallrye.mutiny.Uni;

public interface GitState {
  GitTenant withTenant(Tenant repo);
  Uni<GitTenant> withTenant(String repoNameOrId);
  <R> Uni<R> withTransaction(String repoId, String headName, TransactionFunction<R> callback);
  

  interface GitTenant {
    String getTenantName();
    Tenant getRepo();
    GitInserts insert();
    GitQueries query();
  }
  
  @FunctionalInterface
  interface TransactionFunction<R> {
    Uni<R> apply(GitTenant repoState);
  }
}
