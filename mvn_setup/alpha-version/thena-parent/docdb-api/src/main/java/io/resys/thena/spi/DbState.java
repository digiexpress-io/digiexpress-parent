package io.resys.thena.spi;

import org.immutables.value.Value;

import io.resys.thena.api.entities.Tenant;
import io.resys.thena.datasource.ThenaDataSource;
import io.resys.thena.structures.doc.DocState;
import io.resys.thena.structures.git.GitState;
import io.resys.thena.structures.grim.GrimState;
import io.resys.thena.structures.org.OrgState;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

public interface DbState {
  ThenaDataSource getDataSource();
  InternalTenantQuery tenant();
  
  Uni<GrimState> toGrimState(String tenantId);
  GrimState toGrimState(Tenant repo);
  <R> Uni<R> withGrimTransaction(TxScope tenantId, GrimState.TransactionFunction<R> callback);
  
  
  Uni<GitState> toGitState(String tenantId);
  GitState toGitState(Tenant repo);
  <R> Uni<R> withGitTransaction(TxScope tenantId, GitState.TransactionFunction<R> callback);
  
  Uni<DocState> toDocState(String tenantId);
  DocState toDocState(Tenant repo);
  <R> Uni<R> withDocTransaction(TxScope tenantId, DocState.TransactionFunction<R> callback);
  
  Uni<OrgState> toOrgState(String tenantId);
  OrgState toOrgState(Tenant repo);
  <R> Uni<R> withOrgTransaction(TxScope tenantId, OrgState.TransactionFunction<R> callback);
  
  interface InternalTenantQuery {
    Uni<Tenant> getByName(String name);
    Uni<Tenant> getByNameOrId(String nameOrId);
    Multi<Tenant> findAll();
    Uni<Void> delete();
    Uni<Tenant> delete(Tenant newRepo);
    Uni<Tenant> insert(Tenant newRepo);
  }
  
  @Value.Immutable
  interface TxScope {
    String getTenantId();
    String getCommitAuthor();
    String getCommitMessage();
  }
}
