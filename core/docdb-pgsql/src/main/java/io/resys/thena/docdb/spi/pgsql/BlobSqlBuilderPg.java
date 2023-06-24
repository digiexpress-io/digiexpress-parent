package io.resys.thena.docdb.spi.pgsql;

/*-
 * #%L
 * thena-docdb-pgsql
 * %%
 * Copyright (C) 2021 - 2023 Copyright 2021 ReSys OÜ
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

import io.resys.thena.docdb.api.actions.PullActions.MatchCriteria;
import io.resys.thena.docdb.api.actions.PullActions.MatchCriteriaType;
import io.resys.thena.docdb.api.models.ThenaObject.Blob;
import io.resys.thena.docdb.spi.ClientCollections;
import io.resys.thena.docdb.sql.ImmutableSqlTuple;
import io.resys.thena.docdb.sql.ImmutableSqlTupleList;
import io.resys.thena.docdb.sql.SqlBuilder.BlobSqlBuilder;
import io.resys.thena.docdb.sql.SqlBuilder.SqlTuple;
import io.resys.thena.docdb.sql.SqlBuilder.SqlTupleList;
import io.resys.thena.docdb.sql.statement.DefaultBlobSqlBuilder;
import io.resys.thena.docdb.sql.support.SqlStatement;
import io.vertx.mutiny.sqlclient.Tuple;


public class BlobSqlBuilderPg extends DefaultBlobSqlBuilder implements BlobSqlBuilder {
  
  public BlobSqlBuilderPg(ClientCollections options) {
    super(options);
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
  
  @Override
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
        props.add(entry.getValue());
        where.append("blobs.value -> $")
          .append(String.valueOf(paramIndex++))
          .append(" = $")
          .append(String.valueOf(paramIndex++)).ln();
      } else if(entry.getType() == MatchCriteriaType.LIKE)  {
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
        throw new RuntimeException("Criteria type: " + entry.getType() + " not defined");
      }
    }
  
    return new WhereSqlFragment(where.toString(), props);
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
