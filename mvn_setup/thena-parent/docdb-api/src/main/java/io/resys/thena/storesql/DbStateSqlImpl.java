package io.resys.thena.storesql;

import java.util.Optional;
import java.util.function.Function;

import io.resys.thena.api.ThenaClient;
import io.resys.thena.api.entities.Tenant;
import io.resys.thena.api.exceptions.RepoException;
import io.resys.thena.api.registry.ThenaRegistry;
import io.resys.thena.datasource.TenantTableNames;
import io.resys.thena.datasource.ThenaDataSource;
import io.resys.thena.datasource.ThenaSqlDataSource;
import io.resys.thena.datasource.ThenaSqlDataSourceErrorHandler;
import io.resys.thena.datasource.ThenaSqlDataSourceImpl;
import io.resys.thena.datasource.vertx.ThenaSqlPoolVertx;
import io.resys.thena.registry.ThenaRegistrySqlImpl;
import io.resys.thena.spi.DbState;
import io.resys.thena.spi.ThenaClientPgSql;
import io.resys.thena.storesql.builders.InternalTenantQueryImpl;
import io.resys.thena.structures.doc.DocState;
import io.resys.thena.structures.git.GitState;
import io.resys.thena.structures.git.GitState.TransactionFunction;
import io.resys.thena.structures.grim.GrimState;
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
    return new InternalTenantQueryImpl(dataSource);
  }

  @Override
  public Uni<GrimState> toGrimState(String tenantId) {
    return tenant().getByNameOrId(tenantId).onItem().transformToUni(tenant -> {
      if(tenant == null) {
        return tenantNotFound(tenantId);
      }
      return Uni.createFrom().item(toGrimState(tenant));
    });
  }
  @Override
  public GrimState toGrimState(Tenant repo) {
    return new GrimDbStateImpl(dataSource.withTenant(repo));
  }
  @Override
  public <R> Uni<R> withGrimTransaction(TxScope scope, io.resys.thena.structures.grim.GrimState.TransactionFunction<R> callback) {
    return toGrimState(scope.getTenantId()).onItem().transformToUni(state -> state.withTransaction(callback));
  }
  
  // git state
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
  public <R> Uni<R> withGitTransaction(TxScope scope, TransactionFunction<R> callback) {
    return toGitState(scope.getTenantId()).onItem().transformToUni(state -> state.withTransaction(callback));
  }
  
  // doc state
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
  public <R> Uni<R> withDocTransaction(TxScope scope, io.resys.thena.structures.doc.DocState.TransactionFunction<R> callback) {
    return toDocState(scope.getTenantId()).onItem().transformToUni(state -> state.withTransaction(callback));
  }
  
  // org state
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
  public <R> Uni<R> withOrgTransaction(TxScope scope, io.resys.thena.structures.org.OrgState.TransactionFunction<R> callback) {
    return toOrgState(scope.getTenantId()).onItem().transformToUni(state -> state.withTransaction(callback));
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
        Builder.defaultRegistry(names)
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
    private Function<TenantTableNames, ThenaRegistry> registry;
    
    public Builder registry(Function<TenantTableNames, ThenaRegistry> registry) {this.registry = registry; return this; }
    
    public Builder errorHandler(ThenaSqlDataSourceErrorHandler errorHandler) {this.errorHandler = errorHandler; return this; }
    public Builder db(String db) { this.db = db; return this; }
    public Builder client(io.vertx.mutiny.sqlclient.Pool client) { this.client = client; return this; }
    public static ThenaRegistry defaultRegistry(TenantTableNames ctx) { return new ThenaRegistrySqlImpl(ctx); }
    
    public ThenaClient build() {
      RepoAssert.notNull(client, () -> "client must be defined!");
      RepoAssert.notNull(db, () -> "db must be defined!");
      
      
      final var ctx = TenantTableNames.defaults(db);
      this.errorHandler = new PgErrors(ctx);
      
      final Function<TenantTableNames, ThenaRegistry> registry = this.registry == null ? Builder::defaultRegistry : this.registry;
      final var pool = new ThenaSqlPoolVertx(client);
      
      final var dataSource = new ThenaSqlDataSourceImpl(
          db, ctx, pool, errorHandler, 
          Optional.empty(),
          registry.apply(ctx)
      );
      
      final var state = new DbStateSqlImpl(dataSource);
      return new ThenaClientPgSql(state);
    }
  }
}
