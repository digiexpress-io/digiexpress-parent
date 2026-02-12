package io.resys.thena.fs.tables.mappers;

import java.util.Optional;

import io.resys.thena.api.annotations.TenantSql;
import io.resys.thena.fs.entities.Commit;
import io.resys.thena.fs.entities.ImmutableCommit;
import io.resys.thena.fs.entities.ImmutableCommitTransitives;
import io.vertx.mutiny.sqlclient.Row;

public class CommitMapper implements TenantSql.RowMapper<Commit> {
  @Override
  public Commit apply(Row row) {
    final String parentId = row.getString("parent_id");
    final String mergeId = row.getString("merge_id");

    return ImmutableCommit.builder()
      .id(row.getString("id"))
      .commitCreatedAt(row.getOffsetDateTime("commit_created_at"))
      .commitAuthor(row.getString("commit_author"))
      .commitMessage(row.getString("commit_message"))
      .treeId(row.getString("tree_id"))
      .parentId(Optional.ofNullable(parentId))
      .mergeId(Optional.ofNullable(mergeId))
      .transitives(ImmutableCommitTransitives.builder().build())
      .build();
  }
}
