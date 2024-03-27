package io.resys.thena.storefile;

import io.resys.thena.api.ThenaClient;
import io.resys.thena.api.entities.Tenant;
import io.resys.thena.spi.DbCollections;
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
import io.resys.thena.structures.org.OrgState;
import io.resys.thena.support.ErrorHandler;
import io.resys.thena.support.RepoAssert;
import io.smallrye.mutiny.Uni;

public class DocDBFactoryFile {

  public static Builder create() {
    return new Builder();
  }

  public static DbState state(DbCollections ctx, FilePool client, ErrorHandler handler) {
    return new DbState() {
      @Override
      public ErrorHandler getErrorHandler() {
        return handler;
      }
      @Override
      public DbCollections getCollections() {
        return ctx;
      }
      @Override
      public RepoBuilder tenant() {
        return new RepoBuilderFilePool(client, ctx, sqlMapper(ctx), sqlBuilder(ctx), handler);
      }
      @Override
      public GitState toGitState() {
        return new GitState() {
          @Override
          public <R> Uni<R> withTransaction(String repoId, String headName, TransactionFunction<R> callback) {
            return tenant().getByNameOrId(repoId).onItem().transformToUni(repo -> {
              final GitTenant repoState = withTenant(repo);
              return callback.apply(repoState);
            });
          }
          @Override
          public Uni<GitTenant> withTenant(String repoNameOrId) {
            return tenant().getByNameOrId(repoNameOrId).onItem().transform(repo -> withTenant(repo));
          }
          @Override
          public GitTenant withTenant(Tenant repo) {
            final var wrapper = ImmutableFileClientWrapper.builder()
                .repo(repo)
                .client(client)
                .names(ctx.toRepo(repo))
                .build();
            return new GitTenant() {
              @Override
              public Tenant getRepo() {
                return wrapper.getRepo();
              }
              @Override
              public String getTenantName() {
                return repo.getName();
              }
              @Override
              public GitQueries query() {
                return new ClientQueryFilePool(wrapper, sqlMapper(wrapper.getNames()), sqlBuilder(wrapper.getNames()), handler);
              }
              @Override
              public GitInserts insert() {
                return new ClientInsertBuilderFilePool(wrapper.getClient(), sqlMapper(wrapper.getNames()), sqlBuilder(wrapper.getNames()), handler);
              }
            };
          }
        };
      }
      @Override
      public DocState toDocState() {
        throw new RuntimeException("not implemented");
      }
      @Override
      public OrgState toOrgState() {
        throw new RuntimeException("not implemented");
      }
    };
  }

  public static FileBuilder sqlBuilder(DbCollections ctx) {
    return new DefaultFileBuilder(ctx);
  }
  public static FileMapper sqlMapper(DbCollections ctx) {
    return new DefaultFileMapper();
  }
  
  public static class Builder {
    private FilePool client;
    private String db = "docdb";
    private ErrorHandler errorHandler;
    
    public Builder errorHandler(ErrorHandler errorHandler) {
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

      final var ctx = DbCollections.defaults(db);
      return new ThenaClientPgSql(state(ctx, client, errorHandler));
    }
  }
}
