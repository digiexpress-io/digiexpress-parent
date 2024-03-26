package io.resys.thena.storesql;

import java.util.Optional;
import java.util.function.Function;

import io.resys.thena.api.exceptions.RepoException;
import io.resys.thena.api.models.Repo;
import io.resys.thena.spi.DbCollections;
import io.resys.thena.spi.DbState.RepoBuilder;
import io.resys.thena.storesql.ImmutableClientQuerySqlContext;
import io.resys.thena.storesql.GitDbQueriesSqlImpl.ClientQuerySqlContext;
import io.resys.thena.storesql.builders.DocDbInsertsSqlPool;
import io.resys.thena.storesql.builders.RepoBuilderSqlPool;
import io.resys.thena.storesql.support.ImmutableSqlClientWrapper;
import io.resys.thena.structures.doc.DocInserts;
import io.resys.thena.structures.doc.DocQueries;
import io.resys.thena.structures.doc.DocState;
import io.resys.thena.support.ErrorHandler;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class DocDbStateImpl implements DocState {
  private final DbCollections ctx;
  private final io.vertx.mutiny.sqlclient.Pool pool; 
  private final ErrorHandler handler;
  private final Function<DbCollections, SqlSchema> sqlSchema; 
  private final Function<DbCollections, SqlMapper> sqlMapper;
  private final Function<DbCollections, SqlBuilder> sqlBuilder;
  private final Function<ClientQuerySqlContext, DocQueries> clientQuery;
  
  @Override
  public <R> Uni<R> withTransaction(String repoId, TransactionFunction<R> callback) {
    return pool.withTransaction(conn -> {
      final var repoPool = new RepoBuilderSqlPool(pool, conn, ctx, sqlSchema.apply(ctx), sqlMapper.apply(ctx), sqlBuilder.apply(ctx), handler);
      return repoPool.getByNameOrId(repoId)
        .onItem().transformToUni((repo -> {
          if(repo == null) {
            return repoPool.findAll().collect().asList().onItem().transform(repos -> {
              final var ex = RepoException.builder().notRepoWithName(repoId, repos);
              log.error(ex.getText());
              throw new RepoException(ex.getText());
            });
          }
          
          return Uni.createFrom().item(repo);
        }))
        .onItem().transformToUni((Repo existing) -> {
          if(existing == null) {
            final var ex = RepoException.builder().notRepoWithName(repoId);
            log.error(ex.getText());
            throw new RepoException(ex.getText());
          }
          final var wrapper = ImmutableSqlClientWrapper.builder()
              .repo(existing)
              .pool(pool)
              .tx(conn)
              .names(ctx.toRepo(existing))
              .build();
          return callback.apply(new DocRepoImpl(wrapper, handler, sqlMapper, sqlBuilder, clientQuery));
        });
    });
  }
  @Override
  public Uni<DocQueries> query(String repoNameOrId) {
    return project().getByNameOrId(repoNameOrId).onItem().transform(repo -> query(repo));
  }
  @Override
  public Uni<DocInserts> insert(String repoNameOrId) {
    return project().getByNameOrId(repoNameOrId).onItem().transform(repo -> insert(repo));
  }
  @Override
  public Uni<DocRepo> withRepo(String repoNameOrId) {
    return project().getByNameOrId(repoNameOrId).onItem().transform(repo -> withRepo(repo));
  }
  public RepoBuilder project() {
    return new RepoBuilderSqlPool(pool, null, ctx, sqlSchema.apply(ctx), sqlMapper.apply(ctx), sqlBuilder.apply(ctx), handler);
  }
  @Override
  public DocInserts insert(Repo repo) {
    final var wrapper = ImmutableSqlClientWrapper.builder()
        .repo(repo)
        .pool(pool)
        .tx(Optional.empty())
        .names(ctx.toRepo(repo))
        .build();
    return new DocDbInsertsSqlPool(wrapper, sqlMapper.apply(wrapper.getNames()), sqlBuilder.apply(wrapper.getNames()), handler);
  }
  @Override
  public DocQueries query(Repo repo) {
    final var wrapper = ImmutableSqlClientWrapper.builder()
        .repo(repo)
        .pool(pool)
        .tx(Optional.empty())
        .names(ctx.toRepo(repo))
        .build();
    final var ctx = ImmutableClientQuerySqlContext.builder()
      .mapper(sqlMapper.apply(wrapper.getNames()))
      .builder(sqlBuilder.apply(wrapper.getNames()))
      .wrapper(wrapper)
      .errorHandler(handler)
      .build();
    
    return clientQuery.apply(ctx);
  }
  @Override
  public DocRepo withRepo(Repo repo) {
    final var wrapper = ImmutableSqlClientWrapper.builder()
        .repo(repo)
        .pool(pool)
        .tx(Optional.empty())
        .names(ctx.toRepo(repo))
        .build();
    return new DocRepoImpl(wrapper, handler, sqlMapper, sqlBuilder, clientQuery);
  }
  
}
