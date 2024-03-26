package io.resys.thena.storesql.statement;

import java.io.Serializable;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
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

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import io.resys.thena.api.actions.GitPullActions.MatchCriteria;
import io.resys.thena.api.actions.GitPullActions.MatchCriteriaType;
import io.resys.thena.api.entities.git.Blob;
import io.resys.thena.spi.DbCollections;
import io.resys.thena.storesql.ImmutableSql;
import io.resys.thena.storesql.ImmutableSqlTuple;
import io.resys.thena.storesql.ImmutableSqlTupleList;
import io.resys.thena.storesql.SqlBuilder.GitBlobSqlBuilder;
import io.resys.thena.storesql.SqlBuilder.Sql;
import io.resys.thena.storesql.SqlBuilder.SqlTuple;
import io.resys.thena.storesql.SqlBuilder.SqlTupleList;
import io.resys.thena.storesql.support.SqlStatement;
import io.resys.thena.support.RepoAssert;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.sqlclient.Tuple;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GitBlobSqlBuilderImpl implements GitBlobSqlBuilder {
  protected final DbCollections options;
  
  @Override
  public Sql findAll() {
    return ImmutableSql.builder()
        .value(new SqlStatement()
        .append("SELECT * FROM ").append(options.getBlobs())
        .build())
        .build();
  }
  @Override
  public SqlTuple getById(String blobId) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT * FROM ").append(options.getBlobs())
        .append(" WHERE id = $1")
        .append(" FETCH FIRST ROW ONLY")
        .build())
        .props(Tuple.of(blobId))
        .build();
  }
  @Override
  public SqlTuple findByIds(Collection<String> blobId) {
    StringBuilder params = new StringBuilder();
    List<String> tuple = new ArrayList<>();
    
    int index = 1;
    for(final var id : blobId) {
      if(params.length() == 0) {
        params.append(" WHERE ");
      } else {
        params.append(" OR ");
      }
      params.append(" id = $").append(index++);
      tuple.add(id);
    }
    
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT * FROM ").append(options.getBlobs())
        .append(params.toString())
        .build())
        .props(Tuple.from(tuple))
        .build();
  }
  @Override
  public SqlTuple findByTree(String treeId, List<MatchCriteria> criteria) {
    final var conditions = createWhereCriteria(criteria);
    final var props = new LinkedList<>(conditions.getProps());
    final var treeIdPos = props.size() + 1;
    props.add(treeId);
    
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT blobs.* ").ln()
        .append("  FROM ").append(options.getBlobs()).append(" AS blobs ").ln()
        .append("  LEFT JOIN ").append(options.getTreeItems()).append(" AS item ").ln()
        .append("  ON blobs.id = item.blob").ln()
        .append("  WHERE ").append(conditions.getValue())
        .append(conditions.getValue().isEmpty() ? "  " : "  AND ")
        .append("item.tree = $").append(String.valueOf(treeIdPos)).ln()
        .append(" ")
        .build())
        .props(Tuple.from(props))
        .build();
  }
  @Override
  public SqlTuple findByTree(String treeId, List<String> blobNames, List<MatchCriteria> criteria) {
    final var conditions = createWhereCriteria(criteria);
    final var props = new LinkedList<>(conditions.getProps());
    final var treeIdPos = props.size() + 1;
    props.add(treeId);
    
    final var nameCriteria = new StringBuilder();
    var nameIndex = treeIdPos;
    for(final var name : blobNames) {
      final var pos = ++nameIndex;
      
      if(!nameCriteria.isEmpty()) {
        nameCriteria.append(" OR");
      }
      nameCriteria.append(" item.name = $").append(pos);
      
      props.add(name);
    }
    
    if(!nameCriteria.isEmpty()) {
      nameCriteria.insert(0, " AND (").append(")");
    }

    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT blobs.* ").ln()
        .append("  FROM ").append(options.getBlobs()).append(" AS blobs ").ln()
        .append("  LEFT JOIN ").append(options.getTreeItems()).append(" AS item ").ln()
        .append("  ON blobs.id = item.blob").ln()
        .append("  WHERE ").append(conditions.getValue())
        .append(conditions.getValue().isEmpty() ? "  " : "  AND ")
        .append("item.tree = $").append(String.valueOf(treeIdPos))
        .append(nameCriteria.toString())
        .ln()
        .build())
        .props(Tuple.from(props))
        .build();
  }
/**

WITH RECURSIVE generation AS (
    SELECT id, parent, 0 AS order_no
    FROM nested_10_commits
    WHERE parent IS NULL
UNION ALL
    SELECT child.id, child.parent, order_no+1 AS order_no
    FROM nested_10_commits as child
    JOIN generation g ON g.id = child.parent
)
SELECT * 
FROM ( SELECT 
  blob_name,
  blob_value,
  blob_id,
  tree,
  commit_parent,
  commit_id,
  order_no
  RANK() OVER (PARTITION BY blob_name, order_no ORDER BY order_no DESC) AS RANK
FROM (SELECT 
  item.name as blob_name,
  blobs.value as blob_value,
  item.blob as blob_id,
  item.tree as tree,
  commit.parent as commit_parent,
  commit.id as commit_id,
  generation.order_no as order_no
FROM 
  nested_10_treeItems as item
  LEFT JOIN nested_10_commits AS commit
  ON(commit.tree = item.tree)
  LEFT JOIN generation ON(generation.id = commit.id)
  LEFT JOIN nested_10_blobs AS blobs ON(blobs.id = item.blob)
WHERE blobs.value LIKE $1
)
) WHERE RANK = 1

   */
  @RequiredArgsConstructor @lombok.Data
  protected static class WhereSqlFragment {
    private final String value;
    private final List<Object> props;
  }

  protected String createLatest(String fromData) {
    return new SqlStatement()
        .append("SELECT ranked.* ").ln()
        .append("FROM ( SELECT ").ln()
        .append("  RANK() OVER (PARTITION BY blob_name ORDER BY order_no DESC) AS RANK,").ln()
        .append("  by_commit.*").ln()
        .append("FROM (").append(fromData).append(") as by_commit").ln()
        .append(") as ranked WHERE ranked.RANK = 1").ln()
        .toString();           
  }
  
  protected String createRecursionSelect() {
    return new SqlStatement()
        .append("SELECT ").ln()
        .append("  item.name as blob_name,").ln()
        .append("  blobs.value as blob_value,").ln()
        .append("  item.blob as blob_id,").ln()
        .append("  item.tree as tree,").ln()
        .append("  commit.parent as commit_parent,").ln()
        .append("  commit.id as commit_id,").ln()
        .append("  generation.order_no as order_no").ln()
        .append("FROM ").ln()
        .append("  ").append(options.getTreeItems()).append(" as item").ln()
        .append("  LEFT JOIN ").append(options.getCommits()).append(" AS commit").ln()
        .append("  ON(commit.tree = item.tree)").ln()
        .append("  LEFT JOIN generation ON(generation.id = commit.id)").ln()
        .append("  LEFT JOIN ").append(options.getBlobs()).append(" AS blobs ON(blobs.id = item.blob)").ln()
        .toString();       
  }
  
  protected String createRecursion() {
    return new SqlStatement()
        .append("WITH RECURSIVE generation AS (").ln()
        .append("    SELECT id, parent, 0 AS order_no").ln()
        .append("    FROM ").append(options.getCommits()).ln()
        .append("    WHERE parent IS NULL").ln()
        .append("UNION ALL").ln()
        .append("    SELECT child.id, child.parent, order_no+1 AS order_no").ln()
        .append("    FROM ").append(options.getCommits()).append(" as child").ln()
        .append("    JOIN generation g ON g.id = child.parent").ln()
        .append(")").ln().toString();       
  }
  

  private static final DateTimeFormatter ISO_LOCAL_DATE_TIME = new DateTimeFormatterBuilder()
              .parseCaseInsensitive()
              .append(DateTimeFormatter.ISO_LOCAL_DATE)
              .appendLiteral(' ')
              .append(DateTimeFormatter.ISO_LOCAL_TIME)
              .toFormatter();

  
  @Override
  public SqlTuple insertOne(Blob blob) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("INSERT INTO ").append(options.getBlobs())
        .append(" (id, value) VALUES($1, $2)")
        .append(" ON CONFLICT (id) DO NOTHING")
        .build())
        .props(Tuple.of(blob.getId(), blob.getValue()))
        .build();
  }
  @Override
  public SqlTupleList insertAll(Collection<Blob> blobs) {
    return ImmutableSqlTupleList.builder()
        .value(new SqlStatement()
        .append("INSERT INTO ").append(options.getBlobs())
        .append(" (id, value) VALUES($1, $2)")
        .append(" ON CONFLICT (id) DO NOTHING")
        .build())
        .props(blobs.stream()
            .map(v -> Tuple.of(v.getId(), v.getValue()))
            .collect(Collectors.toList()))
        .build();
  }
  
  protected WhereSqlFragment createWhereCriteria(List<MatchCriteria> criteria) {
    final var props = new LinkedList<>();
    final var where = new SqlStatement();
    int paramIndex = 1;
    for(final var entry : criteria) {
      if(paramIndex > 1) {
        where.append(" AND ").ln();
      }
      // TODO:: null value props
      props.add(entry.getKey());
      if(entry.getType() == MatchCriteriaType.EQUALS) {
        props.add(getCriteriaValue(entry));
        where.append("blobs.value -> ")
          .append(getCriteriaField(entry, paramIndex++))
          .append(" = $")
          .append(String.valueOf(paramIndex++)).ln();

      } else if(entry.getType() == MatchCriteriaType.GTE && entry.getTargetDate() != null) {
        props.add(getCriteriaValue(entry));
        where.append("blobs.value ->> ")
          .append(getCriteriaField(entry, paramIndex++))
          .append(" <= $")
          .append(String.valueOf(paramIndex++)).append("").ln();
        
      } else if(entry.getType() == MatchCriteriaType.LIKE && entry.getValue() != null)  {
        props.add("%"+ entry.getValue() + "%");
        where.append("blobs.value ->> $")
        .append(String.valueOf(paramIndex++))
        .append(" like $")
        .append(String.valueOf(paramIndex++)).ln();
        
      } else if(entry.getType() == MatchCriteriaType.NOT_NULL)  {
        where.append("blobs.value ->> $")
        .append(String.valueOf(paramIndex++))
        .append(" is not null").ln();
        
      } else {
        throw new RuntimeException("Criteria type: " + JsonArray.of(criteria) + " not supported!");
      }
    }
  
    return new WhereSqlFragment(where.toString(), props);
  }
  
  
  private static Serializable getCriteriaValue(MatchCriteria criteria) {
    RepoAssert.isTrue(criteria.getValue() != null || criteria.getTargetDate() != null, () -> "Criteria must define value! But was: " + JsonObject.mapFrom(criteria));
    
    if(criteria.getTargetDate() != null) {
      return criteria.getTargetDate().format(ISO_LOCAL_DATE_TIME);
    }
    return criteria.getValue();
  }
  
  private static String getCriteriaField(MatchCriteria criteria, int fieldIndex) {
    RepoAssert.isTrue(criteria.getValue() != null || criteria.getTargetDate() != null, () -> "Criteria must define value! But was: " + JsonObject.mapFrom(criteria));
    
    if(criteria.getTargetDate() != null) {
      return "$" + String.valueOf(fieldIndex) + "";
    }
    return "$" + String.valueOf(fieldIndex);
  }
  
  @Override
  public SqlTuple find(String name, boolean latestOnly, List<MatchCriteria> criteria) {

    final String sql;
    final var conditions = createWhereCriteria(criteria);
    final var where = new StringBuilder(conditions.getValue());
    if(!where.isEmpty()) {
      where.insert(0, "WHERE ");
    }

    
    if(latestOnly) {
      final var fromData = new SqlStatement()
          .append(createRecursionSelect())
          .append(where.toString()).ln()
          .build();
      sql = new SqlStatement().append(createRecursion()).append(createLatest(fromData)).build();
    } else {
      sql = new SqlStatement()
          .append(createRecursion())
          .append(createRecursionSelect())
          .append(where.toString()).ln()
          .build();      
    }
    return ImmutableSqlTuple.builder()
        .value(sql)
        .props(Tuple.from(conditions.getProps()))
        .build();
  }
}
