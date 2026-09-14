package io.resys.thena.grim.spi.sql;

/*-
 * #%L
 * thena-grim-client
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

import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.resys.thena.api.entities.grim.GrimRps;
import io.resys.thena.api.entities.grim.ImmutableGrimRps;
import io.resys.thena.datasource.ImmutableSql;
import io.resys.thena.datasource.ImmutableSqlTuple;
import io.resys.thena.datasource.ImmutableSqlTupleList;
import io.resys.thena.datasource.ThenaSqlClient.Sql;
import io.resys.thena.datasource.ThenaSqlClient.SqlTuple;
import io.resys.thena.datasource.ThenaSqlClient.SqlTupleList;
import io.resys.thena.grim.spi.datasource.GrimRpsRegistry;
import io.resys.thena.storesql.support.SqlStatement;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.Tuple;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class GrimRpsRegistrySqlImpl implements GrimRpsRegistry {
  private final GrimTableNames options;

  @Override
  public Sql findAll() {
    return ImmutableSql.builder()
        .value(new SqlStatement()
        .append("SELECT * FROM ").append(options.getGrimRps()).ln()
        .build())
        .build();
  }
  @Override
  public SqlTupleList insertAll(Collection<GrimRps> rps) {
    return ImmutableSqlTupleList.builder()
        .value(new SqlStatement()
        .append("INSERT INTO ").append(options.getGrimRps()).append(" ").ln()
        .append("""
  (
  id,
  created_at,
  external_id,
  locale,
  workflow_name,
  form_name,
  form_version,
  rating,
  comment)""").ln()
        .append(" VALUES($1, $2, $3, $4, $5, $6, $7, $8, $9)").ln()
        .build())
        .props(rps.stream()
            .map(entry -> Tuple.from(new Object[]{
                entry.getId(),
                entry.getCreatedAt(),
                entry.getExternalId(),
                entry.getLocale(),
                entry.getWorkflowName(),
                entry.getFormName(),
                entry.getFormVersion(),
                entry.getRating(),
                entry.getComment(),
             }))
            .collect(Collectors.toList()))
        .build();
  }


  @Override
  public SqlTuple getById(String id) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT * FROM ").append(options.getGrimRps()).ln()
        .append(" WHERE id = $1").ln()
        .build())
        .props(Tuple.of(id))
        .build();
  }


  @Override
  public Sql createTable() {
    return ImmutableSql.builder().value(new SqlStatement().ln()

    .append("CREATE TABLE IF NOT EXISTS ").append(options.getGrimRps()).ln()
    .append("""
    (
      id            VARCHAR(40) PRIMARY KEY,
      created_at    TIMESTAMPTZ NOT NULL,
      external_id   VARCHAR(255) NOT NULL,
      locale        VARCHAR(40) NOT NULL,
      workflow_name VARCHAR(255) NOT NULL,
      form_name     VARCHAR(255) NOT NULL,
      form_version  VARCHAR(255) NOT NULL,
      rating        INT NOT NULL,
      comment       TEXT NULL
    );
    """).ln()

    .append("CREATE INDEX IF NOT EXISTS ").append(options.getGrimRps()).append("_WK_NAME_INDEX")
    .append(" ON ").append(options.getGrimRps()).append(" (workflow_name);").ln()

    .append("CREATE INDEX IF NOT EXISTS ").append(options.getGrimRps()).append("_FORM_NAME_INDEX")
    .append(" ON ").append(options.getGrimRps()).append(" (form_name);").ln()

    .build()).build();
  }


  @Override
  public Sql createConstraints() {
    return ImmutableSql.builder().value(new SqlStatement()
    // NONE ATM
    .build()).build();
  }


  @Override
  public Sql dropTable() {
    return ImmutableSql.builder().value(new SqlStatement()
        .append("DROP TABLE ").append(options.getGrimRps()).append(";").ln()
        .build()).build();
  }


  @Override
  public Function<Row, GrimRps> defaultMapper() {
    return (row) -> ImmutableGrimRps.builder()
        .id(row.getString("id"))
        .createdAt(row.getOffsetDateTime("created_at"))
        .externalId(row.getString("external_id"))
        .locale(row.getString("locale"))
        .workflowName(row.getString("workflow_name"))
        .formName(row.getString("form_name"))
        .formVersion(row.getString("form_version"))
        .rating(row.getInteger("rating"))
        .comment(row.getString("comment"))
        .build();
  }


  @Override
  public SqlTuple deleteOneById(String id) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("DELETE FROM ").append(options.getGrimRps())
        .append(" WHERE id = $1")
        .build())
        .props(Tuple.of(id))
        .build();
  }
}
