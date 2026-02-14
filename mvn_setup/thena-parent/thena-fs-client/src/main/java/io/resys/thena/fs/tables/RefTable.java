package io.resys.thena.fs.tables;

/*-
 * #%L
 * thena-fs-client
 * %%
 * Copyright (C) 2015 - 2026 Copyright 2022 ReSys OÜ
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.util.List;

import io.resys.thena.api.annotations.TenantSql;
import io.resys.thena.api.annotations.TenantSql.WrapperType;
import io.resys.thena.datasource.ThenaSqlClient.Sql;
import io.resys.thena.datasource.ThenaSqlClient.SqlTuple;
import io.resys.thena.datasource.ThenaSqlClient.SqlTupleList;
import io.resys.thena.fs.entities.ImmutableRefTransitives;
import io.resys.thena.fs.entities.Ref;
import io.resys.thena.fs.tables.filters.RefTableFilter;
import io.resys.thena.fs.tables.filters.RefTableLockFilter;
import io.resys.thena.fs.tables.mappers.RefSelectMapper;
import io.resys.thena.support.TableUtils;
import io.vertx.mutiny.sqlclient.Row;

@TenantSql.Table(
  name = "ref",
  order = 600,
  ddl = """
    CREATE TABLE {ref} (
      id UUID PRIMARY KEY,

      ref_name TEXT UNIQUE NOT NULL,
      ref_description TEXT,
      ref_props JSONB,
      ref_permissions JSONB,
      ref_flags JSONB,
      ref_author TEXT,
      ref_created_at TIMESTAMPTZ NOT NULL,
      ref_created_from UUID,

      commit_id TEXT NOT NULL REFERENCES {commit}(id)
    );
    
    CREATE INDEX {ref}_commit_idx ON {ref}(commit_id);
    CREATE INDEX {ref}_name_idx ON {ref}(ref_name);
    CREATE INDEX {ref}_desc_idx ON {ref}(ref_description);
    
    COMMENT ON TABLE {ref} IS 'Named references to commits, typically representing branches or bookmarks that can move to point to different commits over time.';
    COMMENT ON COLUMN {ref}.ref_name IS 'Reference name (e.g., "main", "develop", "feature/xyz")';
    COMMENT ON COLUMN {ref}.commit_id IS 'Current commit that this reference points to';
    
    COMMENT ON COLUMN {ref}.ref_description IS 'Optional detailed description of this branch';
    COMMENT ON COLUMN {ref}.ref_created_from IS 'Id of the {ref} from what this branch was created';
    COMMENT ON COLUMN {ref}.ref_created_at IS 'Timestamp when this branch was created, stored in UTC';
    COMMENT ON COLUMN {ref}.ref_author IS 'Author who created this branch';
    
    COMMENT ON COLUMN {ref}.ref_props IS 'User annotations in JSONB format';
    COMMENT ON COLUMN {ref}.ref_permissions IS 'Access control and permission settings in JSONB format';
    COMMENT ON COLUMN {ref}.ref_flags IS 'Boolean flags like hidden, disabled, etc. in JSONB format';
  """,
  constraints = "",
  drop = """
    DROP TABLE IF EXISTS {ref} CASCADE;
  """
)
public interface RefTable {

  @TenantSql.FindAll(
    sql = """
      SELECT 
        ref.id,
        ref.ref_name, 
        ref.ref_description,
        ref.ref_props,
        ref.ref_permissions,
        ref.ref_flags,
        ref.ref_author,
        ref.ref_created_at,
        ref.commit_id,
        ref.ref_created_from,
        
        commit.commit_created_at, 
        commit.commit_author, 
        commit.commit_message,
        commit.tree_id,
        commit.parent_id,
        commit.merge_id
        
      FROM {ref} as ref
      LEFT JOIN {commit} as commit ON ref.commit_id = commit.id
    """,
    rowMapper = RefMapper.class,
    wrapper = WrapperType.MULTI
  )
  Sql findAll();

  @TenantSql.Find(
    optional = false,
    sql = """
      SELECT ref.id, ref.ref_name, ref.ref_description, ref.ref_props, ref.ref_permissions, ref.ref_flags, ref.ref_author, ref.ref_created_at, ref.ref_created_from,
             ref.commit_id, commit.commit_created_at, commit.commit_author, commit.commit_message, commit.tree_id, commit.parent_id, commit.merge_id
      FROM {ref} as ref
      LEFT JOIN {commit} as commit ON ref.commit_id = commit.id
      WHERE ref.ref_name = $1
    """,
    rowMapper = RefMapper.class
  )
  SqlTuple getByName(String refName);

  @TenantSql.FindAll(
    sql = """
      SELECT ref.id, ref.ref_name, ref.ref_description, ref.ref_props, ref.ref_permissions, ref.ref_flags, ref.ref_author, ref.ref_created_at, ref.ref_created_from,
             ref.commit_id, commit.commit_created_at, commit.commit_author, commit.commit_message, commit.tree_id, commit.parent_id, commit.merge_id
      FROM {ref} as ref
      LEFT JOIN {commit} as commit ON ref.commit_id = commit.id
      WHERE ref.commit_id = $1
    """,
    wrapper = WrapperType.MULTI,
    rowMapper = RefMapper.class
  )
  SqlTuple findAllByCommitId(String commitId);

  
  @TenantSql.InsertAll(
    sql = """
      INSERT INTO {ref}
      (id, ref_name, commit_id, ref_description, ref_props, ref_permissions, ref_flags, ref_author, ref_created_at, ref_created_from)
      VALUES($1, $2, $3, $4, $5, $6, $7, $8, $9, $10)
    """,
    propsMapper = RefInsertMapper.class
  )
  SqlTupleList insertMany(List<Ref> refs);

  @TenantSql.UpdateAll(
    sql = """
      UPDATE {ref}
      SET 
        commit_id = $1,
        ref_name = $2,
        ref_description = $3, 
        ref_props = $4, 
        ref_permissions = $5, 
        ref_flags = $6
      WHERE id = $7
    """,
    propsMapper = RefUpdateMapper.class
  )
  SqlTupleList updateMany(List<Ref> refs);

  @TenantSql.DeleteAll(
    sql = "DELETE FROM {ref} WHERE ref_name = $1",
    propsMapper = RefDeleteMapper.class
  )
  SqlTupleList deleteAll(List<Ref> refs);

  
  @TenantSql.FindAll(
      sql = """
  SELECT 
    ref.id, ref.ref_name, ref.ref_description, ref.ref_props, ref.ref_permissions, ref.ref_flags, ref.ref_author, ref.commit_id, ref.ref_created_at, ref.ref_created_from,
    commits.commit_created_at, commits.commit_author, commits.commit_message, commits.tree_id, commits.parent_id, commits.merge_id,
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
    ) FROM unnest(tree.tree_nodes) AS nodes
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

  FROM {ref} as ref
  JOIN {commit} as commits ON commits.id = ref.commit_id
  JOIN {tree} as tree ON tree.id = commits.tree_id
      """,
      wrapper = WrapperType.MULTI,
      rowMapper = RefFilterMapper.class,
      sqlBuilder = RefTableFilter.SQL.class
    )
    SqlTuple findAllByFilter(RefTableFilter filter);
  
  
  // -- Lock branch, get current commit tree
  @TenantSql.Find(
    sql = """
  SELECT 
    ref.id, ref.ref_name, ref.ref_description, ref.ref_props, ref.ref_permissions, ref.ref_flags, ref.ref_author, ref.commit_id, ref.ref_created_at, ref.ref_created_from,
    commits.commit_created_at, commits.commit_author, commits.commit_message, commits.tree_id, commits.parent_id, commits.merge_id,
    tree.id as tree_id,
    
    -- Aggregated Nodes: Hydrated only if object_id matches $2 (your filter.getDocIds())
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
        
        -- These will be NULL if the object_id isn't in the filter list
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
    ) FROM unnest(tree.tree_nodes) AS nodes
      -- "Extended Query" logic inside the aggregator
      
      LEFT JOIN {props} as props 
        ON props.id = nodes.props_id 
        AND nodes.props_id IS NOT NULL
        AND (nodes.object_id = ANY($2) OR CONCAT_WS('/', NULLIF(nodes.node_path, ''), nodes.node_name) = ANY($2) )
            
      LEFT JOIN {blob} as blobs 
        ON blobs.id = nodes.blob_id
        AND nodes.blob_id IS NOT NULL   
        AND (nodes.object_id = ANY($2) OR CONCAT_WS('/', NULLIF(nodes.node_path, ''), nodes.node_name) = ANY($2) )
      
      LEFT JOIN (
        SELECT object_index.object_id, object_index.created_by, object_index.updated_by,
               created_commit.commit_created_at as created_at,
               updated_commit.commit_created_at as updated_at
        FROM {object_index} as object_index
        LEFT JOIN {commit} as created_commit ON object_index.created_by = created_commit.id
        LEFT JOIN {commit} as updated_commit ON object_index.updated_by = updated_commit.id
      ) as idx ON idx.object_id = nodes.object_id
    ) as nodes_json

  FROM (SELECT * FROM {ref} WHERE ref_name = $1 FOR UPDATE NOWAIT) as ref
  JOIN {commit} as commits ON commits.id = ref.commit_id
  JOIN {tree} as tree ON tree.id = commits.tree_id
    """,
    rowMapper = RefLockMapper.class,
    sqlBuilder = RefTableLockFilter.SQL.class
  )
  SqlTuple findOneWithLock(RefTableLockFilter filter);

  
  class RefFilterMapper implements TenantSql.RowMapper<Ref> {
    @Override
    public Ref apply(Row row) {
      return RefSelectMapper.refAndCommitAndPropsAndBlob(row);
    }
  }
  
  class RefLockMapper implements TenantSql.RowMapper<Ref> {
    @Override
    public Ref apply(Row row) {
      return RefSelectMapper.refAndCommitAndPropsAndBlob(row);
    }
  }


  class RefMapper implements TenantSql.RowMapper<Ref> {
    @Override
    public Ref apply(Row row) {
      final var baseline = RefSelectMapper.refAndCommit(row);
      final var trs = ImmutableRefTransitives.builder().commit(baseline.getItem2()).build();
      return baseline.getItem1().transitives(trs).build();
    }
  }

  class RefInsertMapper implements TenantSql.PropsMapper<Ref> {
    @Override
    public io.vertx.mutiny.sqlclient.Tuple apply(Ref ref) {
      return io.vertx.mutiny.sqlclient.Tuple.from(new Object[]{
        TableUtils.toUuid(ref.getId()),
        ref.getRefName(),
        ref.getCommitId(),
        
        ref.getRefDescription().orElse(null),
        ref.getRefProps().orElse(null),
        ref.getRefPermissions().orElse(null),
        ref.getRefFlags().orElse(null),
        
        ref.getRefAuthor().orElse(null),
        ref.getRefCreatedAt(),
        ref.getRefCreatedFrom().orElse(null),
      });
    }
  }

  class RefUpdateMapper implements TenantSql.PropsMapper<Ref> {
    @Override
    public io.vertx.mutiny.sqlclient.Tuple apply(Ref ref) {
      return io.vertx.mutiny.sqlclient.Tuple.from(new Object[]{
        ref.getCommitId(),
        ref.getRefName(),
        
        ref.getRefDescription().orElse(null),
        ref.getRefProps().orElse(null),
        ref.getRefPermissions().orElse(null),
        ref.getRefFlags().orElse(null),
        
        TableUtils.toUuid(ref.getId())
      });
    }
  }

  class RefDeleteMapper implements TenantSql.PropsMapper<Ref> {
    @Override
    public io.vertx.mutiny.sqlclient.Tuple apply(Ref ref) {
      return io.vertx.mutiny.sqlclient.Tuple.from(new Object[]{
          TableUtils.toUuid(ref.getId())
      });
    }
  }
}
