package io.resys.thena.fs.tables;

import java.time.OffsetDateTime;

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
import io.resys.thena.fs.entities.Commit;
import io.resys.thena.fs.entities.ImmutableBlob;
import io.resys.thena.fs.entities.ImmutableCommit;
import io.resys.thena.fs.entities.ImmutableNode;
import io.resys.thena.fs.entities.ImmutableNodeTransitives;
import io.resys.thena.fs.entities.ImmutableProps;
import io.resys.thena.fs.entities.ImmutableRef;
import io.resys.thena.fs.entities.ImmutableRefTransitives;
import io.resys.thena.fs.entities.Ref;
import io.resys.thena.fs.tables.filters.RefTableFilter;
import io.resys.thena.fs.tables.filters.RefTableLockFilter;
import io.resys.thena.support.TableUtils;
import io.smallrye.mutiny.tuples.Tuple2;
import io.vertx.core.json.JsonObject;
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
      
      commit_id TEXT NOT NULL REFERENCES {commit}(id)
    );
    
    CREATE INDEX {ref}_commit_idx ON {ref}(commit_id);
    CREATE INDEX {ref}_name_idx ON {ref}(ref_name);
    CREATE INDEX {ref}_desc_idx ON {ref}(ref_description);
    
    COMMENT ON TABLE {ref} IS 'Named references to commits, typically representing branches or bookmarks that can move to point to different commits over time.';
    COMMENT ON COLUMN {ref}.ref_name IS 'Reference name (e.g., "main", "develop", "feature/xyz")';
    COMMENT ON COLUMN {ref}.commit_id IS 'Current commit that this reference points to';
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
        ref.commit_id,
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
      SELECT ref.id, ref.ref_name, ref.ref_description, ref.ref_props, ref.ref_permissions, ref.ref_flags, ref.ref_author,
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
      SELECT ref.id, ref.ref_name, ref.ref_description, ref.ref_props, ref.ref_permissions, ref.ref_flags, ref.ref_author,
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
      (id, ref_name, commit_id)
      VALUES($1, $2, $3)
    """,
    propsMapper = RefInsertMapper.class
  )
  SqlTupleList insertMany(List<Ref> refs);

  @TenantSql.UpdateAll(
    sql = """
      UPDATE {ref}
      SET commit_id = $1
      WHERE id = $2
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
      SELECT ref.id, ref.ref_name, ref.ref_description, ref.ref_props, ref.ref_permissions, ref.ref_flags, ref.ref_author,
             ref.commit_id, commit.commit_created_at, commit.commit_author, commit.commit_message, commit.tree_id, commit.parent_id, commit.merge_id
      FROM {ref} as ref
      LEFT JOIN {commit} as commit ON ref.commit_id = commit.id
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
    ref.id, ref.ref_name, ref.ref_description, ref.ref_props, ref.ref_permissions, ref.ref_flags, ref.ref_author, ref.commit_id, 
    commits.commit_created_at, commits.commit_author, commits.commit_message, commits.tree_id, commits.parent_id, commits.merge_id,
    tree.id as tree_id,
    
    -- Aggregated Nodes: Hydrated only if object_id matches $2 (your filter.getDocIds())
    (SELECT json_agg(
        json_build_object(
            'id', n.id,
            'object_id', n.object_id,
            'node_path', n.node_path,
            'node_name', n.node_name,
            
            'created_at', idx.created_at,
            'updated_at', idx.updated_at,
            'created_by', idx.created_by,
            'updated_by', idx.updated_by,

                                    
            'blob_id', n.blob_id,
            'props_id', n.props_id,
            -- These will be NULL if the object_id isn't in the filter list
            
            'blob', CASE WHEN b.id IS NOT NULL 
              THEN json_build_object(
                'blob_type', b.blob_type, 
                'blob_value', b.blob_value
              ) 
              ELSE NULL END,
            
            'props', CASE WHEN p.id IS NOT NULL 
              THEN json_build_object(
                'props_labels', p.props_labels, 
                'props_flags', p.props_flags,
                'props_comments', p.props_comments,
                'props_permissions', p.props_permissions
              ) 
              ELSE NULL END
        )
    ) FROM unnest(tree.tree_nodes) AS n
      -- "Extended Query" logic inside the aggregator
      LEFT JOIN {props} p ON p.id = n.props_id AND n.object_id = ANY($2)
      LEFT JOIN {blob} b ON b.id = n.blob_id AND n.object_id = ANY($2)
      LEFT JOIN (
        SELECT object_index.object_id, object_index.created_by, object_index.updated_by,
               created_commit.commit_created_at as created_at,
               updated_commit.commit_created_at as updated_at
        FROM {object_index} as object_index
        LEFT JOIN {commit} as created_commit ON object_index.created_by = created_commit.id
        LEFT JOIN {commit} as updated_commit ON object_index.updated_by = updated_commit.id
      ) as idx ON idx.object_id = n.object_id
    ) as nodes_json

FROM (SELECT * FROM {ref} WHERE ref_name = $1 FOR UPDATE NOWAIT) as ref
JOIN {commit} as commits ON commits.id = ref.commit_id
JOIN {tree} as tree ON tree.id = commits.tree_id
    """,
    rowMapper = RefLockMapper.class,
    sqlBuilder = RefTableLockFilter.SQL.class
  )
  SqlTuple findOneWithLock(RefTableLockFilter filter);

  class RefLockMapper implements TenantSql.RowMapper<Ref> {
    @Override
    public Ref apply(Row row) {
      final var baseline = RefMapper.baseline(row);
      final var trs = ImmutableRefTransitives.builder().commit(baseline.getItem2());
      
      row.getJsonArray("nodes_json")
        .stream().map(node_json -> (JsonObject) node_json)
        .forEach(node_json -> {
          final var blobId = Optional.ofNullable(node_json.getString("blob_id"));
          final var propsId = Optional.ofNullable(node_json.getString("props_id"));
          final var nodeTrs = ImmutableNodeTransitives.builder()
              .createdAt(OffsetDateTime.parse(node_json.getString("created_at")))
              .updatedAt(OffsetDateTime.parse(node_json.getString("updated_at")));

          // optional when queried
          final var json_blob = node_json.getJsonObject("blob");
          if(json_blob != null) {
            final var blob = ImmutableBlob.builder()
                .blobType(json_blob.getString("blob_type"))
                .blobValue(json_blob.getJsonObject("blob_value"))
                .id(blobId.get())
                .build();
            
            nodeTrs.blob(blob);
            trs.putBlobsById(blob.getId(), blob);
          }
          
          final var json_props = node_json.getJsonObject("props");
          if(json_props != null) {
            final var props = ImmutableProps.builder()
              .id(propsId.get())
              .propsLabels(json_props.getJsonObject("props_labels"))
              .propsComments(json_props.getJsonObject("props_comments"))
              .propsPermissions(json_props.getJsonObject("props_permissions"))
              .propsFlags(json_props.getJsonObject("props_flags"))
              .build();
            nodeTrs.props(props);
            trs.putPropsById(props.getId(), props);
          }
          
          // main node
          final var node = ImmutableNode.builder()
            .id(node_json.getString("id"))
            .objectId(node_json.getString("object_id"))
            .nodePath(node_json.getString("node_path"))
            .nodeName(node_json.getString("node_name"))
            .blobId(blobId)
            .propsId(propsId)
            .transitives(nodeTrs.build())
            .build();
          trs.putNodesById(node.getId(), node);
          
        });
      
      return baseline.getItem1().transitives(trs.build()).build();
    }
  }


  class RefMapper implements TenantSql.RowMapper<Ref> {
    
    public static Tuple2<ImmutableRef.Builder, Commit> baseline(Row row) {
      final var refBuilder = ImmutableRef.builder()
          .id(TableUtils.toStringUUID(row, "id"))
          .refName(row.getString("ref_name"))
          .commitId(row.getString("commit_id"));
      
      // Add optional ref properties
      final String refDescription = row.getString("ref_description");
      if (refDescription != null) {
        refBuilder.branchDescription(refDescription);
      }
      
      final JsonObject refProps = row.getJsonObject("ref_props");
      if (refProps != null) {
        refBuilder.branchProps(refProps);
      }
      
      final JsonObject refPermissions = row.getJsonObject("ref_permissions");
      if (refPermissions != null) {
        refBuilder.branchPermissions(refPermissions);
      }
      
      final JsonObject refFlags = row.getJsonObject("ref_flags");
      if (refFlags != null) {
        refBuilder.branchFlags(refFlags);
      }
      
      final String refAuthor = row.getString("ref_author");
      if (refAuthor != null) {
        refBuilder.branchAuthor(refAuthor);
      }
      
      // Build commit object from joined data
      final String commitId = row.getString("commit_id");
      
    
      final var commitBuilder = ImmutableCommit.builder()
          .id(commitId)
          .commitCreatedAt(row.getOffsetDateTime("commit_created_at"))
          .commitAuthor(row.getString("commit_author"))
          .commitMessage(row.getString("commit_message"))
          .treeId(row.getString("tree_id"));
      
      final String parentId = row.getString("parent_id");
      if (parentId != null) {
        commitBuilder.parentId(parentId);
      }
      
      final String mergeId = row.getString("merge_id");
      if (mergeId != null) {
        commitBuilder.mergeId(mergeId);
      }
      
      final var commit = commitBuilder.build();
      return Tuple2.of(refBuilder, commit);
    }

    @Override
    public Ref apply(Row row) {
      final var baseline = baseline(row);
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
        ref.getCommitId()
      });
    }
  }

  class RefUpdateMapper implements TenantSql.PropsMapper<Ref> {
    @Override
    public io.vertx.mutiny.sqlclient.Tuple apply(Ref ref) {
      return io.vertx.mutiny.sqlclient.Tuple.from(new Object[]{
        ref.getCommitId(),
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
