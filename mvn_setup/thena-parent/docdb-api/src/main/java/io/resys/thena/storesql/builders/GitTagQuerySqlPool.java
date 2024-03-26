package io.resys.thena.storesql.builders;

import io.resys.thena.api.LogConstants;
import io.resys.thena.api.models.ThenaGitObject.Tag;
import io.resys.thena.models.git.ImmutableDeleteResult;
import io.resys.thena.models.git.GitQueries.DeleteResult;
import io.resys.thena.models.git.GitQueries.GitTagQuery;
import io.resys.thena.storesql.SqlBuilder;
import io.resys.thena.storesql.SqlMapper;
import io.resys.thena.storesql.support.SqlClientWrapper;
import io.resys.thena.support.ErrorHandler;
import io.resys.thena.support.ErrorHandler.SqlFailed;
import io.resys.thena.support.ErrorHandler.SqlTupleFailed;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.RowSet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = LogConstants.SHOW_SQL)
@RequiredArgsConstructor
public class GitTagQuerySqlPool implements GitTagQuery {
  
  private final SqlClientWrapper wrapper;
  private final SqlMapper sqlMapper;
  private final SqlBuilder sqlBuilder;
  private final ErrorHandler errorHandler;

  private String name;

  @Override
  public GitTagQuery name(String name) {
    this.name = name;
    return this;
  }
  @Override
  public Uni<DeleteResult> delete() {
    final var sql = sqlBuilder.tags().deleteByName(name);
    if(log.isDebugEnabled()) {
      log.debug("Tag delete query, with props: {} \r\n{}", 
          sql.getProps().deepToString(),
          sql.getValue());
    }
    return wrapper.getClient().preparedQuery(sql.getValue())
        .execute(sql.getProps())
        .onItem()
        .transform(result -> (DeleteResult) ImmutableDeleteResult.builder().deletedCount(1).build())
        .onFailure().invoke(e -> errorHandler.deadEnd(new SqlTupleFailed("Can't delete 'TAG' by name: '" + name + "'!", sql, e)));
  }
  @Override
  public Uni<Tag> getFirst() {
    final var sql = sqlBuilder.tags().getFirst();
    if(log.isDebugEnabled()) {
      log.debug("Tag getFirst query, with props: {} \r\n{}", 
          "",
          sql.getValue());
    }
    return wrapper.getClient().preparedQuery(sql.getValue())
        .mapping(row -> sqlMapper.tag(row))
        .execute()
        .onItem()
        .transform((RowSet<Tag> rowset) -> {
          final var it = rowset.iterator();
          if(it.hasNext()) {
            return it.next();
          }
          return null;
        })
        .onFailure().invoke(e -> errorHandler.deadEnd(new SqlFailed("Can't find 'TAG'!", sql, e)));      
  }
  @Override
  public Multi<Tag> find() {
    if(name == null || name.isBlank()) {
      final var sql = sqlBuilder.tags().findAll();
      if(log.isDebugEnabled()) {
        log.debug("Tag findAll query, with props: {} \r\n{}", 
            "",
            sql.getValue());
      }
      return wrapper.getClient().preparedQuery(sql.getValue())
          .mapping(row -> sqlMapper.tag(row))
          .execute()
          .onItem()
          .transformToMulti((RowSet<Tag> rowset) -> Multi.createFrom().iterable(rowset))
          .onFailure().invoke(e -> errorHandler.deadEnd(new SqlFailed("Can't find 'TAG'!", sql, e)));      
    }
    final var sql = sqlBuilder.tags().getByName(name);
    
    if(log.isDebugEnabled()) {
      log.debug("Tag getByName query, with props: {} \r\n{}", 
          sql.getProps().deepToString(),
          sql.getValue());
    }
    return wrapper.getClient().preparedQuery(sql.getValue())
        .mapping(row -> sqlMapper.tag(row))
        .execute(sql.getProps())
        .onItem()
        .transformToMulti((RowSet<Tag> rowset) -> Multi.createFrom().iterable(rowset))
        .onFailure().invoke(e -> errorHandler.deadEnd(new SqlTupleFailed("Can't find 'TAG' by name: '" + name + "'!", sql, e)));   
  }
}
