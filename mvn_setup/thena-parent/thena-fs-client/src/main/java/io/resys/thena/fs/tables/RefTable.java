package io.resys.thena.fs.tables;

import java.util.ArrayList;

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
import java.util.Optional;

import io.resys.thena.api.annotations.TenantSql;
import io.resys.thena.api.annotations.TenantSql.WrapperType;
import io.resys.thena.datasource.ThenaSqlClient.Sql;
import io.resys.thena.datasource.ThenaSqlClient.SqlTuple;
import io.resys.thena.datasource.ThenaSqlClient.SqlTupleList;
import io.resys.thena.fs.entities.ImmutableRef;
import io.resys.thena.fs.entities.ImmutableRefTransitives;
import io.resys.thena.fs.entities.ImmutableTree;
import io.resys.thena.fs.entities.Ref;
import io.resys.thena.fs.tables.filters.RefTableFilter;
import io.resys.thena.fs.tables.filters.RefTableLockFilter;
import io.resys.thena.support.TableUtils;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.sqlclient.Row;

@TenantSql.Table(
  name = "ref",
  order = 600,
  ddl = """
    CREATE TABLE {ref} (
      ref_id UUID PRIMARY KEY,
      ref_name TEXT UNIQUE NOT NULL,
      ref_description TEXT,
      ref_props JSONB,
      ref_permissions JSONB,
      ref_flags JSONB,
      ref_author TEXT,
      ref_created_at TIMESTAMPTZ NOT NULL,
      ref_created_from UUID,

      commit_id TEXT NOT NULL REFERENCES {commit}(commit_id)
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
    sql = "SELECT * FROM {ref} as ref",
    rowMapper = RefMapper.class,
    wrapper = WrapperType.MULTI
  )
  Sql findAll();
  
  @TenantSql.InsertAll(
    sql = """
      INSERT INTO {ref}
      (ref_id, ref_name, commit_id, ref_description, ref_props, ref_permissions, ref_flags, ref_author, ref_created_at, ref_created_from)
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
      WHERE ref_id = $7
    """,
    propsMapper = RefUpdateMapper.class
  )
  SqlTupleList updateMany(List<Ref> refs);

  @TenantSql.DeleteAll(
    sql = "DELETE FROM {ref} WHERE ref_name = $1 OR ref_id::text = $1",
    propsMapper = RefDeleteMapper.class
  )
  SqlTupleList deleteAll(List<Ref> refs);

  
  @TenantSql.FindAll(
      sql = """
  SELECT 
    ref.*,
    
    commit.commit_created_at, 
    commit.commit_author, 
    commit.commit_message, 
    commit.tree_id, 
    commit.parent_id, 
    commit.merge_id,
    cardinality(tree.tree_nodes) as commit_nodes_count,
        
    nodes_and_blobs.tree_node_blob    

  FROM {ref} as ref
  JOIN {commit} as commit ON commit.commit_id = ref.commit_id
  JOIN {tree} as tree ON tree.tree_id = commit.tree_id
  LEFT JOIN LATERAL (__nodes_json) as nodes_and_blobs ON TRUE
      """,
      wrapper = WrapperType.MULTI,
      rowMapper = RefMapper.class,
      sqlBuilder = RefTableFilter.SQL.class
    )
    SqlTuple findAllByFilter(RefTableFilter filter);
  
  // -- Lock branch, get current commit tree
  @TenantSql.Find(
    sql = """
  SELECT 
    ref.*,

    commit.commit_created_at, 
    commit.commit_author, 
    commit.commit_message, 
    commit.tree_id, 
    commit.parent_id, 
    commit.merge_id,
    cardinality(tree.tree_nodes) as commit_nodes_count,

    nodes_and_blobs.tree_node_blob
    
  FROM (SELECT * FROM {ref} WHERE ref_name = $1 FOR UPDATE NOWAIT) as ref
  JOIN {commit} as commit ON commit.commit_id = ref.commit_id
  JOIN {tree} as tree ON tree.tree_id = commit.tree_id
  LEFT JOIN LATERAL (__nodes_json) as nodes_and_blobs ON TRUE
    """,
    rowMapper = RefMapper.class,
    sqlBuilder = RefTableLockFilter.SQL.class
  )
  SqlTuple findOneWithLock(RefTableLockFilter filter);


  class RefMapper implements TenantSql.RowMapper<Ref> {
    @Override
    public Ref apply(Row row) {
      return fromRow(row);
    }
    
    public static Ref fromRow(Row row) {
      final var refTransitives = ImmutableRefTransitives.builder();
      final var visited_blobs = new ArrayList<String>();
      final var visited_props = new ArrayList<String>();
      
      final var isTreeSelected = row.getColumnIndex("tree_node_blob") != -1;
      final var tree_node_blob = isTreeSelected ? row.getJsonArray("tree_node_blob") : null;
      
      final var allNodes = Optional
        .ofNullable(tree_node_blob)
        .orElseGet(() -> new JsonArray())
        .stream().map(node_json -> (JsonObject) node_json)
        .map(node_json -> {

          final var node = NodeTable.NodeMapper.fromJson(node_json);

          final var blob = node.getTransitives().getBlob();
          if(blob != null && !visited_blobs.contains(blob.getId())) {
            refTransitives.putBlobsById(blob.getId(), blob);            
            visited_blobs.add(blob.getId());
          }

          final var props = node.getTransitives().getProps();
          if(props != null && !visited_props.contains(props.getId())) {
            refTransitives.putPropsById(props.getId(), props);
            visited_props.add(props.getId());
          }
          return node;
        })
        .toList();
      
      final var tree = isTreeSelected && tree_node_blob != null ? ImmutableTree.builder().id(row.getString("tree_id")).treeNodes(allNodes).build() : null;
      
      final var isCommitEnabled = row.getColumnIndex("commit_created_at") != -1;
      final var commit = isCommitEnabled ? CommitTable.CommitMapper.fromRow(row) : null;
      
      
      return ImmutableRef.builder()
        .id(TableUtils.toStringUUID(row, "ref_id"))
        .commitId(row.getString("commit_id"))
        
        .refAuthor(Optional.ofNullable(row.getString("ref_author")))
        .refName(row.getString("ref_name"))
        .refCreatedAt(row.getOffsetDateTime("ref_created_at"))
        .refCreatedFrom(Optional.ofNullable(TableUtils.toStringUUID(row, "ref_created_from")))
        
        // Add optional ref properties
        .refDescription(Optional.ofNullable(row.getString("ref_description")))
        .refProps(Optional.ofNullable(row.getJsonObject("ref_props")))
        .refPermissions(Optional.ofNullable(row.getJsonObject("ref_permissions")))
        .refFlags(Optional.ofNullable(row.getJsonObject("ref_flags")))
      
        .transitives(refTransitives.commit(commit).tree(tree).build())
        .build(); 
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