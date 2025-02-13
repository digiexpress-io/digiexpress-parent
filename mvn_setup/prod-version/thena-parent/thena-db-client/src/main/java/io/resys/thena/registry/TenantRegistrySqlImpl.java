package io.resys.thena.registry;

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

import java.util.function.Function;

import io.resys.thena.api.entities.ImmutableTenant;
import io.resys.thena.api.entities.Tenant;
import io.resys.thena.api.entities.Tenant.StructureType;
import io.resys.thena.api.registry.TenantRegistry;
import io.resys.thena.datasource.ImmutableSql;
import io.resys.thena.datasource.ImmutableSqlTuple;
import io.resys.thena.datasource.TenantTableNames;
import io.resys.thena.datasource.ThenaSqlClient;
import io.resys.thena.datasource.ThenaSqlClient.Sql;
import io.resys.thena.datasource.ThenaSqlClient.SqlTuple;
import io.resys.thena.storesql.support.SqlStatement;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.Tuple;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TenantRegistrySqlImpl implements TenantRegistry {
  private final TenantTableNames options;
  
  @Override
  public ThenaSqlClient.SqlTuple exists() {
    return ImmutableSqlTuple.builder().value(new SqlStatement().ln()
        .append("SELECT EXISTS").ln()
        .append("(").ln()
        .append("  SELECT table_name").ln()
        .append("  FROM information_schema.tables").ln()
        .append("  WHERE table_name = ?1").ln()
        .append(")").ln().build())
        .props(Tuple.of(options.getTenant()))
        .build();
  }  
  @Override
  public ThenaSqlClient.Sql findAll() {
    return ImmutableSql.builder()
        .value(new SqlStatement()
        .append("SELECT * FROM ").append(options.getTenant())
        .build())
        .build();
  }
  @Override
  public ThenaSqlClient.SqlTuple getByName(String name) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT * FROM ").append(options.getTenant())
        .append(" WHERE name = $1")
        .append(" FETCH FIRST ROW ONLY")
        .build())
        .props(Tuple.of(name))
        .build();
  }
  @Override
  public ThenaSqlClient.SqlTuple getByNameOrId(String name) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT * FROM ").append(options.getTenant())
        .append(" WHERE name = $1 OR id = $1 OR external_id = $1")
        .append(" FETCH FIRST ROW ONLY")
        .build())
        .props(Tuple.of(name))
        .build();
  }
  @Override
  public ThenaSqlClient.SqlTuple insertOne(Tenant newRepo) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("INSERT INTO ").append(options.getTenant())
        .append(" (id, rev, prefix, name, type, external_id) VALUES($1, $2, $3, $4, $5, $6)")
        .build())
        .props(Tuple.of(newRepo.getId(), newRepo.getRev(), newRepo.getPrefix(), newRepo.getName(), newRepo.getType(), newRepo.getExternalId()))
        .build();
  }
  
  @Override
  public ThenaSqlClient.SqlTuple deleteOne(Tenant newRepo) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("DELETE FROM ").append(options.getTenant())
        .append(" WHERE id = $1")
        .build())
        .props(Tuple.of(newRepo.getId()))
        .build();
  }
  @Override
  public SqlTuple getById(String id) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT * FROM ").append(options.getTenant())
        .append(" WHERE id = $1")
        .append(" FETCH FIRST ROW ONLY")
        .build())
        .props(Tuple.of(id))
        .build();
  }
  @Override
  public Function<Row, Tenant> defaultMapper() {
    return TenantRegistrySqlImpl::repo;
  }
  private static Tenant repo(Row row) {
    return ImmutableTenant.builder()
        .id(row.getString("id"))
        .rev(row.getString("rev"))
        .name(row.getString("name"))
        .externalId(row.getString("external_id"))
        .type(StructureType.valueOf(row.getString("type")))
        .prefix(row.getString("prefix"))
        .build();
  }
  
  @Override
  public Sql createTable() {
    return ImmutableSql.builder().value(new SqlStatement()
        .append("CREATE TABLE IF NOT EXISTS ").append(options.getTenant()).ln()
        .append("(").ln()
        .append("  id VARCHAR(40) PRIMARY KEY,").ln()
        .append("  rev VARCHAR(40) NOT NULL,").ln()
        .append("  prefix VARCHAR(40) NOT NULL,").ln()
        .append("  type VARCHAR(40) NOT NULL,").ln()
        .append("  name VARCHAR(255) NOT NULL,").ln()
        .append("  external_id VARCHAR(255),").ln()
        .append("  UNIQUE(name), UNIQUE(rev), UNIQUE(prefix), UNIQUE(external_id)").ln()
        .append(");").ln()

        .append("CREATE INDEX IF NOT EXISTS ").append(options.getTenant()).append("_NAME_INDEX")
        .append(" ON ").append(options.getTenant()).append(" (name);").ln()
        .append("CREATE INDEX IF NOT EXISTS ").append(options.getTenant()).append("_EXT_INDEX")
        .append(" ON ").append(options.getTenant()).append(" (external_id);").ln()
        
        .build()).build();
  }
  @Override
  public Sql createConstraints() {
    return ImmutableSql.builder().value("").build();
  }
  @Override
  public Sql dropTable() {
    return ImmutableSql.builder().value(new SqlStatement()
        .append("DROP TABLE IF EXISTS ").append(options.getTenant()).append(";").ln()
        .build()).build();
  }
}
