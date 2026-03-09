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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.lang3.mutable.MutableInt;
import org.immutables.value.Value;

import io.resys.thena.api.annotations.TenantSql;
import io.resys.thena.api.annotations.TenantSql.SqlBuilder;
import io.resys.thena.api.annotations.TenantSql.WrapperType;
import io.resys.thena.api.entities.Tenant;
import io.resys.thena.datasource.ImmutableSqlTuple;
import io.resys.thena.datasource.ThenaSqlClient.Sql;
import io.resys.thena.datasource.ThenaSqlClient.SqlTuple;
import io.resys.thena.datasource.ThenaSqlClient.SqlTupleList;
import io.resys.thena.fs.entities.Commit;
import io.resys.thena.fs.entities.ImmutableCommit;
import io.resys.thena.fs.entities.ImmutableTree;
import io.resys.thena.fs.entities.Node;
import io.resys.thena.fs.entities.Ref;
import io.resys.thena.fs.entities.Tree;
import io.resys.thena.storesql.support.SqlStatement;
import io.resys.thena.support.TableUtils;
import io.smallrye.mutiny.tuples.Tuple2;
import io.smallrye.mutiny.tuples.Tuple3;
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

  
  record NodesAndBlobsFilter(String commitId, List<String> blobTypes) { }
  @TenantSql.Find(
    optional = false,
    sql = """
    SELECT 
        commit.*,
        cardinality(tree.tree_nodes) as commit_nodes_count, 
        nodes_and_blobs.tree_node_blob
    FROM {commit} as commit
    JOIN {tree} as tree ON tree.tree_id = commit.tree_id
    LEFT JOIN LATERAL (__nodes_json) as nodes_and_blobs ON TRUE
    WHERE commit.commit_id = $1
    """,
    rowMapper = CommitAndTreeMapper.class,
    sqlBuilder = COMMIT_AND_NODES_SQL.class
  )
  SqlTuple getByIdWithNodesAndBlobs(NodesAndBlobsFilter filter);
  
  
  record NodesFilter(String commitId, List<String> blobTypes) { }
  @TenantSql.Find(
    optional = false,
    sql = """
    SELECT 
        commit.*,
        cardinality(tree.tree_nodes) as commit_nodes_count,  
        nodes_and_blobs.tree_node_blob
    FROM {commit} as commit ON commit.commit_id = ref.commit_id
    JOIN {tree} as tree ON tree.tree_id = commit.tree_id
    LEFT JOIN LATERAL (__nodes_json) as nodes_and_blobs ON TRUE
    WHERE commit.commit_id = $1
    """,
    rowMapper = CommitAndTreeMapper.class,
    sqlBuilder = COMMIT_AND_TREE_SQL.class
  )
  SqlTuple getByIdWithNodes(NodesFilter filter);
  
  @TenantSql.FindAll(
    wrapper = WrapperType.MULTI,
    sql = """
    WITH RECURSIVE commit_ancestry AS (
      -- Start with branch heads
      SELECT branch.ref_name as branch_name, branch.ref_id as branch_id, branch.commit_id
      FROM {ref} as branch
  
      UNION ALL
  
      -- Follow parent commits
      SELECT ancestry.branch_name, ancestry.branch_id, commit.parent_id
      FROM commit_ancestry as ancestry
      JOIN {commit} as commit ON ancestry.commit_id = commit.commit_id
      WHERE commit.parent_id IS NOT NULL
    )
    SELECT DISTINCT 
      ancestry.branch_name, 
      ancestry.branch_id, 
      ref.*,
      commit.*,
      cardinality(tree.tree_nodes) as commit_nodes_count, 
      node.*
    FROM commit_ancestry as ancestry
    JOIN {commit} as commit ON ancestry.commit_id = commit.commit_id
    JOIN {tree} as tree ON tree.tree_id = commit.tree_id
    JOIN {ref} as ref ON ref.ref_id = ancestry.branch_id
    CROSS JOIN LATERAL unnest(tree.tree_nodes) AS node
    """,
    rowMapper = CommitHistoryMapper.class,
    sqlBuilder = COMMIT_HISTORY_FOR_NODES_SQL.class
  )
  SqlTuple findCommitHistoryByNodes(CommitHistoryFilter filter);
  
  @TenantSql.Find(
      optional = true,
      sql = """
        SELECT 
            'commit' as found_by, 
            commit.*,
            cardinality(tree.tree_nodes) as commit_nodes_count, 
            ref.*
        FROM {commit} as commit
        LEFT JOIN {ref} as ref ON commit.commit_id = ref.commit_id AND FALSE
        JOIN {tree} as tree ON tree.tree_id = commit.tree_id
        WHERE commit.commit_id = $1 
        
        UNION 
        
        SELECT 
            'ref' as found_by, 
            commit.*, 
            cardinality(tree.tree_nodes) as commit_nodes_count,
            ref.*
        FROM {ref} as ref
        RIGHT JOIN {commit} as commit ON commit.commit_id = ref.commit_id
        JOIN {tree} as tree ON tree.tree_id = commit.tree_id
        WHERE ref.ref_id::text = $1 OR ref.ref_name = $1
      """,
      rowMapper = CommitOrRefMapper.class
    )
  SqlTuple findByCommitIdOrRef(String commitId);
  
  
  @TenantSql.FindAll(
    sql = """
        SELECT *,
            cardinality(tree.tree_nodes) as commit_nodes_count
        FROM {commit} as commit
        JOIN {tree} as tree ON tree.tree_id = commit.tree_id
    """,
    rowMapper = CommitMapper.class
  )
  Sql findAll();

  @TenantSql.FindAll(
    sql = """
        SELECT *,
            cardinality(tree.tree_nodes) as commit_nodes_count
        FROM {commit} as commit WHERE commit.tree_id = $1
        JOIN {tree} as tree ON tree.tree_id = commit.tree_id
        """,
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

  

  @Value.Immutable
  interface CommitHistoryFilter {
    String getBranchName();
  }
  
  class COMMIT_HISTORY_FOR_NODES_SQL implements SqlBuilder<CommitHistoryFilter> {
    @Override
    public SqlTuple apply(Tenant tenant, String baseline, CommitHistoryFilter filter) {
      final var params = new ArrayList<Object>();
      final var index = new MutableInt(0);
      final var stmt = new SqlStatement();
      
      // branch name or id
      if (filter.getBranchName() != null) {
        stmt.append("(");
        
        try {
          final UUID uuid =  TableUtils.toUuid(filter.getBranchName());
          stmt.append("ancestry.branch_id = $").append(index.incrementAndGet());
          params.add(uuid);
        } catch(Exception e) {}
        
        stmt.append("ancestry.branch_name = $").append(index.incrementAndGet()).append(")");
        params.add(filter.getBranchName());
      }
      
      
      /* branch name or id
      if (filter.getFileOrFolderId() != null) {
        if (!params.isEmpty()) {
          stmt.append(" AND ");
        }
        stmt.append("node.object_id = $").append(index.incrementAndGet());
        params.add(filter.getFileOrFolderId());
      }
      
      
      if (filter.getNameExpr() != null) {
        if (!params.isEmpty()) {
          stmt.append(" AND ");
        }
        final var nameSql = new StringBuilder();
        final var nameBuilder = new NameExpressionBuilderImpl(
          "node.node_name",
          param -> {
            params.add(param);
            return index.incrementAndGet();
          },
          nameSql
        );
        
        filter.getNameExpr().accept(nameBuilder);
        nameBuilder.close();
        stmt.append(nameSql.toString());
      }
      */
      
      final var result = stmt.toString();
      final var clause = (result.isBlank() ? "" : " WHERE ") + result;
      
      return ImmutableSqlTuple.builder()
          .value(baseline + clause)
          .props(Tuple.from(params))
          .build();
    }
  }

  

  class COMMIT_AND_TREE_SQL implements SqlBuilder<NodesFilter> {
    @Override
    public SqlTuple apply(Tenant tenant, String baseline, NodesFilter filter) {
      final var params = new ArrayList<Object>();
      params.add(filter.commitId);
      final var index = new MutableInt(1);
      final var nodes_json = NodeTable.sql()
        .includeBlobs(false)
        .includeBlobTypes(filter.blobTypes)
        .build((prop) -> {
          params.add(prop);
          return index.incrementAndGet();
        });
    
      return ImmutableSqlTuple.builder()
          .value(baseline.replace("__nodes_json", nodes_json))
          .props(Tuple.from(params))
          .build();
    }
  }

  class COMMIT_AND_NODES_SQL implements SqlBuilder<NodesAndBlobsFilter> {
    @Override
    public SqlTuple apply(Tenant tenant, String baseline, NodesAndBlobsFilter filter) {
      final var params = new ArrayList<Object>();
      params.add(filter.commitId);
      final var index = new MutableInt(1);
      
      final var nodes_json = NodeTable.sql()
        .includeBlobs(true)
        .includeBlobTypes(filter.blobTypes)
        .build((prop) -> {
          params.add(prop);
          return index.incrementAndGet();
        });
      
      return ImmutableSqlTuple.builder()
          .value(baseline.replace("__nodes_json", nodes_json))
          .props(Tuple.from(params))
          .build();
    }
  }
  
  class CommitHistoryMapper implements TenantSql.RowMapper<Tuple3<Commit, Ref, Node>> {

    @Override
    public Tuple3<Commit, Ref, Node> apply(Row row) {
      final var node = NodeTable.NodeMapper.fromRow(row);
      final var ref = RefTable.RefMapper.fromRow(row);
      final var commit = CommitMapper.fromRow(row);
      return Tuple3.of(commit, ref, node);
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
  
  class CommitAndTreeMapper implements TenantSql.RowMapper<Tuple2<Commit, Optional<Tree>>> {
    @Override
    public Tuple2<Commit, Optional<Tree>> apply(Row row) {
      final var commit = CommitMapper.fromRow(row);
      
      final var tree_node_blob = row.getJsonArray("tree_node_blob");
      if(tree_node_blob == null) {
        return Tuple2.of(commit, Optional.empty());
      }
      
      final var allNodes = tree_node_blob
        .stream().map(node_json -> (JsonObject) node_json)
        .map(NodeTable.NodeMapper::fromJson)
        .toList();
      final var tree = ImmutableTree.builder()
          .id(row.getString("tree_id"))
          .treeNodes(allNodes)
          .build();
      return Tuple2.of(commit, Optional.of(tree));
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
        .commitNodesCount(row.getInteger("commit_nodes_count"))
        .id(row.getString("commit_id"))
        .commitCreatedAt(row.getOffsetDateTime("commit_created_at"))
        .commitAuthor(row.getString("commit_author"))
        .commitMessage(row.getString("commit_message"))
        .treeId(row.getString("tree_id"))
        .commitNodesCount(row.getInteger("commit_nodes_count"))
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
