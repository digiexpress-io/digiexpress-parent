package io.resys.thena.fs.tables;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import io.resys.thena.api.annotations.TenantSql;
import io.resys.thena.datasource.ThenaSqlClient.Sql;
import io.resys.thena.datasource.ThenaSqlClient.SqlTuple;
import io.resys.thena.datasource.ThenaSqlClient.SqlTupleList;
import io.resys.thena.fs.entities.Commit;
import io.resys.thena.fs.entities.ImmutableCommit;
import io.resys.thena.fs.entities.ImmutableCommitTransitives;
import io.vertx.mutiny.sqlclient.Row;

@TenantSql.Table(
  name = "commit",
  order = 500,
  ddl = """
    CREATE TABLE {commit} (
      id TEXT PRIMARY KEY,
      commit_created_at TIMESTAMPTZ NOT NULL,
      commit_author TEXT NOT NULL,
      commit_message TEXT NOT NULL,
      tree_id TEXT NOT NULL REFERENCES {tree}(id),
      parent_id TEXT REFERENCES {commit}(id),
      merge_id TEXT REFERENCES {commit}(id)
    );
    
    CREATE INDEX {commit}_tree_idx ON {commit}(tree_id);
    CREATE INDEX {commit}_parent_idx ON {commit}(parent_id);
    CREATE INDEX {commit}_merge_idx ON {commit}(merge_id);
    CREATE INDEX {commit}_created_at_idx ON {commit}(commit_created_at);
    
    COMMENT ON TABLE {commit} IS 'Version control commits representing immutable snapshots of the filesystem state with metadata about the change.';
    COMMENT ON COLUMN {commit}.id IS 'Unique commit identifier (hash)';
    COMMENT ON COLUMN {commit}.commit_created_at IS 'Timestamp when this commit was created, stored in UTC';
    COMMENT ON COLUMN {commit}.commit_author IS 'Author of this commit';
    COMMENT ON COLUMN {commit}.commit_message IS 'Commit message describing the changes';
    COMMENT ON COLUMN {commit}.tree_id IS 'Reference to the root tree representing the complete filesystem state';
    COMMENT ON COLUMN {commit}.parent_id IS 'Reference to the previous commit in the linear history (NULL for initial commit)';
    COMMENT ON COLUMN {commit}.merge_id IS 'Reference to the second parent commit for merge commits (NULL for regular commits)';
  """,
  constraints = "",
  drop = """
    DROP TABLE IF EXISTS {commit} CASCADE;
  """
)
public interface CommitTable {

  @TenantSql.FindAll(
    sql = """
      SELECT commit.id, commit.commit_created_at, commit.commit_author, commit.commit_message, 
             commit.tree_id, commit.parent_id, commit.merge_id,
             parent_commit.commit_created_at as parent_created_at,
             merge_commit.commit_created_at as merge_created_at
      FROM {commit} as commit
      LEFT JOIN {commit} as parent_commit ON commit.parent_id = parent_commit.id
      LEFT JOIN {commit} as merge_commit ON commit.merge_id = merge_commit.id
    """,
    rowMapper = CommitMapper.class
  )
  Sql findAll();

  @TenantSql.Find(
    optional = false,
    sql = """
      SELECT commit.id, commit.commit_created_at, commit.commit_author, commit.commit_message, 
             commit.tree_id, commit.parent_id, commit.merge_id,
             parent_commit.commit_created_at as parent_created_at,
             merge_commit.commit_created_at as merge_created_at
      FROM {commit} as commit
      LEFT JOIN {commit} as parent_commit ON commit.parent_id = parent_commit.id
      LEFT JOIN {commit} as merge_commit ON commit.merge_id = merge_commit.id
      WHERE commit.id = $1
    """,
    rowMapper = CommitMapper.class
  )
  SqlTuple getById(String id);

  @TenantSql.FindAll(
    sql = """
      SELECT commit.id, commit.commit_created_at, commit.commit_author, commit.commit_message, 
             commit.tree_id, commit.parent_id, commit.merge_id,
             parent_commit.commit_created_at as parent_created_at,
             merge_commit.commit_created_at as merge_created_at
      FROM {commit} as commit
      LEFT JOIN {commit} as parent_commit ON commit.parent_id = parent_commit.id
      LEFT JOIN {commit} as merge_commit ON commit.merge_id = merge_commit.id
      WHERE commit.tree_id = $1
    """,
    rowMapper = CommitMapper.class
  )
  SqlTuple findAllByTreeId(String treeId);

  @TenantSql.InsertAll(
    sql = """
      INSERT INTO {commit}
      (id, commit_created_at, commit_author, commit_message, tree_id, parent_id, merge_id)
      VALUES($1, $2, $3, $4, $5, $6, $7)
    """,
    propsMapper = CommitInsertMapper.class
  )
  SqlTupleList insertMany(List<Commit> commits);

  @TenantSql.UpdateAll(
    sql = """
      UPDATE {commit}
      SET commit_created_at = $1, commit_author = $2, commit_message = $3, 
          tree_id = $4, parent_id = $5, merge_id = $6
      WHERE id = $7
    """,
    propsMapper = CommitUpdateMapper.class
  )
  SqlTupleList updateMany(List<Commit> commits);

  @TenantSql.DeleteAll(
    sql = "DELETE FROM {commit} WHERE id = $1",
    propsMapper = CommitDeleteMapper.class
  )
  SqlTupleList deleteAll(List<Commit> commits);

  class CommitMapper implements TenantSql.RowMapper<Commit> {
    @Override
    public Commit apply(Row row) {
      final String parentId = row.getString("parent_id");
      final String mergeId = row.getString("merge_id");
      final OffsetDateTime parentCreatedAt = row.getOffsetDateTime("parent_created_at");
      final OffsetDateTime mergeCreatedAt = row.getOffsetDateTime("merge_created_at");

      return ImmutableCommit.builder()
          .id(row.getString("id"))
          .commitCreatedAt(row.getOffsetDateTime("commit_created_at"))
          .commitAuthor(row.getString("commit_author"))
          .commitMessage(row.getString("commit_message"))
          .treeId(row.getString("tree_id"))
          .parentId(Optional.ofNullable(parentId))
          .mergeId(Optional.ofNullable(mergeId))
          .transitives(ImmutableCommitTransitives.builder()
              .parentCreatedAt(Optional.ofNullable(parentCreatedAt))
              .mergeCreatedAt(Optional.ofNullable(mergeCreatedAt))
              .build())
          .build();
    }
  }

  class CommitInsertMapper implements TenantSql.PropsMapper<Commit> {
    @Override
    public io.vertx.mutiny.sqlclient.Tuple apply(Commit commit) {
      return io.vertx.mutiny.sqlclient.Tuple.from(new Object[]{
        commit.getId(),
        commit.getCommitCreatedAt(),
        commit.getCommitAuthor(),
        commit.getCommitMessage(),
        commit.getTreeId(),
        commit.getParentId().orElse(null),
        commit.getMergeId().orElse(null)
      });
    }
  }

  class CommitUpdateMapper implements TenantSql.PropsMapper<Commit> {
    @Override
    public io.vertx.mutiny.sqlclient.Tuple apply(Commit commit) {
      return io.vertx.mutiny.sqlclient.Tuple.from(new Object[]{
        commit.getCommitCreatedAt(),
        commit.getCommitAuthor(),
        commit.getCommitMessage(),
        commit.getTreeId(),
        commit.getParentId().orElse(null),
        commit.getMergeId().orElse(null),
        commit.getId()
      });
    }
  }

  class CommitDeleteMapper implements TenantSql.PropsMapper<Commit> {
    @Override
    public io.vertx.mutiny.sqlclient.Tuple apply(Commit commit) {
      return io.vertx.mutiny.sqlclient.Tuple.from(new Object[]{
        commit.getId()
      });
    }
  }
}