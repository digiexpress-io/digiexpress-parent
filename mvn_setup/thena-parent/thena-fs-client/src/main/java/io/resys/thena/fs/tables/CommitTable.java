package io.resys.thena.fs.tables;

import java.util.List;
import java.util.Optional;

import io.resys.thena.api.annotations.TenantSql;
import io.resys.thena.api.annotations.TenantSql.SqlBuilder;
import io.resys.thena.api.entities.Tenant;
import io.resys.thena.datasource.ImmutableSqlTuple;
import io.resys.thena.datasource.ThenaSqlClient.Sql;
import io.resys.thena.datasource.ThenaSqlClient.SqlTuple;
import io.resys.thena.datasource.ThenaSqlClient.SqlTupleList;
import io.resys.thena.fs.entities.Commit;
import io.resys.thena.fs.entities.ImmutableCommit;
import io.resys.thena.fs.entities.ImmutableTree;
import io.resys.thena.fs.entities.Ref;
import io.resys.thena.fs.entities.Tree;
import io.smallrye.mutiny.tuples.Tuple2;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.Tuple;

@TenantSql.Table(
  name = "commit",
  order = 500,
  ddl = """
    CREATE TABLE {commit} (
      commit_id TEXT PRIMARY KEY,
      commit_created_at TIMESTAMPTZ NOT NULL,
      commit_author TEXT NOT NULL,
      commit_message TEXT NOT NULL,
      tree_id TEXT NOT NULL REFERENCES {tree}(tree_id),
      parent_id TEXT REFERENCES {commit}(commit_id),
      merge_id TEXT REFERENCES {commit}(commit_id)
    );
    
    CREATE INDEX {commit}_tree_idx ON {commit}(tree_id);
    CREATE INDEX {commit}_parent_idx ON {commit}(parent_id);
    CREATE INDEX {commit}_merge_idx ON {commit}(merge_id);
    CREATE INDEX {commit}_created_at_idx ON {commit}(commit_created_at);
    
    COMMENT ON TABLE {commit} IS 'Version control commits representing immutable snapshots of the filesystem state with metadata about the change.';
    COMMENT ON COLUMN {commit}.commit_id IS 'Unique commit identifier (hash)';
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

  @TenantSql.Find(
    optional = false,
    sql = """
    SELECT commit.*, __nodes_json
    FROM {commit} as commit
    JOIN {tree} as tree ON tree.tree_id = commit.tree_id
    WHERE commit.commit_id = $1
    """,
    rowMapper = CommitAndTreeMapper.class,
    sqlBuilder = COMMIT_AND_NODES_SQL.class
  )
  SqlTuple getByIdWithNodesAndBlobs(String id);
  
  @TenantSql.Find(
    optional = false,
    sql = """
    SELECT commit.*, __nodes_json
    FROM {commit} as commit ON commit.commit_id = ref.commit_id
    JOIN {tree} as tree ON tree.tree_id = commit.tree_id
    WHERE commit.commit_id = $1
    """,
    rowMapper = CommitAndTreeMapper.class,
    sqlBuilder = COMMIT_AND_TREE_SQL.class
  )
  SqlTuple getByIdWithNodes(String id);
  
  @TenantSql.Find(
      optional = true,
      sql = """
        SELECT 'commit' as found_by, commit.*, ref.*
        FROM {commit} as commit
        LEFT JOIN {ref} as ref ON commit.commit_id = ref.commit_id AND FALSE
        WHERE commit.commit_id = $1 
        
        UNION 
        
        SELECT 'ref' as found_by, commit.*, ref.*
        FROM {ref} as ref
        RIGHT JOIN {commit} as commit ON commit.commit_id = ref.commit_id
        WHERE ref.ref_id::text = $1 OR ref.ref_name = $1
      """,
      rowMapper = CommitOrRefMapper.class
    )
  SqlTuple findByCommitIdOrRef(String commitId);
  
  
  @TenantSql.FindAll(
      sql = "SELECT * FROM {commit} as commit",
      rowMapper = CommitMapper.class
    )
    Sql findAll();

  @TenantSql.FindAll(
    sql = "SELECT * FROM {commit} as commit WHERE commit.tree_id = $1",
    rowMapper = CommitMapper.class
  )
  SqlTuple findAllByTreeId(String treeId);

  @TenantSql.InsertAll(
    sql = """
      INSERT INTO {commit}
      (commit_id, commit_created_at, commit_author, commit_message, tree_id, parent_id, merge_id)
      VALUES($1, $2, $3, $4, $5, $6, $7)
    """,
    propsMapper = CommitInsertMapper.class
  )
  SqlTupleList insertMany(List<Commit> commits);

  @TenantSql.DeleteAll(
    sql = "DELETE FROM {commit} WHERE commit_id = $1",
    propsMapper = CommitDeleteMapper.class
  )
  SqlTupleList deleteAll(List<Commit> commits);


  class COMMIT_AND_TREE_SQL implements SqlBuilder<String> {
    @Override
    public SqlTuple apply(Tenant tenant, String baseline, String commitId) {
      return ImmutableSqlTuple.builder()
          .value(baseline.replace("__nodes_json", NodeTable.sql().includeBlobs(false).build()))
          .props(Tuple.of(commitId))
          .build();
    }
  }

  class COMMIT_AND_NODES_SQL implements SqlBuilder<String> {
    @Override
    public SqlTuple apply(Tenant tenant, String baseline, String commitId) {
      return ImmutableSqlTuple.builder()
          .value(baseline.replace("__nodes_json", NodeTable.sql().includeBlobs(true).build()))
          .props(Tuple.of(commitId))
          .build();
    }
  }

  class CommitOrRefMapper implements TenantSql.RowMapper<Tuple2<Commit, Optional<Ref>>> {
    @Override
    public Tuple2<Commit, Optional<Ref>> apply(Row row) {
      final var commit = CommitMapper.fromRow(row);
      final Ref ref;
      
      if(row.getString("found_by").equals("commit")) {
        ref = null;
      } else {
        ref = RefTable.RefMapper.fromRow(row);
      }
      return Tuple2.of(commit, Optional.ofNullable(ref));
    }
  }
  
  class CommitAndTreeMapper implements TenantSql.RowMapper<Tuple2<Commit, Tree>> {
    @Override
    public Tuple2<Commit, Tree> apply(Row row) {
      final var commit = CommitMapper.fromRow(row);
      final var allNodes = Optional
        .ofNullable(row.getJsonArray("nodes_json"))
        .orElseGet(() -> new JsonArray())
        .stream().map(node_json -> (JsonObject) node_json)
        .map(NodeTable.NodeMapper::fromJson)
        .toList();
      final var tree = ImmutableTree.builder()
          .id(row.getString("tree_id"))
          .treeNodes(allNodes)
          .build();
      return Tuple2.of(commit, tree);
    }
  }
  
  class CommitMapper implements TenantSql.RowMapper<Commit> {
    @Override
    public Commit apply(Row row) {
      return fromRow(row);
    }
    
    public static Commit fromRow(Row row) {
      final String parentId = row.getString("parent_id");
      final String mergeId = row.getString("merge_id");

      return ImmutableCommit.builder()
        .id(row.getString("commit_id"))
        .commitCreatedAt(row.getOffsetDateTime("commit_created_at"))
        .commitAuthor(row.getString("commit_author"))
        .commitMessage(row.getString("commit_message"))
        .treeId(row.getString("tree_id"))
        .parentId(Optional.ofNullable(parentId))
        .mergeId(Optional.ofNullable(mergeId))
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

  class CommitDeleteMapper implements TenantSql.PropsMapper<Commit> {
    @Override
    public io.vertx.mutiny.sqlclient.Tuple apply(Commit commit) {
      return io.vertx.mutiny.sqlclient.Tuple.from(new Object[]{
        commit.getId()
      });
    }
  }
}
