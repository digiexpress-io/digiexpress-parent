package io.resys.thena.storesql;

import java.util.Optional;
import java.util.function.Function;

import io.resys.thena.api.ThenaClient;
import io.resys.thena.api.entities.Tenant;
import io.resys.thena.api.exceptions.RepoException;
import io.resys.thena.datasource.TenantTableNames;
import io.resys.thena.datasource.SqlDataMapper;
import io.resys.thena.datasource.SqlQueryBuilder;
import io.resys.thena.datasource.SqlSchema;
import io.resys.thena.datasource.ThenaDataSource;
import io.resys.thena.datasource.ThenaSqlDataSource;
import io.resys.thena.datasource.ThenaSqlDataSourceErrorHandler;
import io.resys.thena.datasource.ThenaSqlDataSourceImpl;
import io.resys.thena.datasource.vertx.ThenaSqlPoolVertx;
import io.resys.thena.spi.DbState;
import io.resys.thena.spi.ThenaClientPgSql;
import io.resys.thena.storesql.builders.RepoBuilderSqlPool;
import io.resys.thena.structures.doc.DocState;
import io.resys.thena.structures.git.GitState;
import io.resys.thena.structures.git.GitState.TransactionFunction;
import io.resys.thena.structures.org.OrgState;
import io.resys.thena.support.RepoAssert;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class DbStateSqlImpl implements DbState {
  private final ThenaSqlDataSource dataSource;

  @Override
  public ThenaDataSource getDataSource() {
    return dataSource;
  }
  @Override
  public InternalTenantQuery tenant() {
    return new RepoBuilderSqlPool(dataSource);
  }
  @Override
  public Uni<GitState> toGitState(String tenantId) {
    return tenant().getByNameOrId(tenantId).onItem().transformToUni(tenant -> {
      if(tenant == null) {
        return tenantNotFound(tenantId);
      }
      return Uni.createFrom().item(toGitState(tenant));
    });
  }
  @Override
  public GitState toGitState(Tenant repo) {
    return new GitDbStateImpl(dataSource.withTenant(repo));
  }
  @Override
  public <R> Uni<R> withGitTransaction(String tenantId, TransactionFunction<R> callback) {
    return toGitState(tenantId).onItem().transformToUni(state -> state.withTransaction(callback));
  }
  @Override
  public Uni<DocState> toDocState(String tenantId) {
    return tenant().getByNameOrId(tenantId).onItem().transformToUni(tenant -> {
      if(tenant == null) {
        return tenantNotFound(tenantId);
      }
      return Uni.createFrom().item(toDocState(tenant));
    });
  }
  @Override
  public DocState toDocState(Tenant repo) {
    return new DocDbStateImpl(dataSource.withTenant(repo));
  }
  @Override
  public <R> Uni<R> withDocTransaction(String tenantId, io.resys.thena.structures.doc.DocState.TransactionFunction<R> callback) {
    return toDocState(tenantId).onItem().transformToUni(state -> state.withTransaction(callback));
  }
  @Override
  public Uni<OrgState> toOrgState(String tenantId) {
    return tenant().getByNameOrId(tenantId).onItem().transformToUni(tenant -> {
      if(tenant == null) {
        return tenantNotFound(tenantId);
      }
      return Uni.createFrom().item(toOrgState(tenant));
    });
  }
  @Override
  public OrgState toOrgState(Tenant repo) {
    return new OrgDbStateImpl(dataSource.withTenant(repo));
  }
  @Override
  public <R> Uni<R> withOrgTransaction(String tenantId, io.resys.thena.structures.org.OrgState.TransactionFunction<R> callback) {
    return toOrgState(tenantId).onItem().transformToUni(state -> state.withTransaction(callback));
  }
  private <T> Uni<T> tenantNotFound(String tenantId) {
    return tenant().findAll().collect().asList().onItem().transform(repos -> {
      final var ex = RepoException.builder().notRepoWithName(tenantId, repos);
      log.error(ex.getText());
      throw new RepoException(ex.getText());
    }); 
  }

  public static DbStateSqlImpl create(TenantTableNames names, io.vertx.mutiny.sqlclient.Pool client) {
    final var pool = new ThenaSqlPoolVertx(client);
    final var errorHandler = new PgErrors(names);
    final var dataSource = new ThenaSqlDataSourceImpl(
        "", names, pool, errorHandler, 
        Optional.empty(),
        Builder.defaultSqlSchema(names), 
        Builder.defaultSqlMapper(names), 
        Builder.defaultSqlBuilder(names)
    );
    return new DbStateSqlImpl(dataSource);
  }
  
  public static Builder create() {
    return new Builder();
  }

  public static class Builder {
    private io.vertx.mutiny.sqlclient.Pool client;
    private String db = "docdb";
    private ThenaSqlDataSourceErrorHandler errorHandler;
    private Function<TenantTableNames, SqlSchema> sqlSchema; 
    private Function<TenantTableNames, SqlDataMapper> sqlMapper;
    private Function<TenantTableNames, SqlQueryBuilder> sqlBuilder;
    
    public Builder sqlMapper(Function<TenantTableNames, SqlDataMapper> sqlMapper) {this.sqlMapper = sqlMapper; return this; }
    public Builder sqlBuilder(Function<TenantTableNames, SqlQueryBuilder> sqlBuilder) {this.sqlBuilder = sqlBuilder; return this; }
    public Builder sqlSchema(Function<TenantTableNames, SqlSchema> sqlSchema) {this.sqlSchema = sqlSchema; return this; }
    
    public Builder errorHandler(ThenaSqlDataSourceErrorHandler errorHandler) {this.errorHandler = errorHandler; return this; }
    public Builder db(String db) { this.db = db; return this; }
    public Builder client(io.vertx.mutiny.sqlclient.Pool client) { this.client = client; return this; }

    public static SqlQueryBuilder defaultSqlBuilder(TenantTableNames ctx) { return new SqlBuilderImpl(ctx); }
    public static SqlDataMapper defaultSqlMapper(TenantTableNames ctx) { return new SqlMapperImpl(ctx); }
    public static SqlSchema defaultSqlSchema(TenantTableNames ctx) { return new SqlSchemaImpl(ctx); }
    
    public ThenaClient build() {
      RepoAssert.notNull(client, () -> "client must be defined!");
      RepoAssert.notNull(db, () -> "db must be defined!");
      
      
      final var ctx = TenantTableNames.defaults(db);
      this.errorHandler = new PgErrors(ctx);
      
      final Function<TenantTableNames, SqlSchema> sqlSchema = this.sqlSchema == null ? Builder::defaultSqlSchema : this.sqlSchema;
      final Function<TenantTableNames, SqlDataMapper> sqlMapper = this.sqlMapper == null ? Builder::defaultSqlMapper : this.sqlMapper;
      final Function<TenantTableNames, SqlQueryBuilder> sqlBuilder = this.sqlBuilder == null ? Builder::defaultSqlBuilder : this.sqlBuilder;
      final var pool = new ThenaSqlPoolVertx(client);
      
      final var dataSource = new ThenaSqlDataSourceImpl(
          db, ctx, pool, errorHandler, 
          Optional.empty(),
          sqlSchema.apply(ctx), 
          sqlMapper.apply(ctx), 
          sqlBuilder.apply(ctx)
      );
      
      final var state = new DbStateSqlImpl(dataSource);
      return new ThenaClientPgSql(state);
    }
  }
}
