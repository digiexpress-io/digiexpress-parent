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
import java.util.UUID;
import java.util.function.Function;

import io.resys.thena.api.entities.ImmutableMember;
import io.resys.thena.api.entities.Member;
import io.resys.thena.datasource.ImmutableSql;
import io.resys.thena.datasource.ImmutableSqlTuple;
import io.resys.thena.datasource.TenantContext;
import io.resys.thena.datasource.ThenaSqlClient;
import io.resys.thena.datasource.ThenaSqlClient.Sql;
import io.resys.thena.datasource.ThenaSqlClient.SqlTuple;
import io.resys.thena.storesql.support.SqlStatement;
import io.resys.thena.support.TableUtils;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.Tuple;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MemberRegistrySqlImpl implements MemberRegistry {
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
        .props(Tuple.of(options.getMember()))
        .build();
  }  
  @Override
  public ThenaSqlClient.Sql findAll() {
    return ImmutableSql.builder()
        .value(new SqlStatement()
        .append("SELECT * FROM ").append(options.getMember())
        .build())
        .build();
  }
  @Override
  public ThenaSqlClient.SqlTuple findByExternalId(String externalId) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append(
"""
  SELECT *
  FROM {member} as member
  WHERE external_id = $1
  FETCH FIRST ROW ONLY
""".replace("{member}", options.getMember()))
        .build())
        .props(Tuple.of(externalId))
        .build();
  }
  @Override
  public SqlTuple findByExtIdAndAliasIdAndRef(String extId, UUID uuid, String ref) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append(
"""
  SELECT *
  FROM {member} as member
  LEFT JOIN {alias} as alias ON(member.alias_id = alias.id)
  LEFT JOIN {tenant} as ref ON(ref.id = alias.ref_tenant_id)
  
  WHERE (member.external_id = $1 OR $1 IS NULL)
    AND (member.alias_id = $2 OR $2 IS NULL)
    AND (alias.ref_tenant_id = $3 OR ref.name = $3 OR $3 IS NULL)
""".replace("{member}", options.getMember())
   .replace("{alias}", options.getAlias())
   .replace("{tenant}", options.getTenant()) 
    ).build())
    .props(Tuple.of(extId, uuid, ref))
    .build();
  }  
  @Override
  public SqlTuple getByExtIdAndAliasId(String externalId, UUID aliasId) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append(
"""
  SELECT *
  FROM {member} as member
  WHERE external_id = $1 AND alias_id = $2
  FETCH FIRST ROW ONLY
""".replace("{member}", options.getMember()))
        .build())
        .props(Tuple.of(externalId, aliasId))
        .build();
  }
  @Override
  public ThenaSqlClient.SqlTuple insertOne(Member newRepo) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append(
"""
INSERT INTO {member} 
  (id, external_id, alias_status, alias_id) 
  VALUES($1, $2, $3, $4)
""".replace("{member}", options.getMember()))
        .build())
        .props(Tuple.from(Arrays.asList(
          newRepo.getId(),
          newRepo.getExternalId(), 
          newRepo.getAliasStatus(),
          newRepo.getAliasId()
        )))
        .build();
  }
  @Override
  public ThenaSqlClient.SqlTuple updateOne(Member newRepo) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append(
"""
UPDATE {member} 
SET
  alias_id = $2,
  alias_status = $3
WHERE id = $1
""".replace("{member}", options.getMember()))
        .build())
        .props(Tuple.of(
            newRepo.getId(),
            newRepo.getAliasId(),
            newRepo.getAliasStatus()
        ))
        .build();
  }
  @Override
  public ThenaSqlClient.SqlTuple deleteOne(Member newRepo) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("DELETE FROM ").append(options.getMember())
        .append(" WHERE id = $1")
        .build())
        .props(Tuple.of(newRepo.getId()))
        .build();
  }
  @Override
  public SqlTuple getById(String id) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT * FROM ").append(options.getMember())
        .append(" WHERE id = $1")
        .append(" FETCH FIRST ROW ONLY")
        .build())
        .props(Tuple.of(TableUtils.toUuid(id)))
        .build();
  }
  @Override
  public Function<Row, Member> defaultMapper() {
    return MemberRegistrySqlImpl::repo;
  }
  private static Member repo(Row row) {
    
    return ImmutableMember.builder()
        .id(row.getUUID("id"))
        .aliasId(row.getUUID("alias_id"))
        .aliasStatus(row.getBoolean("alias_status"))
        .externalId(row.getString("external_id"))
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
