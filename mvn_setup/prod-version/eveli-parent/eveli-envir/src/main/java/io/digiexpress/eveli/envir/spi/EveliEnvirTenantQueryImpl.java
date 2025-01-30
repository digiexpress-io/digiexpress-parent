package io.digiexpress.eveli.envir.spi;

import java.util.Optional;

import io.digiexpress.eveli.envir.api.EveliEnvirClient;
import io.digiexpress.eveli.envir.api.EveliEnvirTenantQuery;
import io.resys.thena.spi.DocStore.StoreTenantQuery;
import io.resys.thena.support.RepoAssert;
import io.smallrye.mutiny.Uni;

public class EveliEnvirTenantQueryImpl implements EveliEnvirTenantQuery {
  private final EveliEnvirStore ctx;
  private final StoreTenantQuery<EveliEnvirStore> store;
  private String repoName;
  
  
  public EveliEnvirTenantQueryImpl(EveliEnvirStore ctx) {
    this.store = ctx.query();
    this.ctx = ctx;
  }
  
  @Override 
  public Uni<EveliEnvirClient> createIfNot() { 
    return store.createIfNot().onItem().transform(doc -> new EveliEnvirClientImpl(doc)); 
  }
  @Override
  public Uni<EveliEnvirClient> create() {
    return store.create().onItem()
        .transform(doc -> new EveliEnvirClientImpl(doc));
  }
  @Override
  public EveliEnvirClientImpl build() {
    return new EveliEnvirClientImpl(store.build());
  }
  @Override
  public Uni<EveliEnvirClient> delete() {
    return store.delete().onItem()
        .transform(doc -> new EveliEnvirClientImpl(doc));
  }
  @Override
  public Uni<EveliEnvirClient> deleteAll() {
    return store.deleteAll().onItem()
        .transform(doc -> new EveliEnvirClientImpl(ctx));
  }
  @Override
  public EveliEnvirTenantQueryImpl tenantName(String tenantName) {
    this.repoName = tenantName;
    store.repoName(tenantName);
    return this;
  }
  @Override
  public Uni<Optional<EveliEnvirClient>> get() {
    RepoAssert.notEmpty(repoName, () -> "tenantName must be defined!");
    
    final var client = ctx.getConfig().getClient();
    return client.tenants().find().id(repoName)
        .get().onItem().transform(existing -> {
          if(existing == null) {
            final Optional<EveliEnvirClient> result = Optional.empty();
            return result;
          }
          return Optional.of(new EveliEnvirClientImpl(store.build()));
        });
    
  }
}
