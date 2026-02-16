package io.resys.thena.fs.tables;

import java.util.List;

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

import java.util.Optional;
import java.util.function.Function;

import io.resys.thena.api.annotations.TenantSql;
import io.resys.thena.fs.entities.Blob;
import io.resys.thena.fs.entities.ImmutableNode;
import io.resys.thena.fs.entities.ImmutableNodeTransitives;
import io.resys.thena.fs.entities.Node;
import io.resys.thena.fs.entities.Props;
import io.resys.thena.fs.tables.BlobTable.BlobMapper;
import io.resys.thena.fs.tables.ObjectIndexTable.ObjectIndexMapper;
import io.resys.thena.fs.tables.PropsTable.PropsMapper;
import io.resys.thena.support.RepoAssert;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.sqlclient.Row;

@TenantSql.Table(
  name = "node",
  order = 100,
  ddl = """
    CREATE DOMAIN {node}_required_text AS TEXT NOT NULL;
    
    CREATE TYPE {node} AS (
      node_id {node}_required_text,
      
      object_id {node}_required_text, -- technical id of the object (user api generated)
      node_path TEXT,
      node_name {node}_required_text,

      blob_id TEXT,
      props_id TEXT
    );
    
    CREATE DOMAIN {node}_strict AS {node}
    CHECK (
        -- num_nonnulls returns 1 if exactly one of the fields is NOT NULL
        num_nonnulls((VALUE).blob_id, (VALUE).props_id) = 1
    );
    
    COMMENT ON TYPE {node} IS 'File or directory entry within a version tree. Represents a single item in the filesystem hierarchy with optional references to content and metadata. Referential integrity for blob_id and props_id is enforced via triggers since PostgreSQL cannot validate foreign keys within composite types.';
  """,
  constraints = """

    CREATE OR REPLACE FUNCTION {tree}_validate_tree() 
    RETURNS TRIGGER AS $$
    DECLARE
        missing_count INTEGER;
    BEGIN
  
        -- Validate id uniqueness within the tree
        SELECT count(*) INTO missing_count
        FROM (
            SELECT nodes.node_id, count(*)
            FROM unnest(NEW.tree_nodes) nodes
            GROUP BY nodes.node_id
            HAVING count(*) > 1
        ) duplicates;
  
        IF missing_count > 0 THEN
            RAISE EXCEPTION 'Node(code 002) validation failed: % duplicate id values in tree', missing_count;
        END IF;
    
        -- Validate that object_id, node_path and node_name are not null
        SELECT count(*) INTO missing_count
        FROM unnest(NEW.tree_nodes) nodes
        WHERE nodes.object_id IS NULL OR nodes.node_name IS NULL;
  
        IF missing_count > 0 THEN
            RAISE EXCEPTION 'Node(code 003) validation failed: % nodes have null object_id or node_name', missing_count;
        END IF;
  
        -- Validate object_id uniqueness within the tree
        SELECT count(*) INTO missing_count
        FROM (
            SELECT nodes.object_id, count(*)
            FROM unnest(NEW.tree_nodes) nodes
            GROUP BY nodes.object_id
            HAVING count(*) > 1
        ) duplicates;
  
        IF missing_count > 0 THEN
            RAISE EXCEPTION 'Node(code 004) validation failed: % duplicate object_id values in tree', missing_count;
        END IF;
 
        -- Validate path + name uniqueness within the tree
        SELECT count(*) INTO missing_count
        FROM (
            SELECT nodes.node_path, nodes.node_name, count(*)
            FROM unnest(NEW.tree_nodes) nodes
            GROUP BY nodes.node_path, nodes.node_name
            HAVING count(*) > 1
        ) duplicates;

        IF missing_count > 0 THEN
            RAISE EXCEPTION 'Node(code 006) validation failed: % duplicate path+name combinations in tree', missing_count;
        END IF;

        -- Validate blob_id references
        SELECT count(*) INTO missing_count
        FROM unnest(NEW.tree_nodes) nodes
        LEFT JOIN {blob} b ON b.blob_id = nodes.blob_id
        WHERE nodes.blob_id IS NOT NULL AND b.blob_id IS NULL;

        IF missing_count > 0 THEN
            RAISE EXCEPTION 'Node(code 007) validation failed: % blob_id references do not exist', missing_count;
        END IF;

        -- Validate props_id references
        SELECT count(*) INTO missing_count
        FROM unnest(NEW.tree_nodes) nodes
        LEFT JOIN {props} p ON p.props_id = nodes.props_id
        WHERE nodes.props_id IS NOT NULL AND p.props_id IS NULL;

        IF missing_count > 0 THEN
            RAISE EXCEPTION 'Node(code 008) validation failed: % props_id references do not exist', missing_count;
        END IF;
        
        
        -- Validate object index
        -- SELECT count(*) INTO missing_count
        -- FROM unnest(NEW.tree_nodes) nodes
        -- RIGHT JOIN {object_index} p ON p.object_id = nodes.object_id;

        -- IF missing_count <> 1 THEN
        --     RAISE EXCEPTION 'Node(code 009) validation failed: % object_id references do not exist', missing_count;
        -- END IF;

        RETURN NEW;
    END;
    $$ LANGUAGE plpgsql;
  """,
  drop = """
    DROP FUNCTION IF EXISTS {tree}_validate_tree() CASCADE;
    DROP TYPE IF EXISTS {node} CASCADE;
  """
)
public interface NodeTable {


  public static Builder sql() {
    return new Builder();
  }

  public static class Builder {
    private Function<Object, Integer> sqlProps;
    private boolean includeBlobs = false;
    private Optional<List<String>> objectId = Optional.empty();

    public Builder includeBlobs(boolean includeBlobs) {
      this.includeBlobs = includeBlobs;
      return this;
    }
    
    public Builder objectId(List<String> objectIdOrFullPath) {
      this.objectId = Optional.ofNullable(objectIdOrFullPath);
      return this;
    }
    
    /**
     * @return baseline sql with placeholders
     */
    private String baseline() {
      return 
"""
SELECT json_agg(
  json_build_object(
    'node_id', nodes.node_id,
    'object_id', nodes.object_id,
    'node_path', nodes.node_path,
    'node_name', nodes.node_name,
    
    'created_at', idx.created_at,
    'updated_at', idx.updated_at,
    'created_by', idx.created_by,
    'updated_by', idx.updated_by,
                            
    'blob_id', nodes.blob_id,
    'blob_type', blobs.blob_type,
    'blob_class', blobs.blob_class,
    'blob_value', {hydrateBlobValue},

    'props_id', nodes.props_id,
    'props_labels', props.props_labels, 
    'props_flags', props.props_flags,
    'props_comments', props.props_comments,
    'props_permissions', props.props_permissions
  )
) 

FROM unnest(tree.tree_nodes) AS nodes
LEFT JOIN (
  SELECT object_index.object_id, object_index.created_by, object_index.updated_by,
         created_commit.commit_created_at as created_at,
         updated_commit.commit_created_at as updated_at
  FROM {object_index} as object_index
  LEFT JOIN {commit} as created_commit ON object_index.created_by = created_commit.commit_id
  LEFT JOIN {commit} as updated_commit ON object_index.updated_by = updated_commit.commit_id
) as idx ON idx.object_id = nodes.object_id

LEFT JOIN {props} as props 
  ON props.props_id = nodes.props_id 
  AND nodes.props_id IS NOT NULL
      
LEFT JOIN {blob} as blobs 
  ON blobs.blob_id = nodes.blob_id
  AND nodes.blob_id IS NOT NULL
""";
    }

    /**
     * @return extra constraint for join to select only asked blobs and props
     */
    private String hydrateBlobValue() {
      if(this.objectId.isPresent()) {
        final var object_id_arr = "$" + this.sqlProps.apply(objectId.map(e -> e.toArray(new String[] {})).get());
        return 
"""
CASE 
        WHEN (
          nodes.object_id = ANY({object_id_arr}) 
          OR CONCAT_WS('/', NULLIF(nodes.node_path, ''), nodes.node_name) = ANY({object_id_arr})
        ) THEN blobs.blob_value 
        ELSE NULL 
      END
""".replace("{object_id_arr}", object_id_arr);
      } else if(this.includeBlobs) {
        return "blobs.blob_value";
      } else {
        return "NULL";        
      }
    }
    
    public String build() {
      return build((_ignore) -> { return 0; });
    }

    public String build(Function<Object, Integer> sqlProps) {
      RepoAssert.notNull(sqlProps, () -> "sqlProps must be defined!");
      this.sqlProps = sqlProps;
      
      final var baseline = baseline();
      final var hydrateBlobValue = hydrateBlobValue();
      final var statement = baseline.replace("{hydrateBlobValue}", hydrateBlobValue);
      final var sql = "({statement}) as nodes_json".replace("{statement}", statement);
      return sql;
    }
  }
  
  class NodeMapper implements TenantSql.RowMapper<Node> {
    @Override
    public Node apply(Row row) {
      return ImmutableNode.builder()
          .id(row.getString("node_id"))
          .objectId(row.getString("object_id"))
          .nodePath(row.getString("node_path"))
          .nodeName(row.getString("node_name"))
          .blobId(Optional.ofNullable(row.getString("blob_id")))
          .propsId(Optional.ofNullable(row.getString("props_id")))
          .build();
    }
    
    public static Node fromJson(JsonObject node_json) {
      final var index = ObjectIndexMapper.fromJson(node_json);
      final var blobId = Optional.ofNullable(node_json.getString("blob_id"));
      final var propsId = Optional.ofNullable(node_json.getString("props_id"));
      
      final Optional<Blob> blob = blobId.map(ignore -> node_json).map(BlobMapper::fromJson);
      final Optional<Props> props = propsId.map(ignore -> node_json).map(PropsMapper::fromJson);
      
      return ImmutableNode.builder()
          .id(node_json.getString("node_id"))
          .objectId(index.getObjectId())
          .nodePath(Optional.ofNullable(node_json.getString("node_path")))
          .nodeName(node_json.getString("node_name"))
          .blobId(blobId)
          .propsId(propsId)
          .transitives(ImmutableNodeTransitives.builder()
              .objectIndex(index)
              .blob(blob.orElse(null))
              .props(props.orElse(null))
              .build())
          .build();
    } 
  }
}
