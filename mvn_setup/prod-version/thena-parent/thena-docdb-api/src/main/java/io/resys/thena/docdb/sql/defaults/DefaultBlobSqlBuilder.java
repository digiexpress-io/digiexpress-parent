package io.resys.thena.docdb.sql.defaults;

import java.io.Serializable;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

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

import io.resys.thena.docdb.api.actions.ObjectsActions.MatchCriteria;
import io.resys.thena.docdb.api.actions.ObjectsActions.MatchCriteriaType;
import io.resys.thena.docdb.api.models.Objects.Blob;
import io.resys.thena.docdb.api.models.Objects.Tree;
import io.resys.thena.docdb.spi.ClientCollections;
import io.resys.thena.docdb.spi.support.RepoAssert;
import io.resys.thena.docdb.sql.ImmutableSql;
import io.resys.thena.docdb.sql.ImmutableSqlTuple;
import io.resys.thena.docdb.sql.ImmutableSqlTupleList;
import io.resys.thena.docdb.sql.SqlBuilder.BlobSqlBuilder;
import io.resys.thena.docdb.sql.SqlBuilder.Sql;
import io.resys.thena.docdb.sql.SqlBuilder.SqlTuple;
import io.resys.thena.docdb.sql.SqlBuilder.SqlTupleList;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.sqlclient.Tuple;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DefaultBlobSqlBuilder implements BlobSqlBuilder {
  private final ClientCollections options;
  private static final DateTimeFormatter ISO_LOCAL_DATE_TIME = new DateTimeFormatterBuilder()
      .parseCaseInsensitive()
      .append(DateTimeFormatter.ISO_LOCAL_DATE)
      .appendLiteral(' ')
      .append(DateTimeFormatter.ISO_LOCAL_TIME)
      .toFormatter();


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
  public SqlTuple findByIds(String treeId, Collection<String> blobId, List<MatchCriteria> criteria) {
    final var conditions = createWhereCriteria(criteria);
    final var props = new LinkedList<>(conditions.getProps());
    final var treeIdPos = props.size() + 1;
    props.add(treeId);
    
    final var nameCriteria = new StringBuilder();
    var nameIndex = treeIdPos;
    for(final var name : blobId) {
      final var pos = ++nameIndex;
      
      if(!nameCriteria.isEmpty()) {
        nameCriteria.append(" OR");
      }
      nameCriteria.append(" (item.name = $").append(pos).append(" OR item.blob = $").append(pos).append(")");
      
      props.add(name);
    }
    
    if(!nameCriteria.isEmpty()) {
      nameCriteria.insert(0, " AND (").append(")");
    }

    return ImmutableSqlTuple.builder().value(new SqlStatement()
      .append("SELECT blobs.* ").ln()
      .append("  FROM ").append(options.getBlobs()).append(" AS blobs ").ln()
      .append("  LEFT JOIN ").append(options.getTreeItems()).append(" AS item ").ln()
      .append("  ON blobs.id = item.blob").ln()
      .append("  WHERE ")
      .append(conditions.getValue())
      .append(conditions.getValue().isEmpty() ? "  " : "  AND ")
      .append("item.tree = $").append(String.valueOf(treeIdPos))
      .append(nameCriteria.toString())
      .ln()
      .build())
      .props(Tuple.from(props))
    .build();
  }
  @Override
  public SqlTuple findByTree(Tree tree) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT blob.* ").ln()
        .append("  FROM ").append(options.getBlobs()).append(" AS blob ").ln()
        .append("  LEFT JOIN ").append(options.getTreeItems()).append(" AS item ").ln()
        .append("  ON blob.id = item.blob").ln()
        .append("  WHERE item.tree = $1").ln()
        .append(" ")
        .build())
        .props(Tuple.of(tree.getId()))
        .build();
  }
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
      
      // target field
      where.append("blobs.value");
      
      var nextedIndex = 0;
      for(final var nestedProp : entry.getKey().split("\\.")) {
        if(nextedIndex++ > 0) {
          where.append(" -> $").append(String.valueOf(paramIndex++));
        }
        
        // TODO:: null value props
        props.add(nestedProp.trim());
        
      }
    
      
      if(entry.getType() == MatchCriteriaType.EQUALS) {
        props.add(getCriteriaValue(entry));
        where
          .append(" -> ")
          .append(getFieldIndex(entry, paramIndex++))
          .append(" = $")
          .append(String.valueOf(paramIndex++)).ln();

      } else if(entry.getType() == MatchCriteriaType.GTE && entry.getTargetDate() != null) {
        props.add(getCriteriaValue(entry));
        where.append("blobs.value")
          .append(" ->> ")
          .append(getFieldIndex(entry, paramIndex++))
          .append(" <= $")
          .append(String.valueOf(paramIndex++)).append("").ln();
        
      } else if(entry.getType() == MatchCriteriaType.LIKE && entry.getValue() != null)  {
        props.add("%"+ entry.getValue() + "%");
        where
        .append(" ->> $")
        .append(String.valueOf(paramIndex++))
        .append(" like $")
        .append(String.valueOf(paramIndex++)).ln();
        
      } else if(entry.getType() == MatchCriteriaType.NOT_NULL)  {
        where
        .append(" ->> $")
        .append(String.valueOf(paramIndex++))
        .append(" is not null").ln();
        
      } else {
        throw new RuntimeException("Criteria type: " + JsonArray.of(criteria) + " not supported!");
      }
    }
  
    return new WhereSqlFragment(where.build(), props);
  }
  
  
  private static Serializable getCriteriaValue(MatchCriteria criteria) {
    RepoAssert.isTrue(criteria.getValue() != null || criteria.getTargetDate() != null, () -> "Criteria must define value! But was: " + JsonObject.mapFrom(criteria));
    
    if(criteria.getTargetDate() != null) {
      return criteria.getTargetDate().format(ISO_LOCAL_DATE_TIME);
    }
    return criteria.getValue();
  }
  
  
  
  private static String getFieldIndex(MatchCriteria criteria, int fieldIndex) {
    RepoAssert.isTrue(criteria.getValue() != null || criteria.getTargetDate() != null, () -> "Criteria must define value! But was: " + JsonObject.mapFrom(criteria));
    return "$" + String.valueOf(fieldIndex);
  }
  
  
  @RequiredArgsConstructor @lombok.Data
  protected static class WhereSqlFragment {
    private final String value;
    private final List<Object> props;
  }
}
