package io.resys.thena.docdb.sql.defaults;

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

import io.resys.thena.docdb.api.models.Objects.Tree;
import io.resys.thena.docdb.spi.ClientCollections;
import io.resys.thena.docdb.sql.ImmutableSql;
import io.resys.thena.docdb.sql.ImmutableSqlTuple;
import io.resys.thena.docdb.sql.SqlBuilder.Sql;
import io.resys.thena.docdb.sql.SqlBuilder.SqlTuple;
import io.resys.thena.docdb.sql.SqlBuilder.TreeSqlBuilder;
import io.vertx.mutiny.sqlclient.Tuple;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DefaultTreeSqlBuilder implements TreeSqlBuilder {
  private final ClientCollections options;
  
  @Override
  public Sql findAll() {
    return ImmutableSql.builder()
        .value(new SqlStatement()
        .append("SELECT * FROM ").append(options.getTrees())
        .build())
        .build();
  }
  @Override
  public SqlTuple getById(String id) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT * FROM ").append(options.getTrees())
        .append(" WHERE id = $1")
        .append(" FETCH FIRST ROW ONLY")
        .build())
        .props(Tuple.of(id))
        .build();
  }
  @Override
  public SqlTuple insertOne(Tree tree) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("INSERT INTO ").append(options.getTrees())
        .append(" (id) VALUES($1)")
        .append(" ON CONFLICT (id) DO NOTHING")
        .build())
        .props(Tuple.of(tree.getId()))
        .build();
  }
}
