package io.digiexpress.eveli.envir.spi;

import java.util.Optional;

import io.digiexpress.eveli.dialob.api.DialobClient;
import io.digiexpress.eveli.envir.api.EveliEnvirClient;
import io.digiexpress.eveli.envir.api.EveliEnvirTenantQuery;
import io.digiexpress.eveli.envir.spi.actions.EveliRuntimeCache;
import io.resys.hdes.client.spi.config.HdesClientConfig;
import io.resys.thena.spi.DocStore.StoreTenantQuery;
import io.resys.thena.support.RepoAssert;
import io.smallrye.mutiny.Uni;

public class EveliEnvirTenantQueryImpl implements EveliEnvirTenantQuery {
  private final EveliEnvirStore ctx;
  private final StoreTenantQuery<EveliEnvirStore> store;
  private final HdesClientConfig hdesClientConfig;
  private final DialobClient dialobClient;
  private final EveliRuntimeCache cache;
  private String repoName;
  
  
  public EveliEnvirTenantQueryImpl(
      EveliEnvirStore ctx,
      HdesClientConfig hdesClientConfig,
      DialobClient dialobClient,
      EveliRuntimeCache cache) {
    
    this.store = ctx.query();
    this.ctx = ctx;
    this.hdesClientConfig = hdesClientConfig;
    this.dialobClient = dialobClient;
    this.cache = cache;
  }
  
  @Override 
  public Uni<EveliEnvirClient> createIfNot() { 
    return store.createIfNot().onItem().transform(doc -> new EveliEnvirClientImpl(doc, hdesClientConfig, dialobClient, cache)); 
  }
  @Override
  public Uni<EveliEnvirClient> create() {
    return store.create().onItem()
        .transform(doc -> new EveliEnvirClientImpl(doc, hdesClientConfig, dialobClient, cache));
  }
  @Override
  public EveliEnvirClientImpl build() {
    return new EveliEnvirClientImpl(store.build(), hdesClientConfig, dialobClient, cache);
  }
  @Override
  public Uni<EveliEnvirClient> delete() {
    return store.delete().onItem()
        .transform(doc -> new EveliEnvirClientImpl(doc, hdesClientConfig, dialobClient, cache));
  }
  @Override
  public Uni<EveliEnvirClient> deleteAll() {
    return store.deleteAll().onItem()
        .transform(doc -> new EveliEnvirClientImpl(ctx, hdesClientConfig, dialobClient, cache));
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
          return Optional.of(new EveliEnvirClientImpl(store.build(), hdesClientConfig, dialobClient, cache));
        });
    
  }
}
