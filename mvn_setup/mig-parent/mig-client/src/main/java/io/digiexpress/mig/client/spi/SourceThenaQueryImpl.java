package io.digiexpress.mig.client.spi;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.digiexpress.mig.client.api.ImmutableSourceThena;
import io.digiexpress.mig.client.api.ImmutableTreeValueExt;
import io.digiexpress.mig.client.api.MigClient.SourceThenaQuery;
import io.digiexpress.mig.client.api.SourceThena;
import io.digiexpress.mig.client.api.SourceThena.TreeValueExt;
import io.digiexpress.mig.client.spi.loggers.SourceThenaLogger;
import io.resys.thena.api.entities.git.ImmutableBlob;
import io.resys.thena.api.entities.git.ImmutableBranch;
import io.resys.thena.api.entities.git.ImmutableCommit;
import io.resys.thena.api.entities.git.ImmutableTag;
import io.resys.thena.api.entities.git.ImmutableTree;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class SourceThenaQueryImpl implements SourceThenaQuery {
  private final io.vertx.mutiny.pgclient.PgPool pool;
  private final SourceThenaLogger logger = new SourceThenaLogger();
  
  @Override
  public Uni<SourceThena> findAll(String tenanPrefix) {
    return Uni.combine().all().unis(
        getBlobs(tenanPrefix),
        getBranches(tenanPrefix),
        getCommits(tenanPrefix),
        getTags(tenanPrefix),
        getTrees(tenanPrefix),
        getTreeValues(tenanPrefix)
    ).asTuple().onItem().transform(sources -> {
      final SourceThena result = ImmutableSourceThena.builder()
          .tenantPrefix(tenanPrefix)
          .blobs(sources.getItem1().stream().collect(Collectors.toMap(e -> e.getId(), e -> e)))
          .branches(sources.getItem2())
          .commits(sources.getItem3().stream().collect(Collectors.toMap(e -> e.getId(), e -> e)))
          .tags(sources.getItem4())
          .trees(sources.getItem5())
          .treeValues(sources.getItem6())
          .build();
      logger.ok(result);
      return result;
    })
    .onFailure().invoke(e -> logger.fail(e));
  }
  
  private Uni<List<io.resys.thena.api.entities.git.Blob>> getBlobs(String tenanPrefix) {
    final var sql = "select * from " + tenanPrefix + "blobs";
    return processAnyQuery(io.resys.thena.api.entities.git.Blob.class, sql, (row) -> ImmutableBlob.builder()
        .id(row.getString("id"))
        .value(getJsonValue(row, "value"))
        .build());
  }
  private Uni<List<io.resys.thena.api.entities.git.Branch>> getBranches(String tenanPrefix) {
    final var sql = "select * from " + tenanPrefix + "refs";
    return processAnyQuery(io.resys.thena.api.entities.git.Branch.class, sql, (row) -> ImmutableBranch.builder()
        .name(row.getString("name"))
        .commit(row.getString("commit"))
        .build());
  }
  
  private Uni<List<io.resys.thena.api.entities.git.Commit>> getCommits(String tenanPrefix) {
    final var sql = "select * from " + tenanPrefix + "commits";
    return processAnyQuery(io.resys.thena.api.entities.git.Commit.class, sql, (row) -> ImmutableCommit.builder()
        .id(row.getString("id"))
        .author(row.getString("author"))
        .dateTime(LocalDateTime.parse(row.getString("datetime")))
        .message(row.getString("message"))
        .parent(Optional.ofNullable(row.getString("parent")))
        .merge(Optional.ofNullable(row.getString("merge")))
        .tree(row.getString("tree"))
        .build());
  }
  
  private Uni<List<io.resys.thena.api.entities.git.Tag>> getTags(String tenanPrefix) {
    final var sql = "select * from " + tenanPrefix + "tags";
    return processAnyQuery(io.resys.thena.api.entities.git.Tag.class, sql, (row) -> ImmutableTag.builder()
        .author(row.getString("author"))
        .dateTime(LocalDateTime.parse(row.getString("datetime")))
        .message(row.getString("message"))
        .commit(row.getString("commit"))
        .name(row.getString("id"))
        .build());
  }
  
  private Uni<List<TreeValueExt>> getTreeValues(String tenanPrefix) {
    final var sql = "select * from " + tenanPrefix + "treeitems";
    return processAnyQuery(TreeValueExt.class, sql, (row) -> ImmutableTreeValueExt.builder()
        .name(row.getString("name"))
        .blob(row.getString("blob"))
        .tree(row.getString("tree"))
        .build());
  }
  
  
  private Uni<List<io.resys.thena.api.entities.git.Tree>> getTrees(String tenanPrefix) {
    final var sql = "select * from " + tenanPrefix + "trees";
    return processAnyQuery(io.resys.thena.api.entities.git.Tree.class, sql, (row) -> ImmutableTree.builder()
        .id(row.getString("id"))
        .build());
  }
  
  private JsonObject getJsonValue(Row row, String column) {
    try {
      return new JsonObject(row.getString(column));
    } catch(Exception e) {
      return row.getJsonObject(column);      
    }
  }
  
  private <T> Uni<List<T>> processAnyQuery(Class<T> type, String sql, Function<io.vertx.mutiny.sqlclient.Row, T> mapper) {
    final var logger = this.logger.entityQuery(type);
    logger.query(sql);
    return pool.preparedQuery(sql)
      .mapping(row -> {
        try {
          final T result = mapper.apply(row);
          logger.mappingOk(result);
          return result;
        } catch(Exception e) {
          logger.mappingFail(row, e);
          return null;
        }
      })
      .execute()
      .onItem()
      .transformToMulti(RowSet::toMulti).collect().asList()
      .onItem().transform(data -> data.stream().filter(e -> e != null).toList())
      .onItem().invoke(e -> logger.queryOk(e))
      .onFailure().invoke(e -> logger.queryFail(e));
  }
}
