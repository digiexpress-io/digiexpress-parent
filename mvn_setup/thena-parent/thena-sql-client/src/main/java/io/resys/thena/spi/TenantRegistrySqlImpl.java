package io.resys.thena.spi;

import java.util.Arrays;

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
import io.resys.thena.datasource.ImmutableSql;
import io.resys.thena.datasource.ImmutableSqlTuple;
import io.resys.thena.datasource.TenantContext;
import io.resys.thena.datasource.ThenaSqlClient;
import io.resys.thena.datasource.ThenaSqlClient.Sql;
import io.resys.thena.datasource.ThenaSqlClient.SqlTuple;
import io.resys.thena.storesql.support.SqlStatement;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.Tuple;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TenantRegistrySqlImpl implements TenantRegistry {
  private final TenantContext options;
  
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
  public ThenaSqlClient.Sql findAllWithLabels() {
    return ImmutableSql.builder()
        .value(new SqlStatement()
        .append("SELECT * FROM ").append(options.getTenant()).append(" WHERE label IS NOT NULL")
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
        .append(" (id, rev, prefix, name, type, label, comment, external_id) VALUES($1, $2, $3, $4, $5, $6, $7, $8)")
        .build())
        .props(Tuple.from(Arrays.asList(
            newRepo.getId(), newRepo.getRev(), newRepo.getPrefix(), 
            newRepo.getName(), newRepo.getType(),
            
            newRepo.getLabel(), newRepo.getComment(),
            newRepo.getExternalId()
        )))
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
    StructureType type;
    
    try {
      type = StructureType.valueOf(row.getString("type"));
    } catch(Exception e) {
      type = StructureType.unknown;
    }
    
    return ImmutableTenant.builder()
        .id(row.getString("id"))
        .rev(row.getString("rev"))
        .name(row.getString("name"))
        .externalId(row.getString("external_id"))
        .label(row.getString("label"))
        .comment(row.getString("comment"))
        .type(type)
        .prefix(row.getString("prefix"))
        .build();
  }
  
  @Override
  public Sql createTable() {
    return ImmutableSql.builder().value(new SqlStatement()
        .append(
"""
CREATE TABLE IF NOT EXISTS {tenant} (
  id VARCHAR(40) PRIMARY KEY,
  rev VARCHAR(40) NOT NULL,
  prefix VARCHAR(40) NOT NULL,
  type VARCHAR(40) NOT NULL,
  name VARCHAR(255) NOT NULL,
  external_id VARCHAR(255),
  label TEXT,
  comment TEXT,
  UNIQUE(name), UNIQUE(rev), UNIQUE(prefix), UNIQUE(external_id)
);

CREATE INDEX IF NOT EXISTS {tenant}_NAME_INDEX ON {tenant} (name);
CREATE INDEX IF NOT EXISTS {tenant}_EXT_INDEX ON {tenant} (external_id);

DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = '{alias}_config') THEN

    CREATE DOMAIN {alias}_text AS TEXT NOT NULL;
    CREATE DOMAIN {alias}_jsonb AS JSONB NOT NULL;

    CREATE TYPE {alias}_config AS (
      config_type {alias}_text,
      config_body {alias}_jsonb
    );
  END IF;
END
$$;

CREATE TABLE IF NOT EXISTS {alias} (
  id UUID PRIMARY KEY,
  ref_tenant_id TEXT NOT NULL REFERENCES {tenant}(id),
  alias_tenant_id TEXT NOT NULL REFERENCES {tenant}(id),
  
  created_at TIMESTAMPTZ NOT NULL,
  updated_at TIMESTAMPTZ NOT NULL,
  
  created_by TEXT NOT NULL,
  updated_by TEXT NOT NULL,
  
  alias_name TEXT NOT NULL UNIQUE,
  alias_desc TEXT,
  alias_config {alias}_config[]
);

CREATE INDEX IF NOT EXISTS {alias}_REF_INDEX ON {alias} (ref_tenant_id);
CREATE INDEX IF NOT EXISTS {alias}_ALIAS_INDEX ON {alias} (alias_tenant_id);
CREATE INDEX IF NOT EXISTS {alias}_NAME_INDEX ON {alias} (alias_name);


CREATE TABLE IF NOT EXISTS {member} (
  id UUID PRIMARY KEY,
  external_id TEXT NOT NULL,
  alias_id UUID REFERENCES {alias}(id),
  alias_status BOOLEAN NOT NULL,
  
  UNIQUE (external_id, alias_id)
);

CREATE INDEX IF NOT EXISTS {member}_EXT_INDEX ON {member} (external_id);
CREATE INDEX IF NOT EXISTS {member}_ALIAS_INDEX ON {member} (alias_id);
CREATE INDEX IF NOT EXISTS {member}_GRP_INDEX ON {member} (external_id, alias_id);

""".replace("{tenant}", options.getTenant())
   .replace("{member}", options.getMember())
   .replace("{alias}", options.getAlias())).build()).build();
  }
  @Override
  public Sql createConstraints() {
    return ImmutableSql.builder().value("").build();
  }
  @Override
  public Sql dropTable() {
    return ImmutableSql.builder().value(new SqlStatement()
        .append("DROP TABLE IF EXISTS ").append(options.getTenant()).append(";").ln()
        .append("DROP TABLE IF EXISTS ").append(options.getMember()).append(";").ln()
        .append("DROP TABLE IF EXISTS ").append(options.getAlias()).append(";").ln()
        .append("DROP TYPE IF EXISTS ").append(options.getAlias()).append("_config;").ln()
        
        .build()).build();
  }
}