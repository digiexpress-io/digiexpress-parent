package io.digiexpress.thena.batch.client.api.persistence;

import io.resys.thena.api.entities.Tenant;
import io.resys.thena.spi.TenantDataSource;
import io.smallrye.mutiny.Uni;

public interface BatchDb extends TenantDataSource {
  
  // load whatever tenant there is
  Uni<BatchDb> withTenant();
  
  Uni<BatchDb> withTenant(String tenantId);
  BatchDb withTenant(Tenant tenant);
  
  BatchDbQuery query();
  BatchDbBuilder builder();
  
  <R> Uni<R> withTransaction(TxScope scope, TransactionFunction<R> callback);
  
  @FunctionalInterface
  interface TransactionFunction<R> {
    Uni<R> apply(BatchDb repoState);
  }
}
