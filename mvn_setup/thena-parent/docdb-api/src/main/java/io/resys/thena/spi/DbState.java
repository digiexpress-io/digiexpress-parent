package io.resys.thena.spi;

import io.resys.thena.api.entities.Tenant;
import io.resys.thena.datasource.ThenaDataSource;
import io.resys.thena.structures.doc.DocState;
import io.resys.thena.structures.git.GitState;
import io.resys.thena.structures.org.OrgState;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

public interface DbState {
  ThenaDataSource getDataSource();
  InternalTenantQuery tenant();
  
  Uni<GitState> toGitState(String tenantId);
  GitState toGitState(Tenant repo);
  <R> Uni<R> withGitTransaction(String tenantId, GitState.TransactionFunction<R> callback);
  
  Uni<DocState> toDocState(String tenantId);
  DocState toDocState(Tenant repo);
  <R> Uni<R> withDocTransaction(String tenantId, DocState.TransactionFunction<R> callback);
  
  Uni<OrgState> toOrgState(String tenantId);
  OrgState toOrgState(Tenant repo);
  <R> Uni<R> withOrgTransaction(String tenantId, OrgState.TransactionFunction<R> callback);
  
  interface InternalTenantQuery {
    Uni<Tenant> getByName(String name);
    Uni<Tenant> getByNameOrId(String nameOrId);
    Multi<Tenant> findAll();
    Uni<Tenant> delete(Tenant newRepo);
    Uni<Tenant> insert(Tenant newRepo);
  }
}
