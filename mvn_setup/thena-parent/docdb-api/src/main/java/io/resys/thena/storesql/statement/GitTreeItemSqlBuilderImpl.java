package io.resys.thena.storesql.statement;

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

import java.util.stream.Collectors;

import io.resys.thena.api.entities.git.Tree;
import io.resys.thena.api.entities.git.TreeValue;
import io.resys.thena.datasource.TenantTableNames;
import io.resys.thena.datasource.ImmutableSql;
import io.resys.thena.datasource.ImmutableSqlTuple;
import io.resys.thena.datasource.ImmutableSqlTupleList;
import io.resys.thena.datasource.SqlQueryBuilder.GitTreeItemSqlBuilder;
import io.resys.thena.datasource.SqlQueryBuilder.Sql;
import io.resys.thena.datasource.SqlQueryBuilder.SqlTuple;
import io.resys.thena.datasource.SqlQueryBuilder.SqlTupleList;
import io.resys.thena.storesql.support.SqlStatement;
import io.vertx.mutiny.sqlclient.Tuple;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GitTreeItemSqlBuilderImpl implements GitTreeItemSqlBuilder {
  private final TenantTableNames options;
  
  @Override
  public Sql findAll() {
    return ImmutableSql.builder()
        .value(new SqlStatement()
        .append("SELECT * FROM ").append(options.getTreeItems())
        .build())
        .build();
  }
  @Override
  public SqlTuple getByTreeId(String treeId) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT * ").ln()
        .append("  FROM ").append(options.getTreeItems()).ln()
        .append("  WHERE tree = $1").ln()
        .build())
        .props(Tuple.of(treeId))
        .build();
  }
  @Override
  public SqlTuple insertOne(Tree tree, TreeValue item) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("INSERT INTO ").append(options.getTreeItems())
        .append(" (name, blob, tree) VALUES($1, $2, $3)").ln()
        .append(" ON CONFLICT (name, blob, tree) DO NOTHING")
        .build())
        .props(Tuple.of(item.getName(), item.getBlob(), tree.getId()))
        .build();
  }
  @Override
  public SqlTupleList insertAll(Tree item) {
    return ImmutableSqlTupleList.builder()
        .value(new SqlStatement()
        .append("INSERT INTO ").append(options.getTreeItems())
        .append(" (name, blob, tree) VALUES($1, $2, $3)").ln()
        .append(" ON CONFLICT (name, blob, tree) DO NOTHING")
        .build())
        .props(item.getValues().values().stream()
            .map(v -> Tuple.of(v.getName(), v.getBlob(), item.getId()))
            .collect(Collectors.toList()))
        .build();
  }
}
