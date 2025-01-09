package io.resys.thena.registry.org;

/*-
 * #%L
 * thena-docdb-api
 * %%
 * Copyright (C) 2015 - 2024 Copyright 2022 ReSys OÜ
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
import java.util.function.Function;
import java.util.stream.Collectors;

import io.resys.thena.api.entities.org.OrgCommitTree;
import io.resys.thena.api.registry.org.OrgCommitTreeRegistry;
import io.resys.thena.datasource.ImmutableSql;
import io.resys.thena.datasource.ImmutableSqlTuple;
import io.resys.thena.datasource.ImmutableSqlTupleList;
import io.resys.thena.datasource.TenantTableNames;
import io.resys.thena.datasource.ThenaSqlClient;
import io.resys.thena.storesql.support.SqlStatement;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.Tuple;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OrgCommitTreeRegistrySqlImpl implements OrgCommitTreeRegistry {
  private final TenantTableNames options;
  @Override
  public ThenaSqlClient.Sql findAll() {
    return ImmutableSql.builder()
        .value(new SqlStatement()
        .append("SELECT * FROM ").append(options.getOrgCommitTrees())
        .build())
        .build();
  }
  @Override
  public ThenaSqlClient.SqlTuple getById(String id) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT * ").ln()
        .append("  FROM ").append(options.getOrgCommitTrees()).ln()
        .append("  WHERE id = $1").ln() 
        .build())
        .props(Tuple.of(id))
        .build();
  }
  @Override
  public ThenaSqlClient.SqlTupleList insertAll(Collection<OrgCommitTree> commits) {
    return ImmutableSqlTupleList.builder()
        .value(new SqlStatement()
        .append("INSERT INTO ").append(options.getOrgCommitTrees()).ln()
        .append(" (id,").ln()
        .append("  commit_id,").ln()
        .append("  operation_type,").ln()
        .append("  body_after,").ln()
        .append("  body_before,").ln()
        .append("  actor_id,").ln()
        .append("  actor_type)").ln()
        
        
        
        .append(" VALUES($1, $2, $3, $4, $5, $6, $7)").ln()
        .build())
        .props(commits.stream()
            .map(doc -> Tuple.from(new Object[]{ 
                doc.getId(), 
                doc.getCommitId(),
                doc.getOperationType().name(),
                doc.getBodyAfter(),
                doc.getBodyBefore(),
                doc.getActorId(),
                doc.getActorType()
             }))
            .collect(Collectors.toList()))
        .build();
  }
  @Override
  public ThenaSqlClient.SqlTuple findByCommmitId(String commitId) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT * ").ln()
        .append("  FROM ").append(options.getOrgCommitTrees()).ln()
        .append("  WHERE commit_id = $1").ln() 
        .build())
        .props(Tuple.of(commitId))
        .build();
  }
  @Override
  public Function<Row, OrgCommitTree> defaultMapper() {
    throw new RuntimeException("Not implemented");
  }
  @Override
  public ThenaSqlClient.Sql createTable() {
    return ImmutableSql.builder().value(new SqlStatement()
            
        .append("CREATE TABLE ").append(options.getOrgCommitTrees()).ln()
        .append("(").ln()
        .append("  id VARCHAR(40) PRIMARY KEY,").ln()
        .append("  commit_id VARCHAR(40) NOT NULL,").ln()
        
        .append("  actor_id VARCHAR(40) NOT NULL,").ln()
        .append("  actor_type VARCHAR(40) NOT NULL,").ln()

        .append("  operation_type VARCHAR(40),").ln()
        .append("  body_after JSONB,").ln()
        .append("  body_before JSONB").ln()
        
        .append(");").ln().ln()
        
        .append("CREATE INDEX ").append(options.getOrgCommitTrees()).append("_ACTOR_INDEX")
        .append(" ON ").append(options.getOrgCommitTrees()).append(" (actor_type, actor_id);").ln()
        
        .append("CREATE INDEX ").append(options.getOrgCommitTrees()).append("_COMMIT_INDEX")
        .append(" ON ").append(options.getOrgCommitTrees()).append(" (commit_id);").ln()
        
        
        .build()).build();
  }
  @Override
  public ThenaSqlClient.Sql createConstraints() {
    return ImmutableSql.builder().value("").build();
  }
  @Override
  public ThenaSqlClient.Sql dropTable() {
    return ImmutableSql.builder().value(new SqlStatement()
        .append("DROP TABLE ").append(options.getOrgCommitTrees()).append(";").ln()
        .build()).build();
  }

}
