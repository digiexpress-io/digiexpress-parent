package io.resys.crm.client.spi;

import java.util.Optional;

import io.resys.crm.client.api.CrmClient;
import io.resys.crm.client.spi.actions.CreateCustomerActionImpl;
import io.resys.crm.client.spi.actions.CustomerQueryImpl;
import io.resys.crm.client.spi.actions.UpdateCustomerActionImpl;
import io.resys.thena.api.entities.Tenant;
import io.resys.thena.support.RepoAssert;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CrmClientImpl implements CrmClient {
  private final CrmStore ctx;
  
  public CrmStore getCtx() { return ctx; }
  
  @Override
  public CrmClient withRepoId(String repoId) {
    return new CrmClientImpl(ctx.withTenantId(repoId));
  }

  @Override
  public Uni<Tenant> getRepo() {
    return ctx.getTenant();
  }
  
  @Override
  public CreateCustomerAction createCustomer(){
    return new CreateCustomerActionImpl(ctx);
  }

  @Override
  public UpdateCustomerAction updateCustomer() {
    return new UpdateCustomerActionImpl(ctx);
  }

  @Override
  public CustomerQuery customerQuery() {
    return new CustomerQueryImpl(ctx);
  }
  
  @Override
  public RepositoryQuery repoQuery() {
    var repo = ctx.query();
    return new RepositoryQuery() {
      private String repoName;
      
      @Override public Uni<CrmClient> createIfNot() { return repo.createIfNot().onItem().transform(doc -> new CrmClientImpl(doc)); }
      @Override public Uni<CrmClient> create() { return repo.create().onItem().transform(doc -> new CrmClientImpl(doc)); }
      @Override public CrmClient build() { return new CrmClientImpl(repo.build()); }
      @Override public Uni<CrmClient> delete() { return repo.delete().onItem().transform(doc -> new CrmClientImpl(doc)); }
      @Override public Uni<CrmClient> deleteAll() { return repo.deleteAll().onItem().transform(doc -> new CrmClientImpl(ctx)); }
      @Override
      public RepositoryQuery repoName(String repoName) {
        this.repoName = repoName;
        repo.repoName(repoName);
        return this;
      }
      @Override
      public Uni<Optional<CrmClient>> get(String customerId) {
        RepoAssert.notEmpty(customerId, () -> "customerId must be defined!");
        RepoAssert.notEmpty(repoName, () -> "repoName must be defined!");
        
        final var client = ctx.getConfig().getClient();
        return client.tenants().find().id(repoName)
            .get().onItem().transform(existing -> {
              if(existing == null) {
                final Optional<CrmClient> result = Optional.empty();
                return result;
              }
              return Optional.of(new CrmClientImpl(repo.build()));
            });
        
      }
    };
  }
}
