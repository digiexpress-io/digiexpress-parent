package io.resys.thena.spi;

/*-
 * #%L
 * thena-sql-client
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

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import io.resys.thena.api.entities.Alias;
import io.resys.thena.api.entities.ImmutableAlias;
import io.resys.thena.api.entities.ImmutableAliasConfig;
import io.resys.thena.datasource.ImmutableSql;
import io.resys.thena.datasource.ImmutableSqlTuple;
import io.resys.thena.datasource.TenantContext;
import io.resys.thena.datasource.ThenaSqlClient;
import io.resys.thena.datasource.ThenaSqlClient.Sql;
import io.resys.thena.datasource.ThenaSqlClient.SqlTuple;
import io.resys.thena.storesql.support.SqlStatement;
import io.resys.thena.support.TableUtils;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.Tuple;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AliasRegistrySqlImpl implements AliasRegistry {
  private final TenantContext options;
  
  @Override
  public ThenaSqlClient.SqlTuple exists() {
    return ImmutableSqlTuple.builder().value(new SqlStatement().ln()
        .append(
"""
SELECT EXISTS(
  SELECT table_name
  FROM information_schema.tables
  WHERE table_name = $1
)
""")
        .build())
        .props(Tuple.of(options.getAlias()))
        .build();
  }  
  @Override
  public ThenaSqlClient.Sql findAll() {
    return ImmutableSql.builder()
        .value(new SqlStatement()
        .append("SELECT * FROM ").append(options.getAlias())
        .build())
        .build();
  }
  @Override
  public ThenaSqlClient.SqlTuple getByName(String name) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append(
"""
  SELECT *
  FROM {alias} as alias
  WHERE alias_name = $1
  FETCH FIRST ROW ONLY
""".replace("{alias}", options.getAlias()))
        .build())
        .props(Tuple.of(name))
        .build();
  }
  @Override
  public ThenaSqlClient.SqlTuple getByNameOrId(String name) {
    
    UUID id;
    try {
      id = TableUtils.toUuid(name);
    } catch(Exception e) {
      id = null;
    }

    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
          .append(
"""
  SELECT *
  FROM {alias} as alias
  WHERE alias_name = $1 OR id = $2
  FETCH FIRST ROW ONLY
""".replace("{alias}", options.getAlias()))
        .build())
        .props(Tuple.of(name, id))
        .build();
  }
  @Override
  public ThenaSqlClient.SqlTuple insertOne(Alias newRepo) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append(
"""
INSERT INTO {alias} (
  id, 
  ref_tenant_id, alias_tenant_id,
  created_at, updated_at,
  created_by, updated_by,
  alias_name, alias_desc, alias_config
) 
VALUES($1, $2, $3, $4, $5, $6, $7, $8, $9,
  ARRAY(
    SELECT node::{alias}_config 
    FROM jsonb_populate_recordset(NULL::{alias}_config, $10::jsonb) as node
  )
)
""".replace("{alias}", options.getAlias()))
        .build())
        .props(Tuple.from(Arrays.asList(
          newRepo.getId(), 
          newRepo.getRefTenantId(), newRepo.getAliasTenantId(),
          newRepo.getCreatedAt(), newRepo.getUpdatedAt(),
          newRepo.getCreatedBy(), newRepo.getUpdatedBy(),
          newRepo.getAliasName(), newRepo.getAliasDesc(),
          new JsonArray(newRepo.getAliasConfig().stream()
              .map(e -> new JsonObject(Map.of(
                  "config_type", e.getConfigType(), 
                  "config_body", e.getConfigBody())))
              .toList())
        )))
        .build();
  }
  @Override
  public ThenaSqlClient.SqlTuple updateOne(Alias newRepo) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append(
"""
UPDATE {alias} SET
  updated_at = $1,
  updated_by = $2,
  alias_desc = $3, 
  alias_config = ARRAY(
    SELECT node::{alias}_config 
    FROM jsonb_populate_recordset(NULL::{alias}_config, $4::jsonb) as node
  ) 
WHERE id = $5
""".replace("{alias}", options.getAlias()))
        .build())
        .props(Tuple.of(
            newRepo.getUpdatedAt(),
            newRepo.getUpdatedBy(),
            newRepo.getAliasDesc(),
            new JsonArray(newRepo.getAliasConfig()),
            newRepo.getId()
        ))
        .build();
  }
  @Override
  public ThenaSqlClient.SqlTuple deleteOne(Alias newRepo) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("DELETE FROM ").append(options.getAlias())
        .append(" WHERE id = $1")
        .build())
        .props(Tuple.of(newRepo.getId()))
        .build();
  }
  @Override
  public SqlTuple getById(String id) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT * FROM ").append(options.getAlias())
        .append(" WHERE id = $1")
        .append(" FETCH FIRST ROW ONLY")
        .build())
        .props(Tuple.of(id))
        .build();
  }
  @Override
  public Function<Row, Alias> defaultMapper() {
    return AliasRegistrySqlImpl::repo;
  }
  private static Alias repo(Row row) {
    
    
    final var aliasConfig = Arrays.asList(Optional.ofNullable(row.getArrayOfStrings("alias_config"))
      .orElse(new String[]{}))
      .stream()
      .map(text -> {
        final var configType = text.substring(1, text.indexOf(","));
        final var configBody = text.substring(text.indexOf(",")+2, text.length() - 2)
            .replace("\"\"", "\"");
        
        return ImmutableAliasConfig.builder()
            .configType(configType)
            .configBody(new JsonObject(configBody))
            .build();
      })
      .toList();
    
    return ImmutableAlias.builder()
        .id(row.getUUID("id"))
        .refTenantId(row.getString("ref_tenant_id"))
        .aliasTenantId(row.getString("alias_tenant_id"))
        .createdAt(row.getOffsetDateTime("created_at"))
        .updatedAt(row.getOffsetDateTime("updated_at"))
        .createdBy(row.getString("created_by"))
        .updatedBy(row.getString("updated_by"))

        .aliasName(row.getString("alias_name"))
        .aliasDesc(row.getString("alias_desc"))
        .aliasConfig(aliasConfig)
        .build();
  }
  @Override
  public Sql createTable() {
    return ImmutableSql.builder().value("").build();
  }
  @Override
  public Sql createConstraints() {
    return ImmutableSql.builder().value("").build();
  }
  @Override
  public Sql dropTable() {
    return ImmutableSql.builder().value("").build();
  }
}
