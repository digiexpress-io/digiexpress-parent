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

import io.resys.thena.api.annotations.TenantSql;
import io.resys.thena.datasource.ThenaSqlClient.Sql;
import io.resys.thena.fs.entities.Tree;
import io.resys.thena.fs.jackson.NodesAndBlobStdDeserializer;
import io.vertx.mutiny.sqlclient.Row;


@TenantSql.Table(
  name = "tree_view",
  order = 800,
  ddl = """
    CREATE OR REPLACE FUNCTION {tree_view}(
      _tree_id UUID,
      _hydrate_all BOOLEAN,
      _hydrate_ids JSONB DEFAULT '[]'::jsonb,
      _hydrate_types JSONB DEFAULT '[]'::jsonb
    )
    RETURNS TABLE (
      tree_id UUID,
      tree_node_blob JSONB
    ) AS $$
    BEGIN
    RETURN QUERY
    SELECT 
      tree.tree_id,
      jsonb_agg(
        json_build_object(
          'node_id', nodes.node_id,
          'object_id', nodes.object_id,
          'node_path', nodes.node_path,
          'node_name', nodes.node_name,
          
          'node_full_name', CONCAT_WS('/', NULLIF(nodes.node_path, ''), nodes.node_name),
          
          'created_at',        idx.created_at,
          'updated_at',        idx.updated_at,
          'created_by_author', idx.created_by_author,
          'created_by',        idx.created_by,
          'updated_by_author', idx.updated_by_author,
          'updated_by',        idx.updated_by,

          'tree_id', tree.tree_id,
                                  
          'blob_id', nodes.blob_id,
          'blob_type', blobs.blob_type,
          'blob_class', blobs.blob_class,
          
          -- HYDRATE BLOB VALUE
          'blob_value', CASE 
            WHEN jsonb_array_length(_hydrate_ids) > 0 AND (
                _hydrate_ids @> jsonb_build_array(nodes.object_id)
                OR
                _hydrate_ids @> jsonb_build_array(CONCAT_WS('/', NULLIF(nodes.node_path, ''), nodes.node_name))
            ) THEN blobs.blob_value
            
            WHEN jsonb_array_length(_hydrate_types) > 0 AND (
                _hydrate_types @> jsonb_build_array(blobs.blob_type)
            ) THEN blobs.blob_value
             
            WHEN _hydrate_all THEN blobs.blob_value
            ELSE NULL 
          END,
        
          'props_id', nodes.props_id,
          'props_labels', props.props_labels, 
          'props_flags', props.props_flags,
          'props_comments', props.props_comments,
          'props_permissions', props.props_permissions
        )
      ) as tree_node_blob

    FROM {tree} as tree 
    CROSS JOIN LATERAL unnest(tree.tree_nodes) AS nodes
    
    -- Find the specific index row for this tree and the specific node metadata in the array
    LEFT JOIN LATERAL (
      SELECT 
        tree_ancestry.object_id,
        tree_ancestry.created_by,
        tree_ancestry.updated_by,
        updated_commit.commit_author as updated_by_author,
        created_commit.commit_author as created_by_author,
        
        created_commit.commit_created_at AS created_at,
        updated_commit.commit_created_at AS updated_at
  
      FROM {tree_index} AS tree_index
  
      CROSS JOIN LATERAL unnest(tree_index.tree_ancestry) AS tree_ancestry
      LEFT JOIN {commit} AS created_commit ON tree_ancestry.created_by = created_commit.commit_id
      LEFT JOIN {commit} AS updated_commit ON tree_ancestry.updated_by = updated_commit.commit_id 
        
      WHERE tree_index.tree_id = tree.tree_id AND tree_ancestry.object_id = nodes.object_id
    ) AS idx ON TRUE
  
    LEFT JOIN {props} as props 
      ON props.props_id = nodes.props_id 
      AND nodes.props_id IS NOT NULL
          
    LEFT JOIN {blob} as blobs 
      ON blobs.blob_id = nodes.blob_id
      AND nodes.blob_id IS NOT NULL

    WHERE (_tree_id IS NULL OR tree.tree_id = _tree_id)
    GROUP BY tree.tree_id;
    END;
    $$ LANGUAGE plpgsql STABLE;
  """,
  constraints = """
    
  """,
  drop = """
    DROP FUNCTION IF EXISTS {tree_view} CASCADE;
  """
)
public interface TreeView {
  @TenantSql.FindAll(
    sql = "SELECT tree_id, tree_node_blob::TEXT FROM {tree_view}(null::UUID,false, '[]'::jsonb, '[]'::jsonb) as tree_view",
    rowMapper = TreeViewMapper.class
    
  )
  Sql findAll();
  
  class TreeViewMapper implements TenantSql.RowMapper<Tree> {
    @Override
    public Tree apply(Row row) {
      final var tree_node_blob = NodesAndBlobStdDeserializer.deserialize(row.getString("tree_node_blob"));
      return tree_node_blob.toTreeBuilder().id(row.getUUID("tree_id")).build();
    }
  }

  
  public static class TreeViewException extends RuntimeException {
    private static final long serialVersionUID = -7466040066422391701L;

    public TreeViewException(String msg) {
      super(msg);
    }
  }
}
