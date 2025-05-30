package io.digiexpress.thena.batch.client.spi.persistence.sql;

import java.util.Optional;

import io.digiexpress.thena.batch.client.api.BatchException;
import io.digiexpress.thena.batch.client.api.persistence.BatchDb;
import io.digiexpress.thena.batch.client.api.persistence.BatchDbBuilder;
import io.digiexpress.thena.batch.client.api.persistence.BatchDbQuery;
import io.resys.thena.api.entities.Tenant;
import io.resys.thena.datasource.TenantCacheImpl;
import io.resys.thena.datasource.TenantContext;
import io.resys.thena.datasource.ThenaDataSource;
import io.resys.thena.datasource.ThenaSqlDataSource;
import io.resys.thena.datasource.ThenaSqlDataSource.TenantCache;
import io.resys.thena.datasource.ThenaSqlDataSourceErrorHandler;
import io.resys.thena.datasource.ThenaSqlDataSourceImpl;
import io.resys.thena.datasource.vertx.ThenaSqlPoolVertx;
import io.resys.thena.storesql.PgErrors;
import io.resys.thena.support.RepoAssert;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@RequiredArgsConstructor
@Slf4j
public class BatchDbImpl implements BatchDb {
  private final ThenaSqlDataSource dataSource;

  @Override
  public ThenaDataSource getDataSource() {
    return dataSource;
  }

  @Override
  public InternalTenantQuery tenant() {
    return new BatchDbInternalTenantQuery(dataSource);
  }

  @Override
  public Uni<BatchDb> withTenant(String tenantId) {
    return tenant().getByNameOrId(tenantId).onItem().transformToUni(tenant -> {
      if(tenant == null) {
        return tenantNotFound(tenantId);
      }
      return Uni.createFrom().item(withTenant(tenant));
    });
  }

  @Override
  public BatchDb withTenant(Tenant tenant) {
    return new BatchDbImpl(dataSource.withTenant(tenant));
  }

  @Override
  public <R> Uni<R> withTransaction(TxScope scope, TransactionFunction<R> callback) {
    return dataSource.getPool().withTransaction(conn -> {
      return callback.apply(new BatchDbImpl(dataSource.withTx(conn)));
    });
  }
  private <T> Uni<T> tenantNotFound(String tenantId) {
    return tenant().findAll().collect().asList().onItem().transform(tenants -> {
      final var text = new StringBuilder()
          .append("Batch tenant with name: '").append(tenantId).append("' does not exist!")
          .append(" known tenants: '").append(String.join(",", tenants.stream().map(r -> r.getName()).toList())).append("'")
          .toString();
      
      log.error(text);
      throw new BatchException(text);
    }); 
  }
  

  public static BatchDbImpl create(TenantContext names, io.vertx.mutiny.sqlclient.Pool client, TenantCache tenantCache) {
    final var pool = new ThenaSqlPoolVertx(client);
    final var errorHandler = new PgErrors();
    final var dataSource = new ThenaSqlDataSourceImpl(
        "", names, pool, errorHandler, 
        Optional.empty(),
        tenantCache
    );
    return new BatchDbImpl(dataSource);
  }


  public static Builder create() {
    return new Builder();
  }


  public static class Builder {
    private io.vertx.mutiny.sqlclient.Pool client;
    private String db = "docdb";
    private ThenaSqlDataSourceErrorHandler errorHandler;

    private TenantCache tenantCache;    
    public Builder errorHandler(ThenaSqlDataSourceErrorHandler errorHandler) {this.errorHandler = errorHandler; return this; }
    public Builder db(String db) { this.db = db; return this; }
    public Builder tenantCache(TenantCache tenantCache) { this.tenantCache = tenantCache; return this; }
    public Builder client(io.vertx.mutiny.sqlclient.Pool client) { this.client = client; return this; }

    
    public BatchDbImpl build() {
      RepoAssert.notNull(client, () -> "client must be defined!");
      RepoAssert.notNull(db, () -> "db must be defined!");
      
      final var tenantCache = this.tenantCache == null ? new TenantCacheImpl() : this.tenantCache;
      
      final var ctx = TenantContext.defaults(db);
      this.errorHandler = new PgErrors();
      
      
      final var pool = new ThenaSqlPoolVertx(client);
      
      final var dataSource = new ThenaSqlDataSourceImpl(
          db, ctx, pool, errorHandler, 
          Optional.empty(),
          tenantCache
      );
      return new BatchDbImpl(dataSource);
    }
  }


  @Override
  public BatchDbQuery query() {
    return new BatchDbQueryImpl(dataSource);
  }

  @Override
  public BatchDbBuilder builder() {
    return new BatchDbBuilderImpl(dataSource);
  }

  @Override
  public Uni<BatchDb> withTenant() {
    if(this.dataSource.isTenantLoaded()) {
      return Uni.createFrom().item(this);
    }
    return this.withTenant(this.dataSource.getTenant().getName());
  }
}
