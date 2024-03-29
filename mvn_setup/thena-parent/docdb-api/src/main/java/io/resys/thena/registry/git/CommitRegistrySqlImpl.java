package io.resys.thena.registry.git;

import java.time.LocalDateTime;
import java.util.ArrayList;

/*-
 * #%L
 * thena-docdb-pgsql
 * %%
 * Copyright (C) 2021 Copyright 2021 ReSys OÜ
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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import io.resys.thena.api.entities.git.Commit;
import io.resys.thena.api.entities.git.CommitTree;
import io.resys.thena.api.entities.git.ImmutableBlob;
import io.resys.thena.api.entities.git.ImmutableCommit;
import io.resys.thena.api.entities.git.ImmutableCommitTree;
import io.resys.thena.api.entities.git.ImmutableTreeValue;
import io.resys.thena.api.registry.git.CommitRegistry;
import io.resys.thena.datasource.ImmutableSql;
import io.resys.thena.datasource.ImmutableSqlTuple;
import io.resys.thena.datasource.SqlQueryBuilder.Sql;
import io.resys.thena.datasource.SqlQueryBuilder.SqlTuple;
import io.resys.thena.datasource.TenantTableNames;
import io.resys.thena.storesql.support.SqlStatement;
import io.resys.thena.structures.git.GitQueries.LockCriteria;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.Tuple;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CommitRegistrySqlImpl implements CommitRegistry {
  private final TenantTableNames options;
 
  @Override
  public Sql findAll() {
    return ImmutableSql.builder()
        .value(new SqlStatement()
        .append("SELECT * FROM ").append(options.getCommits())
        .build())
        .build();
  }
  @Override
  public SqlTuple getById(String id) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT * FROM ").append(options.getCommits())
        .append(" WHERE id = $1")
        .append(" FETCH FIRST ROW ONLY")
        .build())
        .props(Tuple.of(id))
        .build();
  }
  @Override
  public SqlTuple insertOne(Commit commit) {
    
    var message = commit.getMessage();
    if(commit.getMessage().length() > 100) {
      message = message.substring(0, 100);
    }
    
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("INSERT INTO ").append(options.getCommits())
        .append(" (id, datetime, author, message, tree, parent, merge) VALUES($1, $2, $3, $4, $5, $6, $7)")
        .build())
        .props(Tuple.from(Arrays.asList(
            commit.getId(), commit.getDateTime().toString(), commit.getAuthor(), message, 
            commit.getTree(), commit.getParent().orElse(null), commit.getMerge().orElse(null))))
        .build();
  }
  @Override
  public SqlTuple getLock(LockCriteria crit) {
    final var commitId = crit.getCommitId(); 
    final var headName = crit.getHeadName();

    if(crit.getTreeValueIds().isEmpty()) {

      final var where = new StringBuilder();
      final List<Object> props = new ArrayList<>();
      
      props.add(headName);
      if(commitId != null) {
        where.append(" WHERE commits.id = $2 ");
        props.add(commitId);
      }
      return ImmutableSqlTuple.builder()
          .value(new SqlStatement()
          .append("SELECT ")
          .append("  refs.name as ref_name,").ln()
          .append("  commits.author as author,").ln()
          .append("  commits.datetime as datetime,").ln()
          .append("  commits.message as message,").ln()
          .append("  commits.merge as merge,").ln()
          .append("  commits.parent as commit_parent,").ln()
          .append("  commits.id as commit_id,").ln()
          .append("  commits.tree as tree_id,").ln()
          .append("  treeValues.name as blob_name,").ln()
          .append("  treeValues.blob as blob_id").ln()
          .append(" FROM (SELECT * FROM ").append(options.getRefs()).append(" WHERE name = $1 FOR UPDATE NOWAIT) as refs").ln()
          .append("  JOIN ").append(options.getCommits()).append(" as commits ON(commits.id = refs.commit)").ln()
          .append("  LEFT JOIN ").append(options.getTreeItems()).append(" as treeValues ON(treeValues.tree = commits.tree)").ln()
          .append(where.toString())
          .build())
          .props(Tuple.from(props))
          .build();  
    }
    

    final var props = new ArrayList<Object>();
    props.add(headName);
    props.add(crit.getTreeValueIds().toArray(new String[]{}));
    final var where = new StringBuilder("treeValues.name = ANY($2)");
    
    if(commitId != null) {
      where.append(" AND commits.id = $3");
      props.add(commitId);
    }
    
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT ")
        .append("  refs.name as ref_name,").ln()
        .append("  blobs.value as blob_value,").ln()
        .append("  treeValues.name as blob_name,").ln()
        .append("  treeValues.blob as blob_id,").ln()
        .append("  treeValues.tree as tree_id,").ln()
        .append("  commits.author as author,").ln()
        .append("  commits.datetime as datetime,").ln()
        .append("  commits.message as message,").ln()
        .append("  commits.merge as merge,").ln()
        .append("  commits.parent as commit_parent,").ln()
        .append("  commits.id as commit_id").ln()
        .append(" FROM (SELECT * FROM ").append(options.getRefs()).append(" WHERE name = $1 FOR UPDATE NOWAIT) as refs").ln()
        .append("  JOIN ").append(options.getCommits()).append(" as commits ON(commits.id = refs.commit)").ln()
        .append("  LEFT JOIN ").append(options.getTreeItems()).append(" as treeValues ON(treeValues.tree = commits.tree)").ln()
        .append("  LEFT JOIN ").append(options.getBlobs()).append(" as blobs ON(blobs.id = treeValues.blob)").ln()
        .append(" WHERE ").append(where.toString())
        .build())
        .props(Tuple.from(props))
        .build();
  }
  @Override
  public Function<Row, Commit> defaultMapper() {
    return CommitRegistrySqlImpl::commit;
  }
  
  @Override
  public Function<Row, CommitTree> commitTreeMapper() {
    return CommitRegistrySqlImpl::commitTree;
  }
  @Override
  public Function<Row, CommitTree> commitTreeWithBlobsMapper() {
    return CommitRegistrySqlImpl::commitTreeWithBlobs;
  }  

  private static Commit commit(Row row) {
    return ImmutableCommit.builder()
        .id(row.getString("id"))
        .author(row.getString("author"))
        .dateTime(LocalDateTime.parse(row.getString("datetime")))
        .message(row.getString("message"))
        .parent(Optional.ofNullable(row.getString("parent")))
        .merge(Optional.ofNullable(row.getString("merge")))
        .tree(row.getString("tree"))
        .build();
  }

  
  private static CommitTree commitTree(Row row) {
    return commitTreeInternal(row)
        .blob(Optional.empty())
        .build();
  }

  private static CommitTree commitTreeWithBlobs(Row row) {
    return commitTreeInternal(row)
        .blob(ImmutableBlob.builder()
            .id(row.getString("blob_id"))
            .value(jsonObject(row, "blob_value"))
            .build())
        .build();
  }
  private static JsonObject jsonObject(Row row, String columnName) {
    // string based - new JsonObject(row.getString(columnName));
    return row.getJsonObject(columnName);
  }
  private static ImmutableCommitTree.Builder commitTreeInternal(Row row) {
    final var blob = row.getString("blob_id");
    final var blobName = row.getString("blob_name");
    return ImmutableCommitTree.builder()
        .treeId(row.getString("tree_id"))
        .commitId(row.getString("commit_id"))
        .commitParent(row.getString("commit_parent"))
        .commitAuthor(row.getString("author"))
        .commitDateTime(LocalDateTime.parse(row.getString("datetime")))
        .commitMessage(row.getString("message"))
        .commitMerge(row.getString("merge"))
        .branchName(row.getString("ref_name"))
        .treeValue(blob == null ? Optional.empty() : Optional.of(ImmutableTreeValue.builder()
            .blob(blob)
            .name(blobName)
            .build()));
  }
  
  @Override
  public Sql createTable() {
    return ImmutableSql.builder().value(new SqlStatement().ln()
    .append("CREATE TABLE ").append(options.getCommits()).ln()
    .append("(").ln()
    .append("  id VARCHAR(40) PRIMARY KEY,").ln()
    .append("  datetime VARCHAR(29) NOT NULL,").ln()
    .append("  author VARCHAR(40) NOT NULL,").ln()
    .append("  message VARCHAR(255) NOT NULL,").ln()
    .append("  tree VARCHAR(40) NOT NULL,").ln()
    .append("  parent VARCHAR(40),").ln()
    .append("  merge VARCHAR(40)").ln()
    .append(");").ln()
    
    .append("CREATE INDEX ").append(options.getCommits()).append("_TREE_INDEX")
    .append(" ON ").append(options.getCommits()).append(" (tree);").ln()
    
    .append("CREATE INDEX ").append(options.getCommits()).append("_PARENT_INDEX")
    .append(" ON ").append(options.getCommits()).append(" (tree);").ln()
    .build()).build();
  }
  @Override
  public Sql createConstraints() {
    return ImmutableSql.builder()
        .value(new SqlStatement().ln()
        .append("ALTER TABLE ").append(options.getCommits()).ln()
        .append("  ADD CONSTRAINT ").append(options.getCommits()).append("_COMMIT_PARENT_FK").ln()
        .append("  FOREIGN KEY (parent)").ln()
        .append("  REFERENCES ").append(options.getCommits()).append(" (id);").ln()
        
        .append("ALTER TABLE ").append(options.getCommits()).ln()
        .append("  ADD CONSTRAINT ").append(options.getCommits()).append("_COMMIT_TREE_FK").ln()
        .append("  FOREIGN KEY (tree)").ln()
        .append("  REFERENCES ").append(options.getTrees()).append(" (id);").ln()
        .build())
        .build();
  }
  @Override
  public Sql dropTable() {
    return ImmutableSql.builder().value(new SqlStatement()
        .append("DROP TABLE ").append(options.getCommits()).append(";").ln()
        .build()).build();
  }
}
