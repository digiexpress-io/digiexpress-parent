package io.digiexpress.mig.client.spi;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import io.digiexpress.mig.client.api.MigClient.StencilEntityConverter;
import io.digiexpress.mig.client.api.MigClient.TargetStencilBuilder;
import io.digiexpress.mig.client.api.SourceTasks;
import io.digiexpress.mig.client.api.SourceThena;
import io.digiexpress.mig.client.spi.converters.StencilEntityConverterImpl;
import io.digiexpress.mig.client.spi.loggers.EntityQueryLogger;
import io.digiexpress.mig.client.spi.loggers.TargetThenaLogger;
import io.resys.thena.api.entities.git.Blob;
import io.resys.thena.api.entities.git.Branch;
import io.resys.thena.api.entities.git.Tree;
import io.resys.thena.git.spi.sql.GitTableNames;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import io.vertx.mutiny.sqlclient.Tuple;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TargetStencilBuilderImpl implements TargetStencilBuilder {
  private final TargetThenaLogger logger = new TargetThenaLogger();
  private final io.vertx.mutiny.pgclient.PgPool pool;
  private StencilEntityConverter stencilEntityConverter;
  private GitTableNames names;

  @Override
  public Uni<SourceThena> build(SourceThena source, SourceTasks tasks, String tenantName) {
    this.names = GitTableNames.defaults().toRepo(tenantName);
    this.stencilEntityConverter = new StencilEntityConverterImpl(tasks.getWorkflows().values());
    return pool.withTransaction(conn -> execute(conn, source));
  }
  /**
   * 
delete from STENCIL_AS12_git_refs;
delete from STENCIL_AS12_git_tags;
delete from STENCIL_AS12_git_commits;
delete from STENCIL_AS12_git_treeitems;
delete from STENCIL_AS12_git_trees;
delete from STENCIL_AS12_git_blobs;
   */

  private Uni<SourceThena> execute(io.vertx.mutiny.sqlclient.SqlConnection conn, SourceThena source) {

    return Uni.combine().all()
        .unis(
            createBlobs(conn, source),
            createTrees(conn, source), 
            createTreeValues(conn, source),
            createCommits(conn, source),
            createRefs(conn, source)
        ).asTuple().onItem().transform(e -> {
          logger.ok(source);
          return source;
        });
  }
  private Uni<?> createCommits(io.vertx.mutiny.sqlclient.SqlConnection conn, SourceThena source) {
    final var sql = "INSERT INTO  " + names.getCommits() + """
        (id, datetime, author, message, tree, parent, merge) VALUES($1, $2, $3, $4, $5, $6, $7)
        ON CONFLICT (id) DO NOTHING
        RETURNING *
        """;

    final var props = source.getCommits().values().stream()
        .sorted((a, b) -> a.getDateTime().compareTo(b.getDateTime()))
        .map(commit -> 
          Tuple.from(Arrays.asList(
              commit.getId(), 
              commit.getDateTime().toString(), 
              commit.getAuthor(), 
              commit.getMessage(), 
              commit.getTree(), 
              commit.getParent().orElse(null), 
              commit.getMerge().orElse(null)))
          
        )
        .collect(Collectors.toList());
    return batch(conn, Tree.class, sql, props);
  }
  private Uni<?> createTrees(io.vertx.mutiny.sqlclient.SqlConnection conn, SourceThena source) {
    final var sql = "INSERT INTO  " + names.getTrees() + """
        (id) VALUES($1)
        ON CONFLICT (id) DO NOTHING
        RETURNING *
        """;

    final var props = source.getTrees().stream()
        .map(doc -> Tuple.from(Arrays.asList(doc.getId())))
        .collect(Collectors.toList());
    return batch(conn, Tree.class, sql, props);
  }
  private Uni<?> createTreeValues(io.vertx.mutiny.sqlclient.SqlConnection conn, SourceThena source) {
    final var sql = "INSERT INTO  " + names.getTreeItems() + """
        (name, blob, tree) VALUES($1, $2, $3)
        ON CONFLICT (name, blob, tree) DO NOTHING
        RETURNING *
        """;

    final var props = source.getTreeValues().stream()
        .map(doc -> Tuple.from(Arrays.asList(doc.getName(), doc.getBlob(), doc.getTree())))
        .collect(Collectors.toList());
    return batch(conn, Tree.class, sql, props);
  }
  
  private Uni<?> createBlobs(io.vertx.mutiny.sqlclient.SqlConnection conn, SourceThena source) {
    final var sql = "INSERT INTO  " + names.getBlobs() + """
        (id, value) VALUES($1, $2)
        ON CONFLICT (id) DO NOTHING
        RETURNING *
        """;

    final var props = source.getBlobs().values().stream()
        .map(doc -> 
          Tuple.from(Arrays.asList(doc.getId(), stencilEntityConverter.convertValue(doc.getValue())))
        )
        .collect(Collectors.toList());
    return batch(conn, Blob.class, sql, props);
  }
  
  
  private Uni<?> createRefs(io.vertx.mutiny.sqlclient.SqlConnection conn, SourceThena source) {
    final var sql = "INSERT INTO  " + names.getRefs() + """
        (name, commit) VALUES($1, $2)
        ON CONFLICT (name) DO NOTHING
        RETURNING *
        """;

    final var props = source.getBranches().stream()
        .map(doc -> Tuple.from(Arrays.asList(doc.getName(), doc.getCommit())))
        .collect(Collectors.toList());
    return batch(conn, Branch.class, sql, props);
  }

  private <T> Uni<RowSet<Row>> batch(
      io.vertx.mutiny.sqlclient.SqlConnection conn, 
      Class<T> type, 
      String sql,
      List<Tuple> props) {

    final EntityQueryLogger<T> logger = this.logger.entityQuery(type).query(sql, props);
    
    return conn.preparedQuery(sql).executeBatch(props).onItem()
        .invoke(data -> logger.queryOk(data)).onFailure()
        .invoke(e -> logger.queryFail(e));
  }
}
