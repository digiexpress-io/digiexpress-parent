package io.resys.thena.storefile;

import io.resys.thena.api.ThenaClient;
import io.resys.thena.api.entities.Tenant;
import io.resys.thena.datasource.TenantTableNames;
import io.resys.thena.datasource.ThenaDataSource;
import io.resys.thena.datasource.ThenaFileDataSourceImpl;
import io.resys.thena.datasource.ThenaSqlDataSourceErrorHandler;
import io.resys.thena.spi.DbState;
import io.resys.thena.spi.ThenaClientPgSql;
import io.resys.thena.storefile.queries.ClientInsertBuilderFilePool;
import io.resys.thena.storefile.queries.RepoBuilderFilePool;
import io.resys.thena.storefile.tables.ImmutableFileClientWrapper;
import io.resys.thena.storefile.tables.Table.FileMapper;
import io.resys.thena.storefile.tables.Table.FilePool;
import io.resys.thena.structures.doc.DocState;
import io.resys.thena.structures.git.GitInserts;
import io.resys.thena.structures.git.GitQueries;
import io.resys.thena.structures.git.GitState;
import io.resys.thena.structures.git.GitState.TransactionFunction;
import io.resys.thena.structures.org.OrgState;
import io.resys.thena.support.RepoAssert;
import io.smallrye.mutiny.Uni;

public class DocDBFactoryFile {

  public static Builder create() {
    return new Builder();
  }

  public static DbState state(TenantTableNames ctx, FilePool client, ThenaSqlDataSourceErrorHandler handler) {
    return new DbState() {
      @Override
      public InternalTenantQuery tenant() {
        return new RepoBuilderFilePool(client, ctx, sqlMapper(ctx), sqlBuilder(ctx), handler);
      }
      @Override
      public <R> Uni<R> withGitTransaction(String tenantId, TransactionFunction<R> callback) {
        return toGitState(tenantId).onItem().transformToUni(state -> state.withTransaction(callback));
      }
      @Override
      public Uni<GitState> toGitState(String tenantId) {
        return tenant().getByNameOrId(tenantId).onItem().transform(repo -> toGitState(repo));
      }
      @Override
      public GitState toGitState(Tenant repo) {
        final var wrapper = ImmutableFileClientWrapper.builder()
            .repo(repo)
            .client(client)
            .names(ctx.toRepo(repo))
            .build();
        final var dataSource = new ThenaFileDataSourceImpl(repo, ctx);
        return new GitState() {
          @Override
          public GitQueries query() {
            return new ClientQueryFilePool(wrapper, sqlMapper(wrapper.getNames()), sqlBuilder(wrapper.getNames()), handler);
          }
          @Override
          public GitInserts insert() {
            return new ClientInsertBuilderFilePool(wrapper.getClient(), sqlMapper(wrapper.getNames()), sqlBuilder(wrapper.getNames()), handler);
          }
          @Override
          public <R> Uni<R> withTransaction(TransactionFunction<R> callback) {
            return tenant().getByNameOrId(wrapper.getRepo().getId()).onItem().transformToUni(repo -> {
              return callback.apply(this);
            });
          }
          @Override
          public ThenaDataSource getDataSource() {
            return dataSource;
          }
        };
      }
      @Override
      public ThenaDataSource getDataSource() {
        throw new RuntimeException("not implemented");
      }
      @Override
      public Uni<DocState> toDocState(String tenantId) {
        throw new RuntimeException("not implemented");
      }
      @Override
      public DocState toDocState(Tenant repo) {
        throw new RuntimeException("not implemented");
      }
      @Override
      public <R> Uni<R> withDocTransaction(String tenantId,
          io.resys.thena.structures.doc.DocState.TransactionFunction<R> callback) {
        throw new RuntimeException("not implemented");
      }
      @Override
      public Uni<OrgState> toOrgState(String tenantId) {
        throw new RuntimeException("not implemented");
      }
      @Override
      public OrgState toOrgState(Tenant repo) {
        throw new RuntimeException("not implemented");
      }
      @Override
      public <R> Uni<R> withOrgTransaction(String tenantId,
          io.resys.thena.structures.org.OrgState.TransactionFunction<R> callback) {
        throw new RuntimeException("not implemented");
      }

    };
  }  
  public static FileBuilder sqlBuilder(TenantTableNames ctx) {
    return new DefaultFileBuilder(ctx);
  }
  public static FileMapper sqlMapper(TenantTableNames ctx) {
    return new DefaultFileMapper();
  }
  
  public static class Builder {
    private FilePool client;
    private String db = "docdb";
    private ThenaSqlDataSourceErrorHandler errorHandler;
    
    public Builder errorHandler(ThenaSqlDataSourceErrorHandler errorHandler) {
      this.errorHandler = errorHandler;
      return this;
    }
    public Builder db(String db) {
      this.db = db;
      return this;
    }
    public Builder client(FilePool client) {
      this.client = client;
      return this;
    }
    public ThenaClient build() {
      RepoAssert.notNull(client, () -> "client must be defined!");
      RepoAssert.notNull(db, () -> "db must be defined!");
      RepoAssert.notNull(errorHandler, () -> "errorHandler must be defined!");

      final var ctx = TenantTableNames.defaults(db);
      return new ThenaClientPgSql(state(ctx, client, errorHandler));
    }
  }
}
