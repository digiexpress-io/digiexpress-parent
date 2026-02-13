package io.resys.thena.fs.tables;

import java.util.List;

import io.resys.thena.api.annotations.TenantSql;
import io.resys.thena.datasource.ThenaSqlClient.Sql;
import io.resys.thena.datasource.ThenaSqlClient.SqlTuple;
import io.resys.thena.datasource.ThenaSqlClient.SqlTupleList;
import io.resys.thena.fs.entities.Commit;
import io.resys.thena.fs.tables.mappers.CommitAndTreeMapper;
import io.resys.thena.fs.tables.mappers.CommitMapper;
import io.resys.thena.fs.tables.mappers.CommitOrRefMapper;

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
      SELECT commit.*
      FROM {commit} as commit
    """,
    rowMapper = CommitMapper.class
  )
  Sql findAll();

  @TenantSql.Find(
    optional = false,
    sql = """
    SELECT 
      commits.id,
      commits.commit_created_at, commits.commit_author, 
      commits.commit_message, commits.tree_id, 
      commits.parent_id, commits.merge_id,
      tree.id as tree_id,
    
      -- Aggregated Nodes
      (SELECT json_agg(
        json_build_object(
          'id', nodes.id,
          'object_id', nodes.object_id,
          'node_path', nodes.node_path,
          'node_name', nodes.node_name,
          
          'created_at', idx.created_at,
          'updated_at', idx.updated_at,
          'created_by', idx.created_by,
          'updated_by', idx.updated_by,
                                  
          'blob_id', nodes.blob_id,
          'props_id', nodes.props_id,
          
          'blob', 
            CASE WHEN blobs.id IS NOT NULL 
            THEN json_build_object(
              'blob_type', blobs.blob_type, 
              'blob_value', blobs.blob_value
            ) 
            ELSE NULL END,
          'props', 
            CASE WHEN props.id IS NOT NULL 
            THEN json_build_object(
              'props_labels', props.props_labels, 
              'props_flags', props.props_flags,
              'props_comments', props.props_comments,
              'props_permissions', props.props_permissions
            ) 
            ELSE NULL END
        )
      ) 
      FROM unnest(tree.tree_nodes) AS nodes
        LEFT JOIN {props} as props ON props.id = nodes.props_id AND nodes.props_id IS NOT NULL
        LEFT JOIN {blob} as blobs ON blobs.id = nodes.blob_id AND nodes.blob_id IS NOT NULL
        LEFT JOIN (
          SELECT object_index.object_id, object_index.created_by, object_index.updated_by,
                 created_commit.commit_created_at as created_at,
                 updated_commit.commit_created_at as updated_at
          FROM {object_index} as object_index
          LEFT JOIN {commit} as created_commit ON object_index.created_by = created_commit.id
          LEFT JOIN {commit} as updated_commit ON object_index.updated_by = updated_commit.id
        ) as idx ON idx.object_id = nodes.object_id
      ) as nodes_json

    FROM {commit} as commits
    JOIN {tree} as tree ON tree.id = commits.tree_id
    WHERE commits.id = $1
    """,
    rowMapper = CommitAndTreeMapper.class
  )
  SqlTuple getByIdWithNodesAndBlobs(String id);
  
  @TenantSql.Find(
    optional = false,
    sql = """
    SELECT 
      commits.id,
      commits.commit_created_at, commits.commit_author, 
      commits.commit_message, commits.tree_id, 
      commits.parent_id, commits.merge_id,
      tree.id as tree_id,
    
      -- Aggregated Nodes
      (SELECT json_agg(
        json_build_object(
          'id', nodes.id,
          'object_id', nodes.object_id,
          'node_path', nodes.node_path,
          'node_name', nodes.node_name,
          
          'created_at', idx.created_at,
          'updated_at', idx.updated_at,
          'created_by', idx.created_by,
          'updated_by', idx.updated_by,
                                  
          'blob_id', nodes.blob_id,
          'props_id', nodes.props_id,
          
          'blob', null,
          'props', null
        )
      ) 
      FROM unnest(tree.tree_nodes) AS nodes
        LEFT JOIN (
          SELECT object_index.object_id, object_index.created_by, object_index.updated_by,
                 created_commit.commit_created_at as created_at,
                 updated_commit.commit_created_at as updated_at
          FROM {object_index} as object_index
          LEFT JOIN {commit} as created_commit ON object_index.created_by = created_commit.id
          LEFT JOIN {commit} as updated_commit ON object_index.updated_by = updated_commit.id
        ) as idx ON idx.object_id = nodes.object_id
      ) as nodes_json

    FROM {commit} as commits ON commits.id = ref.commit_id
    JOIN {tree} as tree ON tree.id = commits.tree_id
    """,
    rowMapper = CommitAndTreeMapper.class
  )
  SqlTuple getByIdWithNodes(String id);
  
  @TenantSql.Find(
      optional = true,
      sql = """
        SELECT 
            commit.*,
            'commit' as found_by, 
            null as ref_id,
            null as ref_name, 
            null as ref_description,
            null as ref_props,
            null as ref_permissions,
            null as ref_flags,
            null as ref_author
        FROM {commit} as commit
        WHERE commit.id = $1 
        
        UNION 
        
        SELECT 
            commit.*,
            'ref' as found_by,
            ref.id as ref_id,
            ref.ref_name, 
            ref.ref_description,
            ref.ref_props,
            ref.ref_permissions,
            ref.ref_flags,
            ref.ref_author            
        FROM {ref} as ref
        RIGHT JOIN {commit} as commit ON commit.id = ref.commit_id
        WHERE ref.id::text = $1 OR ref.ref_name = $1
      """,
      rowMapper = CommitOrRefMapper.class
    )
  SqlTuple findByCommitIdOrRef(String commitId);
  

  @TenantSql.FindAll(
    sql = """
      SELECT commit.*
      FROM {commit} as commit
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
