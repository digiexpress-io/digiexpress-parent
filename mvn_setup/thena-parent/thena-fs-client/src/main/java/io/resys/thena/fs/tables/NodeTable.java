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

import java.util.Optional;

import io.resys.thena.api.annotations.TenantSql;
import io.resys.thena.fs.entities.Node;
import io.resys.thena.fs.entities.ImmutableNode;
import io.vertx.mutiny.sqlclient.Row;

@TenantSql.Table(
  name = "node",
  order = 100,
  ddl = """
    CREATE TYPE {node} AS (
      id TEXT,
      
      node_id TEXT, -- technical id of the object (user api generated)
      node_path TEXT,
      node_name TEXT,

      blob_id TEXT,
      props_id TEXT
    );
    
    COMMENT ON TYPE {node} IS 'File or directory entry within a version tree. Represents a single item in the filesystem hierarchy with optional references to content and metadata. Referential integrity for blob_id and props_id is enforced via triggers since PostgreSQL cannot validate foreign keys within composite types.';
  """,
  constraints = """
    CREATE OR REPLACE FUNCTION {tree}_validate_tree() 
    RETURNS TRIGGER AS $$
    DECLARE
        missing_count INTEGER;
    BEGIN
        -- Validate that id, node_path and node_name are not null
        SELECT count(*) INTO missing_count
        FROM unnest(NEW.tree_nodes) nodes
        WHERE nodes.id IS NULL OR nodes.node_path IS NULL OR nodes.node_name IS NULL;
  
        IF missing_count > 0 THEN
            RAISE EXCEPTION 'Node(code 001) validation failed: % nodes have null id, node_path or node_name', missing_count;
        END IF;
  
        -- Validate id uniqueness within the tree
        SELECT count(*) INTO missing_count
        FROM (
            SELECT nodes.id, count(*)
            FROM unnest(NEW.tree_nodes) nodes
            GROUP BY nodes.id
            HAVING count(*) > 1
        ) duplicates;
  
        IF missing_count > 0 THEN
            RAISE EXCEPTION 'Node(code 002) validation failed: % duplicate id values in tree', missing_count;
        END IF;
    
        -- Validate that node_id, node_path and node_name are not null
        SELECT count(*) INTO missing_count
        FROM unnest(NEW.tree_nodes) nodes
        WHERE nodes.node_id IS NULL OR nodes.node_path IS NULL OR nodes.node_name IS NULL;
  
        IF missing_count > 0 THEN
            RAISE EXCEPTION 'Node(code 003) validation failed: % nodes have null node_id, node_path or node_name', missing_count;
        END IF;
  
        -- Validate node_id uniqueness within the tree
        SELECT count(*) INTO missing_count
        FROM (
            SELECT nodes.node_id, count(*)
            FROM unnest(NEW.tree_nodes) nodes
            GROUP BY nodes.node_id
            HAVING count(*) > 1
        ) duplicates;
  
        IF missing_count > 0 THEN
            RAISE EXCEPTION 'Node(code 004) validation failed: % duplicate node_id values in tree', missing_count;
        END IF;
 
    
        -- Validate that both node_path and node_name are not null
        SELECT count(*) INTO missing_count
        FROM unnest(NEW.tree_nodes) nodes
        WHERE nodes.node_path IS NULL OR nodes.node_name IS NULL;

        IF missing_count > 0 THEN
            RAISE EXCEPTION 'Node(code 005) validation failed: % nodes have null node_path or node_name', missing_count;
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
        LEFT JOIN {blob} b ON b.id = nodes.blob_id
        WHERE nodes.blob_id IS NOT NULL AND b.id IS NULL;

        IF missing_count > 0 THEN
            RAISE EXCEPTION 'Node(code 007) validation failed: % blob_id references do not exist', missing_count;
        END IF;

        -- Validate props_id references
        SELECT count(*) INTO missing_count
        FROM unnest(NEW.tree_nodes) nodes
        LEFT JOIN {props} p ON p.id = nodes.props_id
        WHERE nodes.props_id IS NOT NULL AND p.id IS NULL;

        IF missing_count > 0 THEN
            RAISE EXCEPTION 'Node(code 008) validation failed: % props_id references do not exist', missing_count;
        END IF;

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

  class NodeMapper implements TenantSql.RowMapper<Node> {
    @Override
    public Node apply(Row row) {
      return ImmutableNode.builder()
          .id(row.getString("id"))
          .nodeId(row.getString("node_id"))
          .nodePath(row.getString("node_path"))
          .nodeName(row.getString("node_name"))
          .blobId(Optional.ofNullable(row.getString("blob_id")))
          .propsId(Optional.ofNullable(row.getString("props_id")))
          .build();
    }
  }
}
