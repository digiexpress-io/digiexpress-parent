package io.resys.thena.datasource;

import java.util.Optional;

import io.resys.thena.api.entities.ImmutableTenant;
import io.resys.thena.api.entities.Tenant;
import io.resys.thena.api.entities.Tenant.StructureType;
import io.resys.thena.spi.DbCollections;
import io.vertx.mutiny.sqlclient.Pool;
import io.vertx.mutiny.sqlclient.SqlClient;

public class ThenaSqlDataSourceImpl implements ThenaSqlDataSource {
  private final Tenant tenant;
  private final DbCollections tenantTableNames;
  private final io.vertx.mutiny.sqlclient.Pool pool;
  private final ThenaSqlDataSourceErrorHandler errorHandler;
  private final Optional<SqlClient> tx;
  private final SqlSchema schema;
  private final SqlDataMapper dataMapper;
  private final SqlQueryBuilder queryBuilder;
  private final boolean isTenantLoaded;
  
  public ThenaSqlDataSourceImpl(
      Tenant tenant, 
      DbCollections tenantTableNames, 
      Pool pool,
      ThenaSqlDataSourceErrorHandler errorHandler, 
      Optional<SqlClient> tx, 
      SqlSchema schema,
      SqlDataMapper dataMapper,
      SqlQueryBuilder queryBuilder) {
    super();
    this.tenant = tenant;
    this.tenantTableNames = tenantTableNames.toRepo(tenant);
    this.dataMapper = dataMapper.withOptions(this.tenantTableNames);
    this.queryBuilder = queryBuilder.withOptions(this.tenantTableNames);
    this.schema = schema.withOptions(this.tenantTableNames);
    this.errorHandler = errorHandler.withOptions(this.tenantTableNames);
    this.pool = pool;
    this.tx = tx;
    this.isTenantLoaded = !tenant.getId().equals("") && !tenant.getPrefix().equals("");
  }
  
  public ThenaSqlDataSourceImpl(
      String tenant, 
      DbCollections tenantTableNames, 
      Pool pool,
      ThenaSqlDataSourceErrorHandler errorHandler, 
      Optional<SqlClient> tx, 
      SqlSchema schema,
      SqlDataMapper dataMapper,
      SqlQueryBuilder queryBuilder) {
    super();
    this.isTenantLoaded = false;
    this.tenant = ImmutableTenant.builder()
        .name(tenant)
        .type(StructureType.git)
        .id("")
        .rev("")
        .prefix("")
        .build();
    this.tenantTableNames = tenantTableNames.toRepo(this.tenant);
    this.dataMapper = dataMapper.withOptions(this.tenantTableNames);
    this.queryBuilder = queryBuilder.withOptions(this.tenantTableNames);
    this.errorHandler = errorHandler.withOptions(this.tenantTableNames);
    this.schema = schema.withOptions(this.tenantTableNames);
    this.pool = pool;
    this.tx = tx;
  }
  
  @Override
  public Tenant getTenant() {
    return tenant;
  }
  @Override
  public DbCollections getTenantTableNames() {
    return tenantTableNames;
  }
  @Override
  public Pool getPool() {
    return pool;
  }
  @Override
  public ThenaSqlDataSourceErrorHandler getErrorHandler() {
    return errorHandler;
  }
  @Override
  public Optional<SqlClient> getTx() {
    return tx;
  }
  @Override
  public SqlDataMapper getDataMapper() {
    return dataMapper;
  }
  @Override
  public SqlSchema getSchema() {
    return schema;
  }
  @Override
  public SqlQueryBuilder getQueryBuilder() {
    return queryBuilder;
  }
  @Override
  public ThenaSqlDataSource withTenant(Tenant tenant) {
    return new ThenaSqlDataSourceImpl(tenant, tenantTableNames, pool, errorHandler, tx, schema, dataMapper, queryBuilder);
  }

  @Override
  public boolean isLocked(Throwable t) {
    return this.errorHandler.isLocked(t);
  }

  @Override
  public ThenaSqlDataSource withTx(SqlClient tx) {
    return new ThenaSqlDataSourceImpl(tenant, tenantTableNames, pool, errorHandler, Optional.of(tx), schema, dataMapper, queryBuilder);
  }

  @Override
  public boolean isTenantLoaded() {
    return isTenantLoaded;
  }
}
