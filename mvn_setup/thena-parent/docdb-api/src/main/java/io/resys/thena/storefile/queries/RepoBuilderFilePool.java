package io.resys.thena.storefile.queries;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import io.resys.thena.api.entities.Tenant;
import io.resys.thena.datasource.ThenaSqlDataSourceErrorHandler;
import io.resys.thena.datasource.ThenaSqlDataSourceErrorHandler.SqlSchemaFailed;
import io.resys.thena.spi.DbCollections;
import io.resys.thena.spi.DbState.RepoBuilder;
import io.resys.thena.storefile.FileBuilder;
import io.resys.thena.storefile.tables.Table.FileMapper;
import io.resys.thena.storefile.tables.Table.FilePool;
import io.resys.thena.storefile.tables.Table.FilePreparedQuery;
import io.resys.thena.storefile.tables.Table.FileStatement;
import io.resys.thena.storefile.tables.Table.FileTuple;
import io.resys.thena.storefile.tables.Table.FileTupleList;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.groups.UniJoin.JoinAllStrategy;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RepoBuilderFilePool implements RepoBuilder {
  private final FilePool client;
  private final DbCollections names;
  private final FileMapper mapper;
  private final FileBuilder builder;
  private final ThenaSqlDataSourceErrorHandler errorHandler;


  @Override
  public Uni<Tenant> getByName(String name) {
    final var sql = builder.repo().getByName(name);
    return client.preparedQuery(sql)
        .mapping(row -> mapper.repo(row))
        .execute()
        .onItem()
        .transform((Collection<Tenant> rowset) -> {
          final var it = rowset.iterator();
          if(it.hasNext()) {
            return it.next();
          }
          return null;
        })
        .onFailure(e -> errorHandler.notFound(e)).recoverWithNull()
        .onFailure().invoke(e -> {
          
          
          errorHandler.deadEnd(new SqlSchemaFailed("Can't find 'REPOS' by 'name'!", "", e));
        });
  }

  @Override
  public Uni<Tenant> getByNameOrId(String nameOrId) {
    final var sql = builder.repo().getByNameOrId(nameOrId);
    return client.preparedQuery(sql)
        .mapping(row -> mapper.repo(row))
        .execute()
        .onItem()
        .transform((Collection<Tenant> rowset) -> {
          final var it = rowset.iterator();
          if(it.hasNext()) {
            return it.next();
          }
          return null;
        })
        .onFailure(e -> errorHandler.notFound(e)).recoverWithNull()
        .onFailure().invoke(e -> errorHandler.deadEnd(new SqlSchemaFailed("Can't find 'REPOS' by 'name' or 'id'!", "", e)));
  }
  
  @Override
  public Uni<Tenant> insert(final Tenant newRepo) {
    final var next = names.toRepo(newRepo);
    final var sqlBuilder = this.builder.withOptions(next);
    final var repoInsert = sqlBuilder.repo().insertOne(newRepo);
    final var tablesCreate = Arrays.asList(
      sqlBuilder.blobs().create(),
      sqlBuilder.commits().create(),
      sqlBuilder.treeItems().create(),
      sqlBuilder.trees().create(),
      sqlBuilder.refs().create(),
      sqlBuilder.tags().create(),
      
      sqlBuilder.commits().constraints(),
      sqlBuilder.refs().constraints(),
      sqlBuilder.tags().constraints(),
      sqlBuilder.treeItems().constraints()
    );
      
    
    final Uni<Void> create = client.preparedQuery(sqlBuilder.repo().create()).execute()
        .onItem().transformToUni(data -> Uni.createFrom().voidItem())
        .onFailure().invoke(e -> errorHandler.deadEnd(new SqlSchemaFailed("Can't create table 'REPOS'!", "", e)));
    
    
    final Uni<Void> insert = client.preparedQuery(repoInsert).execute()
        .onItem().transformToUni(rowSet -> Uni.createFrom().voidItem())
        .onFailure().invoke(e -> errorHandler.deadEnd(new SqlSchemaFailed("Can't insert into 'REPO': '" + repoInsert.getValue() + "'!", "", e)));
    
    
    final List<Uni<Void>> commands = new ArrayList<Uni<Void>>();
    commands.add(create);
    commands.add(insert);
    
    for(final var table : tablesCreate) {
      FilePreparedQuery<?> query = null;
      if(table instanceof FileStatement) {
        query = client.preparedQuery((FileStatement) table);
      } else if(table instanceof FileTuple) {
        query = client.preparedQuery((FileTuple) table);        
      } else {
        query = client.preparedQuery((FileTupleList) table);
      }
      
      final Uni<Void> nested = query.execute()
          .onItem().transformToUni(rowSet -> Uni.createFrom().voidItem())
          .onFailure().invoke(e -> errorHandler.deadEnd(new SqlSchemaFailed("Can't create tables: " + tablesCreate, "", e)));
      commands.add(nested);
    }    
    
    @SuppressWarnings("unchecked")
    final JoinAllStrategy<Void> join = Uni.join().all(commands.toArray(new Uni[] {}));
        
    return join.andFailFast().onItem().transform((items) -> newRepo);
  }

  @Override
  public Multi<Tenant> findAll() {
    return client.preparedQuery(this.builder.repo().findAll())
    .mapping(row -> mapper.repo(row))
    .execute()
    .onItem()
    .transformToMulti((Collection<Tenant> rowset) -> Multi.createFrom().iterable(rowset))
    .onFailure(e -> errorHandler.notFound(e)).recoverWithCompletion()
    .onFailure().invoke(e -> errorHandler.deadEnd(new SqlSchemaFailed("Can't find 'REPOS'!", "", e)));
  }

  @Override
  public Uni<Tenant> delete(Tenant repo) {
    throw new RuntimeException("Not implemented!!!");
  }
}
